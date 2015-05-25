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

@SuppressLint("NewApi") public class UserMode extends Activity {

	/**
	 * Fornt View
	 * Image view where we draw new segments and color in it
	 * it's just a reference layer where we apply color
	 */
	public static ImageView frontView;
	
	/**
	 * A flag that will alter the icon of pencil at
	 * the top of the menu
	 * if true then we'll show user that he can now draw
	 * segments
	 * if false then user cannot draw on image
	 */
	public static boolean draw = false;
	
	/**
	 * those are the bitmaps that will hold down the 
	 * colored images and referenced images
	 */
	public static Bitmap cannyBmp, sendBmp;
	
	/**
	 * Default Replacement color
	 * we set it to white.
	 * User can change it by opening color picker 
	 * and change it by choosing other colors
	 */
	public static int replacementColor = Color.WHITE;
	
	/**
	 * Color that will be applied on the new segments
	 * 
	 */
	public static int targetColor;
	
	/**
	 * Camera Request
	 */
	int ActivityAcquirePicture = 1;
	
	/**
	 * Coordinates where user draw lines
	 */
	int xCord, yCord;
	
	/**
	 * Path of the images that are got by camera or gallery
	 */
	Uri selectedImage, imageUri;
	
	/**
	 * This imageview will show the actual image user
	 * select from the gallery or camera..
	 * we do not change in the image instead we 
	 * change in the front view
	 * Take it as background image
	 */
	ImageView backView;
	
	/**
	 * Default bitmap
	 */
	Bitmap bmp;
	
	/**
	 * Points where user started and ended drawing
	 */
	Point p = new Point();
	
	/**
	 * A userModeDrawing class instance 
	 */
	UserModeDrawing umd;
	
	/**
	 * parent view
	 * It's hold down the current view we are seeing
	 * so basically we get this view to save in our gallery
	 */
	private RelativeLayout userView;
	
	/**
	 * Default Dialog
	 */
	AlertDialog.Builder builder;
	
	/**
	 * A progress Dialog
	 * That is showing user that the currently we are
	 * busy doing some thing so please wait
	 */
	ProgressDialog currentProgress;

	/**
	 * Requests for camera or gallery
	 */
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
		backView = (ImageView) findViewById(R.id.backimageview);
		frontView = (ImageView) findViewById(R.id.frontimageview);
		backView.setScaleType(ScaleType.FIT_XY);
		frontView.setScaleType(ScaleType.FIT_XY);
		umd = (UserModeDrawing) findViewById(R.id.userCanvas);
		userView = (RelativeLayout) findViewById(R.id.userRelativity);
		
		currentProgress = new ProgressDialog( UserMode.this );
		currentProgress.setProgressStyle( ProgressDialog.STYLE_SPINNER );

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case SELECT_PICTURE:
				onButtonPressed(data.getData());

				break;
			case CAMERA_REQUEST:
				onButtonPressed(data.getData());
				break;
			}
		}

	}

	private void onButtonPressed(Uri data) {
		Uri image = data;
		Bitmap finalBmp = null;
		try {
			bmp = Images.Media.getBitmap(getContentResolver(), image);
			if( bmp.getWidth() > bmp.getHeight() ){
				Matrix matrix = new Matrix();
				matrix.postRotate( 90 );
				finalBmp = Bitmap.createBitmap( bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true );
				finalBmp = finalBmp.copy( Config.ARGB_8888, true );
				finalBmp.setHasAlpha( true );
				finalBmp = Bitmap.createScaledBitmap( finalBmp, 720, 1080, true );
			} else {
				finalBmp = bmp.copy( Config.ARGB_8888, true );
				finalBmp.setHasAlpha( true );
				finalBmp = Bitmap.createScaledBitmap( finalBmp, 720, 1080, true );
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		backView.setImageBitmap( finalBmp );
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

	/**
	 * On Back Press We need to move to first activity
	 * where we started
	 * Handling of back press button
	 */
	@Override
	public void onBackPressed() {
		this.finish();
		startActivity(new Intent(this, MainActivity.class));
	}
	
	
	/**
	 * 
	 * @author Farhan Khan
	 * A sub Async Class to save image in gallery
	 * while showing spinner until writing in the gallery
	 * of image is not done
	 */
	class SaveCurrentImage extends AsyncTask<Void, Void, Void>{	
		
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
				// userView.setDrawingCacheEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// Intent get = getIntent();
				// finish();
				// startActivity(get);
			}
			// ProgressBar pb = new ProgressBar(getBaseContext());
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
			currentProgress.setCancelable( false );
			currentProgress.show();
						
		}
	}

}