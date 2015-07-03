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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Gravity;
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

	/**
	 * Image View that will hold down our image with edges and actual images.
	 */
	ImageView backView, frontView;

	/**
	 * Variable that will holds down the request for camera or gallery
	 */
	public final int GALLERY_REQUEST = 101, CAMERA_REQUEST = 102;

	/**
	 * Image that is selected by user Either from gallery or from Camera
	 */
	Bitmap galleryBmp;

	/**
	 * those are the bitmaps that will hold down the current state of the bitmap
	 * i.e. FloodFilling for different segmentations and last point where we
	 * done filling color.
	 */
	Bitmap flood1, cannys, flood11, holdLastPic, white, black, laplac,
			threshold, sobel;

	/**
	 * The whole layout where we are showing our images and stuff in the end we
	 * save the whole view and saving it in gallery
	 */
	RelativeLayout view;

	/**
	 * Initializing the replacement color that is byDefault will be shown on
	 * Color Picker Dialog
	 */
	int replacementColor = Color.GRAY;

	/**
	 * alert dialog holds some of the options to be chosen from user like
	 * getting image from camera or gallery etc..
	 */
	AlertDialog.Builder builder;

	/**
	 * showing progress when process is being executed so user can know and wait
	 * unitl process is done
	 */
	boolean showProgress = false;

	/**
	 * Progress Bar that will be shown when app is busy
	 */
	ProgressDialog currentProgress;

	/**
	 * A boolean flag that will allow users to Save current image. Handling of a
	 * case where user didn't choose any image and tries to save
	 */
	private boolean canSaveImage = false;

	/**
	 * A boolean flag that will use to handle the case where user didn't choose
	 * any image neither he applied any segmentation
	 */
	private boolean canClearEdges = false;

	/**
	 * A boolean flag that is use to handle the case where user didn't choose
	 * any image tries to apply segmentation
	 */
	private boolean canApplySegmentation = false;

	/**
	 * A Toast Message that is appear
	 */
	private static Toast toastShow;
	
	/**
	 * Small alert that is shown when user tap on folder gallery button
	 */
	AlertDialog alert;
	/**
	 * default onCreate Method
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.automode);

		// initialize views
		initializer();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.segmentation, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// getting the back image and saving in Bitmap
		backView.setDrawingCacheEnabled(true);
		Bitmap bmpToBeSegmented = backView.getDrawingCache();

		int id = item.getItemId();
		switch (id) {

		// Showing a dialog with color chooser
		case R.id.SagColPic:

			ColorPicker();
			break;

		// clearing edges that was drawn before
		// case R.id.segClearFrontView:
		//
		// if( canClearEdges ){
		// clearEdge();
		// } else {
		// showToast(this, "Please Apply segmentation");
		// }
		//
		// break;
		//
		// saving image in gallery
		case R.id.segSave:

			if (canSaveImage) {
				saveImage();
			} else {
				showToast(this, "Please Choose an image before saving");
			}
			break;

		// a dialog with two buttons, one for camera, one for gallery
		case R.id.SegGal:

			showOption();
			break;

		// applying canny
		// case R.id.segCanny:
		//
		// if( canApplySegmentation ){
		// AutoApplyCanny( bmpToBeSegmented );
		// canClearEdges = true;
		// } else {
		// showToast(this, "Please Choose an image before segmentation");
		// }
		//
		// break;

		// applying laplacian
		// case R.id.segLap:
		//
		// if( canApplySegmentation ){
		// applyLaplacian( bmpToBeSegmented );
		// canClearEdges = true;
		// } else {
		// showToast(this, "Please Choose an image before segmentation");
		// }
		// break;

		// applying simple threshold
		// case R.id.segThreshold:
		//
		// if( canApplySegmentation ){
		// thresholding( bmpToBeSegmented );
		// canClearEdges = true;
		// } else {
		// showToast(this, "Please Choose an image before segmentation");
		// }
		// break;

		// applying sobel image segmentation
		// case R.id.segSobel:
		//
		// if( canApplySegmentation ){
		// sobel(bmpToBeSegmented);
		// canClearEdges = true;
		// } else {
		// showToast(this, "Please Choose an image before segmentation");
		// }
		// break;

		// handle leakage of memory
		default:

			backView.setDrawingCacheEnabled(false);
			bmpToBeSegmented.recycle();

		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Showing a dialog with two options
	 */
	private void showOption() {

		builder = new AlertDialog.Builder(this);
		builder.setMessage("Use Image From")
				.setCancelable(true)
				.setPositiveButton("Camera",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// clear views before carry out
								// new one
								clearView();
								callTehCamIntent();
							}
						})
				.setNegativeButton("Gallery",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// clear views before
								clearView();
								callTheGalIntent();
							}
						});
		alert = builder.create();

		alert.show();
	}

	/**
	 * Before we carry out new images we had to clear views
	 */
	private void clearView() {
		// clear the backview holding actual image
		backView.setImageBitmap(null);
		backView.destroyDrawingCache();

		// clearing the front image view holding edges/segments
		frontView.setImageBitmap(null);
		frontView.destroyDrawingCache();
	}

	/**
	 * clear the edges of current image
	 * 
	 * @param bmp
	 *            with edges
	 * @return bmp without edges
	 */
	private void clearEdge() {
		new ClearEdges().execute();
	}

	/**
	 * Save image in Gallery
	 * 
	 * @param bmp
	 */
	private void saveImage() {
		new SaveCurrentImage().execute();
	}

	/**
	 * it's after we applied LAPLACIAN algo Here we are just getting the
	 * coordinates of touch and filling color to it We color on those places at
	 * the front view where we see Black color. Because now frontView is in only
	 * two( Binary ) colors and we just had to supposed white color as edges..
	 * 
	 * @param galleryBmp
	 */
	private void applyLaplacian(Bitmap bmp) {
		new LaplacianTechnique().execute();
	}

	/**
	 * it's after we applied CANNY algo Here we are just getting the coordinates
	 * of touch and filling color to it We color on those places at the front
	 * view where we see Black color. Because now frontView is in only two(
	 * Binary ) colors and we just had to supposed white color as edges..
	 * 
	 * @param galleryBmp
	 */
	private void AutoApplyCanny(Bitmap bmp) {
		new CannyTechnique().execute();
	}

	/**
	 * Applying Threshold
	 * 
	 * @param bm
	 * @param x
	 * @param y
	 */
	private void thresholding(Bitmap galleryBmp) {

		threshold = galleryBmp;
		new ThresholdTechnique().execute();
	}

	/*
	 * Applying Sobel
	 * 
	 * @param bm
	 * 
	 * @param x
	 * 
	 * @param y
	 */
	private void sobel(Bitmap galleryBmp) {

		sobel = galleryBmp;
		new SobelTechnique().execute();
	}

	/**
	 * Here we are drawing a selected segment with the selected color
	 * 
	 * @param bm
	 * @param x
	 * @param y
	 */

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

		clearEdge();
	}

	/**
	 * Apply canny which is good from others at the moment
	 * 
	 * @param bmp
	 * @return Segmented Image
	 */
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

	/**
	 * Applying laplacian
	 * 
	 * @param bmp
	 * @return segmented Bitmap
	 */
	private Bitmap applyLaplac(Bitmap bmp) {

		Bitmap op = galleryBmp.copy(Config.ARGB_8888, true);
		Mat lapMat = new Mat(op.getWidth(), op.getHeight(), CvType.CV_8UC4);
		Utils.bitmapToMat(op, lapMat);
		Imgproc.cvtColor(lapMat, lapMat, Imgproc.COLOR_BGRA2GRAY);
		Imgproc.Laplacian(lapMat, lapMat, CvType.CV_8U);
		Imgproc.cvtColor(lapMat, lapMat, Imgproc.COLOR_GRAY2BGRA);
		Utils.matToBitmap(lapMat, op);
		bmp = op;

		return bmp;
	}

	/**
	 * applying thresholding
	 * 
	 * @param bmp
	 * @return segmented bitmap
	 */
	private Bitmap applyThreshold(Bitmap bmp) {
		Bitmap op = galleryBmp.copy(Config.ARGB_8888, true);
		Mat threshMat = new Mat(op.getWidth(), op.getHeight(), CvType.CV_8UC4);
		Utils.bitmapToMat(op, threshMat);
		Imgproc.cvtColor(threshMat, threshMat, Imgproc.COLOR_BGRA2GRAY);
		Imgproc.threshold(threshMat, threshMat, 100, 255, Imgproc.THRESH_OTSU);
		Imgproc.cvtColor(threshMat, threshMat, Imgproc.COLOR_GRAY2BGRA);
		Utils.matToBitmap(threshMat, op);
		bmp = op;

		return bmp;
	}

	/**
	 * applying thresholding
	 * 
	 * @param bmp
	 * @return segmented bitmap
	 */
	private Bitmap applySobel(Bitmap bmp) {
		Bitmap op = galleryBmp.copy(Config.ARGB_8888, true);
		Mat threshMat = new Mat(op.getWidth(), op.getHeight(), CvType.CV_8UC4);
		Utils.bitmapToMat(op, threshMat);
		Imgproc.cvtColor(threshMat, threshMat, Imgproc.COLOR_BGRA2GRAY);
		// Imgproc.sobel(threshMat, threshMat, 100, 255, Imgproc.THRESH_OTSU);
		Imgproc.Sobel(threshMat, threshMat, threshMat.depth(), 2, 2);
		Imgproc.cvtColor(threshMat, threshMat, Imgproc.COLOR_GRAY2BGRA);
		Utils.matToBitmap(threshMat, op);
		bmp = op;

		return bmp;
	}

	/**
	 * When new intent started with some specific information here we use this
	 * to handle the camera and gallery data
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			// gallery request
			case GALLERY_REQUEST:

				onButtonPressed(data);
				break;

			// camera request
			case CAMERA_REQUEST:

				forCamera();
				break;

			}
		}
	}
	private void forCamera(){
		  File file = new File(Environment.getExternalStoragePublicDirectory(
				  				Environment.DIRECTORY_DCIM).getPath(), "tempPreColor.jpg");
        Uri uri = Uri.fromFile(file);
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            onGetImage(bitmap);
        
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	}
	
	/**
	 * For getting full size image for camera intent
	 */
	

	/**
	 * Initializing all our views
	 */
	private void initializer() {
		backView = (ImageView) findViewById(R.id.dImageView);
		frontView = (ImageView) findViewById(R.id.dImageViewFront);
		view = (RelativeLayout) findViewById(R.id.relativity);
		currentProgress = new ProgressDialog(AutoMode.this);
		currentProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	}

	/**
	 * Either Camera Button pressed or gallery In Both cases we need to get the
	 * path of the image and then save it in the variabel
	 * 
	 * @param Intent
	 */
	private void onButtonPressed(Intent data) {
		Uri currentData = data.getData();
		if (currentData != null) {
			Uri image = currentData;

			try {
				galleryBmp = Images.Media
						.getBitmap(getContentResolver(), image);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			
			Bundle extras = data.getExtras();
			galleryBmp = (Bitmap) extras.get("data");
		}
		onGetImage(galleryBmp);

	}

	/**
	 * After getting image from camera or gallery We have to do some calculation
	 * on which we rotate the screen or image
	 * 
	 * @param bm
	 */
	private void onGetImage(Bitmap bm) {

		if (bm.getWidth() > bm.getHeight()) {
			Bitmap bMapRotate = null;
			Matrix mat = new Matrix();
			mat.postRotate(90);
			bMapRotate = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
					bm.getHeight(), mat, true);
			bm.recycle();
			bm = null;
			galleryBmp = Bitmap.createScaledBitmap(bMapRotate,
					backView.getWidth(), backView.getHeight(), true);
			backView.setImageBitmap(bMapRotate);
		} else {
			galleryBmp = Bitmap.createScaledBitmap(bm, backView.getWidth(),
					backView.getHeight(), true);
			backView.setImageBitmap(bm);
		}

		canApplySegmentation = true;
		canSaveImage = true;

		AutoApplyCanny(galleryBmp);
	}

	/**
	 * A simple dialog holds color picker User can select color from it
	 */
	protected void ColorPicker() {
		AmbilWarnaDialog pickerDialog = new AmbilWarnaDialog(this, Color.BLACK,
				true, new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int colors) {
						replacementColor = colors;

						// we are applying colors to those area where white
						// color isn't present
						// but what if user choose white color?
						// we should take care of that and little bit tinkering
						// of white color
						// wouldn't hurt much
						// int color =
						// (int)Long.parseLong(String.valueOf(replacementColor),
						// 16);
						// int r = (color >> 16) & 0xFF;
						// int g = (color >> 8) & 0xFF;
						// int b = (color >> 0) & 0xFF;

						if (replacementColor == Color.WHITE) {

							replacementColor = Color.parseColor("#fefefe");

						} // else {
							// by this we can add default transparency to
							// applying color because
							// in some mobile we can't show proper color picker
							// dialog
							// replacementColor = Color.argb(10, r, g, b);
						// }

					}

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {
						Toast.makeText(getBaseContext(),
								"You did not choose any Color",
								Toast.LENGTH_SHORT).show();

					}
				});

		pickerDialog.show();
	}

	/**
	 * On Back Press we need to go back to the main activity where we are
	 * showing choose b/w modes
	 * Destroying each view that was added in the current context, so we can
	 * prevent memory leakage issue
	 */
	@Override
	public void onBackPressed() {
		alert.dismiss();
		currentProgress.dismiss();
		this.finish();
		startActivity(new Intent(this, MainActivity.class));
	}

	/**
	 * calling Camera
	 */
	private void callTehCamIntent() {
		String pathToCard = "file://"+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
		Intent camInt = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		Uri uri = Uri.parse(pathToCard+"/tempPreColor.jpg");
		camInt.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
		startActivityForResult(camInt, CAMERA_REQUEST);

	}

	/**
	 * calling Gallery
	 */
	private void callTheGalIntent() {
		Intent galleryIntent = new Intent();
		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, GALLERY_REQUEST);

	}

	/**
	 * 
	 * @author Farhan Khan A sub Async class for applying canny in the
	 *         background, so screen wont hang again also is a good practice to
	 *         show that some background Stuff is going on so user wont feel lag
	 *         or irritate
	 */
	class CannyTechnique extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
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
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			currentProgress.hide();
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

		@Override
		protected void onPreExecute() {
			currentProgress.setTitle(" Please Wait...");
			currentProgress.setMessage(" Segmentation is under process. ");
			currentProgress.setCancelable(false);
			currentProgress.show();
		}
	}

	/**
	 * 
	 * @author Farhan Khan A sub Async class for applying Laplacian in the
	 *         background, so screen wont hang again also is a good practice to
	 *         show that some background Stuff is going on so user wont feel lag
	 *         or irritate
	 */
	class LaplacianTechnique extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			laplac = galleryBmp;
			laplac = laplac.copy(Config.ARGB_8888, true);
			laplac = applyLaplac(laplac);

			for (int i = 0; i < laplac.getWidth(); i++) {
				for (int j = 0; j < laplac.getHeight(); j++) {
					if (laplac.getPixel(i, j) == Color.BLACK) {
						laplac.setHasAlpha(true);
						laplac.setPixel(i, j, Color.TRANSPARENT);
					}

				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			currentProgress.hide();
			frontView.setImageBitmap(laplac);
			flood1 = laplac.copy(Config.ARGB_8888, true);

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

		@Override
		protected void onPreExecute() {
			currentProgress.setTitle(" Please Wait...");
			currentProgress
					.setMessage("While Applying Laplacian Edge Detection");
			currentProgress.setCancelable(false);
			currentProgress.show();
		}
	}

	/**
	 * 
	 * @author Farhan Khan A sub Async class for applying canny in the
	 *         background, so screen wont hang again also is a good practice to
	 *         show that some background Stuff is going on so user wont feel lag
	 *         or irritate
	 */
	class ThresholdTechnique extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			threshold = galleryBmp;
			threshold = threshold.copy(Config.ARGB_8888, true);
			threshold = applyThreshold(threshold);

			for (int i = 0; i < threshold.getWidth(); i++) {
				for (int j = 0; j < threshold.getHeight(); j++) {
					if (threshold.getPixel(i, j) == Color.BLACK) {
						threshold.setHasAlpha(true);
						threshold.setPixel(i, j, Color.TRANSPARENT);
					}

				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			currentProgress.hide();
			frontView.setImageBitmap(threshold);
			flood1 = threshold.copy(Config.ARGB_8888, true);

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

		@Override
		protected void onPreExecute() {
			currentProgress.setTitle(" Please Wait...");
			currentProgress.setMessage(" While Applying Simple Edge Detection");
			currentProgress.setCancelable(false);
			currentProgress.show();
		}
	}

	/**
	 * 
	 * @author Farhan Khan A sub Async class for applying canny in the
	 *         background, so screen wont hang again also is a good practice to
	 *         show that some background Stuff is going on so user wont feel lag
	 *         or irritate
	 */
	class SobelTechnique extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			sobel = galleryBmp;
			sobel = sobel.copy(Config.ARGB_8888, true);
			sobel = applySobel(sobel);

			for (int i = 0; i < sobel.getWidth(); i++) {
				for (int j = 0; j < sobel.getHeight(); j++) {
					if (sobel.getPixel(i, j) == Color.BLACK) {
						sobel.setHasAlpha(true);
						sobel.setPixel(i, j, Color.TRANSPARENT);
					}

				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			currentProgress.hide();
			frontView.setImageBitmap(sobel);
			flood1 = sobel.copy(Config.ARGB_8888, true);

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

		@Override
		protected void onPreExecute() {
			currentProgress.setTitle(" Please Wait...");
			currentProgress.setMessage("While Applying Sobel Edge Detection. ");
			currentProgress.setCancelable(false);
			currentProgress.show();
		}
	}

	/**
	 * A sub class which will clear the edges on pressing tapping the menu clear
	 * edges
	 */
	class ClearEdges extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

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
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			frontView.setImageBitmap(white);
			frontView.setDrawingCacheEnabled(false);
			currentProgress.hide();
			Toast.makeText(getBaseContext(), "Color Applied!",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPreExecute() {
			frontView.setDrawingCacheEnabled(true);
			white = frontView.getDrawingCache();

			currentProgress.setTitle(" Please Wait...");
			currentProgress.setMessage("Applying Color To The Segmented Area ");
			currentProgress.setCancelable(false);
			currentProgress.show();
		}
	}

	public void showToast(Activity actRef, String message) {

		if (toastShow == null
				|| toastShow.getView().getWindowVisibility() != View.VISIBLE) {
			toastShow = Toast.makeText(actRef, message, Toast.LENGTH_SHORT);
			toastShow.setGravity(Gravity.CENTER, 0, 0);
			toastShow.show();
		} else {
			toastShow.cancel();
		}
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
			resultImage = Bitmap.createBitmap(galleryBmp);
			resultImage.copy(Config.ARGB_8888, true);
			view.setDrawingCacheEnabled(true);
			resultImage = view.getDrawingCache();
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
				view.setDrawingCacheEnabled(false);
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
