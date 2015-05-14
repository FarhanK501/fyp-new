package com.finalyearproject.precolorvisualizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class AutoMode extends Activity {
	ImageView backView, frontView;
	public final int GALLERY_REQUEST = 101, CAMERA_REQUEST = 102;
	Bitmap galleryBmp, cannyBmp;
	Bitmap flood1, cannys, flood11, holdLastPic, white, black;
	RelativeLayout view;
	int replacementColor = Color.GRAY;
	AlertDialog.Builder builder;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.automode);

		initializer();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.segmentation, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.SagColPic:
			ColorPicker();
			break;

		case R.id.segClearFrontView:
			Toast.makeText(getBaseContext(), "Edges Cleared!", 0).show();

			frontView.setDrawingCacheEnabled(true);
			white = frontView.getDrawingCache();

			white = white.copy(Config.ARGB_8888, true);
			for (int i = 0; i < white.getWidth(); i++) {
				for (int j = 0; j < white.getHeight(); j++) {
					if (white.getPixel(i, j) == Color.WHITE) {
						white.setPixel(i, j, Color.TRANSPARENT);
					}
				}
			}
			Mat m = new Mat(white.getWidth(), white.getHeight(), CvType.CV_8UC4);
			Utils.bitmapToMat(white, m);
			Imgproc.morphologyEx(m, m, Imgproc.MORPH_OPEN, new Mat());
			Utils.matToBitmap(m, white);
			frontView.setImageBitmap(white);
			frontView.setDrawingCacheEnabled(false);

			break;
		case R.id.segSave:
			Toast.makeText(getBaseContext(),
					"Image Saved at/sdCard/Pictures/PreColorResults/",
					Toast.LENGTH_SHORT).show();

			Bitmap resultImage;
			resultImage = Bitmap.createBitmap(galleryBmp);
			resultImage.copy(Config.ARGB_8888, true);
			view.setDrawingCacheEnabled(true);
			resultImage = view.getDrawingCache();
			String root = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/Pictures".toString();
			File myDir = new File(root + "/PreColorResults");

			myDir.mkdirs();

			Random generator = new Random();
			long n = 1000000;
			n = generator.nextLong();
			String fname = "Image-" + n + ".png";
			File file = new File(myDir, fname);

			Log.i("" + n, "" + file);
			if (file.exists())
				file.delete();
			try {
				FileOutputStream out = new FileOutputStream(file);
				resultImage.compress(Bitmap.CompressFormat.PNG, 100, out);
				sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
						Uri.parse("file://"
								+ Environment.getExternalStorageDirectory())));
				out.flush();
				out.close();
				// frontView.setDrawingCacheEnabled(false);
				view.setDrawingCacheEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// Intent get = getIntent();
				// finish();
				// startActivity(get);
			}
			// ProgressBar pb = new ProgressBar(getBaseContext());

			break;
		case R.id.SegGal:
			// a dialog with two buttons, one for camera, one for gallery
			builder = new AlertDialog.Builder(this);
			builder.setMessage("Use Image From")
					.setCancelable(true)
					.setPositiveButton("Camera",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									callTehCamIntent();
								}
							})
					.setNegativeButton("Gallery",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									callTheGalIntent();
								}
							});
			AlertDialog alert = builder.create();

			alert.show();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	private void AutoApplyCanny(Bitmap galleryBmp) {

		cannys = galleryBmp;
		cannys = cannys.copy(Config.ARGB_8888, true);
		cannys = applyCanny(cannys);

		for (int i = 0; i < cannys.getWidth(); i++) {
			for (int j = 0; j < cannys.getHeight(); j++) {
				if (cannys.getPixel(i, j) == Color.BLACK) {
					cannys.setHasAlpha(true);
					cannys.setPixel(i, j, Color.TRANSPARENT);
				}

			}
		}

		frontView.setImageBitmap(cannys);
		flood1 = cannys.copy(Config.ARGB_8888, true);

		frontView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				int x, y;
				x = (int) arg1.getX();
				y = (int) arg1.getY();

				sendToFill(flood1, x, y);

				return false;
			}
		});

	}

	public void sendToFill(Bitmap bm, int x, int y) {
		holdLastPic = bm.copy(Config.ARGB_8888, true);
		QueueLinearFloodFiller qlfl = new QueueLinearFloodFiller(holdLastPic);

		qlfl.useImage(holdLastPic);
		qlfl.setTargetColor(Color.TRANSPARENT);
		qlfl.setFillColor(replacementColor);
		qlfl.floodFill(x, y);
		holdLastPic = qlfl.getImage();
		frontView.setImageBitmap(holdLastPic);

		flood1 = holdLastPic;
		flood11 = holdLastPic;
		Toast.makeText(getBaseContext(), "Color Applied",
				Toast.LENGTH_SHORT - 1).show();
	}

	private Bitmap applyCanny(Bitmap bmp) {
		Bitmap op = galleryBmp.copy(Config.ARGB_8888, true);
		Size s = new Size(3, 3);
		Mat canyMat = new Mat(op.getWidth(), op.getHeight(), CvType.CV_8UC4);
		Utils.bitmapToMat(op, canyMat);
		Imgproc.cvtColor(canyMat, canyMat, Imgproc.COLOR_RGBA2BGR);
		canyMat.convertTo(canyMat, CvType.CV_8UC4);
		Imgproc.blur(canyMat, canyMat, s);
		Imgproc.Canny(canyMat, canyMat, 50, 50);
		Imgproc.morphologyEx(canyMat, canyMat, 4, new Mat());
		Imgproc.cvtColor(canyMat, canyMat, Imgproc.COLOR_GRAY2BGRA);
		Utils.matToBitmap(canyMat, op);
		bmp = op;
		return bmp;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case GALLERY_REQUEST:
				onButtonPressed(data.getData());

				break;
			case CAMERA_REQUEST:
				onButtonPressed(data.getData());
				break;
			}
		}
	}

	private void initializer() {
		backView = (ImageView) findViewById(R.id.dImageView);
		frontView = (ImageView) findViewById(R.id.dImageViewFront);
		view = (RelativeLayout) findViewById(R.id.relativity);
	}

	private void onButtonPressed(Uri r) {
		Uri image = r;
		try {
			galleryBmp = Images.Media.getBitmap(getContentResolver(), image);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		onGetImage(galleryBmp);
	}

	private void onGetImage(Bitmap bm) {
		
		if(bm.getWidth() > bm.getHeight())
        {
            Bitmap bMapRotate=null;
            Matrix mat=new Matrix();
            mat.postRotate(90);
        bMapRotate = Bitmap.createBitmap(bm, 0, 0,bm.getWidth(),bm.getHeight(), mat, true);
        bm.recycle();
        bm=null;
        galleryBmp = Bitmap.createScaledBitmap(bMapRotate, backView.getWidth(),
				backView.getHeight(), true);
        backView.setImageBitmap(bMapRotate);
        }else {
        	galleryBmp = Bitmap.createScaledBitmap(bm, backView.getWidth(),
    				backView.getHeight(), true);
        	backView.setImageBitmap(bm);
        }
		
		AutoApplyCanny(galleryBmp);
	}

	protected void ColorPicker() {
			AmbilWarnaDialog dg = new AmbilWarnaDialog(this, Color.BLACK,
					true, new OnAmbilWarnaListener() {
						@Override
						public void onOk(AmbilWarnaDialog dialog, int colors) {
							replacementColor = colors;

						}
						@Override
						public void onCancel(AmbilWarnaDialog dialog) {
							Toast.makeText( getBaseContext() ,
									"You did not choose any Color",
									Toast.LENGTH_SHORT).show();

						}
					});

			dg.show();
		}	

	@Override
	public void onBackPressed() {
		this.finish();
		startActivity(new Intent(this, MainActivity.class));

	}

	protected void callTehCamIntent() {
		Intent camInt = new Intent("android.media.action.IMAGE_CAPTURE");
		startActivityForResult(camInt, CAMERA_REQUEST);

	}

	public void callTheGalIntent() {
		Intent galleryIntent = new Intent();
		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, GALLERY_REQUEST);

	}
}
