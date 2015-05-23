package com.finalyearproject.precolorvisualizer;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;

public class CallCameraOrGalleryIntent extends Activity {
	Intent currentIntent;

	private static final int CAMERA_INTENT = 101;
	private static final int GALLERY_INTENT = 102;

	public void callTheCam() {
		Intent camIntent = new Intent("android.media.action.IMAGE_CAPTURE");
		startActivityForResult(camIntent, CAMERA_INTENT);
	}

	public void callGallery() {
		Intent galleryIntent = new Intent();
		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		startActivityForResult(galleryIntent, GALLERY_INTENT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CAMERA_INTENT:
				Bitmap currentCameraImage = getImageFromCamOrGal(data);
				setCameraImage(currentCameraImage);
				break;

			case GALLERY_INTENT:
				Bitmap currentGalleryImage = getImageFromCamOrGal(data);
				setGalleryImage(currentGalleryImage);
				break;

			default:
				break;
			}
		}
	}

	/**
	 * 
	 * @param data
	 * @return Bitmap Will return Image upon calling from Camera or Gallery
	 *         Intents
	 */

	private Bitmap getImageFromCamOrGal(Intent data) {
		Bitmap currentImage = null;
		Uri imagePath = data.getData();
		try {
			currentImage = Images.Media.getBitmap(getContentResolver(),
					imagePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return currentImage;
	}

	// Setter Getter For Images
	// That are Received From one
	// of the intents above
	// i.e Camera/Gallery

	// Camera Image
	private Bitmap cameraImage;

	// Gallery Image
	private Bitmap galleryImage;

	public Bitmap getCameraImage() {
		return cameraImage;
	}

	public void setCameraImage(Bitmap cameraImage) {
		this.cameraImage = cameraImage;
	}

	public Bitmap getGalleryImage() {
		return galleryImage;
	}

	public void setGalleryImage(Bitmap galleryImage) {
		this.galleryImage = galleryImage;
	}

}
