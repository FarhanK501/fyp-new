package com.finalyearproject.precolorvisualizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import org.opencv.core.Point;
import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class UserMode extends Activity {

	public static ImageView frontView;
	public static boolean draw = false;
	public static Bitmap cannyBmp, sendBmp;
	public static int replacementColor = Color.WHITE;
	public static int targetColor;
	int ActivityAcquirePicture = 1;
	int xCord, yCord;
	Uri selectedImage, imageUri;
	ImageView backView;
	Bitmap bmp;
	Point p = new Point();
	UserModeDrawing umd;
	public static RelativeLayout userView;
	AlertDialog.Builder builder;

	/**
	 * Progress Bar that will be shown when app is busy
	 */
	ProgressDialog currentProgress;

	private final int SELECT_PICTURE = 100, CAMERA_REQUEST = 101;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.usermode, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@SuppressLint({ "ResourceAsColor", "NewApi" })
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int menuId = item.getItemId();
		switch (menuId) {
		case R.id.colorpicker:

			ColorPicker();
			break;
		case R.id.openGallery:
			callTehGal();
			break;

		case R.id.drawLine:
			if (!draw) {
				draw = true;
				item.setIcon(R.drawable.pencilnot);
			} else {
				draw = false;
				item.setIcon(R.drawable.pencil);
			}
			break;
		case R.id.userSave:

			new SaveCurrentImage().execute();

			break;

		}
		return super.onOptionsItemSelected(item);

	}

	private void callTehGal() {
		Intent galleryIntent = new Intent();
		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, SELECT_PICTURE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.usermode);
		initializer();
		chooseMenu();
	}

	private void chooseMenu() {
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Use Image From")
				.setCancelable(true)
				.setPositiveButton("Camera",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								callTehCamIntent();
							}
						})
				.setNegativeButton("Gallery",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								callTehGal();
							}
						});
		AlertDialog alert = builder.create();

		alert.show();

	}

	protected void callTehCamIntent() {
		Intent camInt = new Intent("android.media.action.IMAGE_CAPTURE");
		startActivityForResult(camInt, CAMERA_REQUEST);

	}

	private void initializer() {
		currentProgress = new ProgressDialog(UserMode.this);
		currentProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		backView = (ImageView) findViewById(R.id.backimageview);
		frontView = (ImageView) findViewById(R.id.frontimageview);
		backView.setScaleType(ScaleType.FIT_XY);
		frontView.setScaleType(ScaleType.FIT_XY);
		umd = (UserModeDrawing) findViewById(R.id.userCanvas);
		userView = (RelativeLayout) findViewById(R.id.userRelativity);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SELECT_PICTURE:
				onButtonPressed(data);

				break;
			case CAMERA_REQUEST:
				onButtonPressed(data);
				break;
			}
		}

	}

	private void onButtonPressed(Intent data) {
		Uri image = data.getData();
		if (image != null) {
			try {
				bmp = Images.Media.getBitmap(getContentResolver(), image);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Bundle bundle = data.getExtras();
			bmp = ( Bitmap ) bundle.get("data");
		}
		
		afterGettingBmp(bmp);
	}

	/**
	 * After Getting Image from gallery Or Camera
	 */
	private void afterGettingBmp(Bitmap bmp) {

		Bitmap finalBmp = null;

		if (bmp.getWidth() > bmp.getHeight()) {
			Matrix matrix = new Matrix();
			matrix.postRotate(90);
			finalBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
					bmp.getHeight(), matrix, true);
			finalBmp = finalBmp.copy(Config.ARGB_8888, true);
			finalBmp.setHasAlpha(true);
			finalBmp = Bitmap.createScaledBitmap(finalBmp, 720, 1080, true);
		} else {
			finalBmp = bmp.copy(Config.ARGB_8888, true);
			finalBmp.setHasAlpha(true);
			finalBmp = Bitmap.createScaledBitmap(finalBmp, 720, 1080, true);
		}

		backView.setImageBitmap(finalBmp);
	}

	protected void ColorPicker() {
		AmbilWarnaDialog dg = new AmbilWarnaDialog(this, Color.BLACK, true,
				new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int colors) {
						replacementColor = colors;
					}

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {
						Toast.makeText(getBaseContext(),
								"CHoosing a color isn't easy! :D",
								Toast.LENGTH_SHORT).show();

					}
				});
		dg.show();
	}

	@Override
	public void onBackPressed() {
		this.finish();
		startActivity(new Intent(this, MainActivity.class));

		// super.onBackPressed();
	}

	/**
	 * 
	 * @author Farhan Khan Saving current Image on view whether it's edged or
	 *         just simple image. We just want to save the image.
	 * 
	 */
	class SaveCurrentImage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			Bitmap resultImage;
			resultImage = Bitmap.createBitmap(bmp);
			resultImage.copy(Config.ARGB_8888, true);
			userView.setDrawingCacheEnabled(true);
			resultImage = userView.getDrawingCache();
			String root = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
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
				userView.setDrawingCacheEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// Intent get = getIntent();
				// finish();
				// startActivity(get);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			currentProgress.hide();

			Toast.makeText(getBaseContext(),
					"Image Saved at/sdcard/PreColorResults/",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPreExecute() {

			currentProgress.setTitle(" Please Wait...");
			currentProgress.setMessage("While Saving Image to Gallery");
			currentProgress.setCancelable(false);
			currentProgress.show();

		}
	}
}