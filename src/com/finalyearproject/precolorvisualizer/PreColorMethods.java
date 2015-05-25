package com.finalyearproject.precolorvisualizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

public class PreColorMethods implements PreColor {
	
	@Override
	public Bitmap ClearFrontView(Bitmap bmp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bitmap ApplyCanny(Bitmap bmp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bitmap ApplyFloodFill(Bitmap bmp) {
		Bitmap currentBm = bmp;
		
		return null;
	}

	@Override
	public void ShowColorPickerDialog( Context ctx ) {
		ShowColorPickerDialogClass colorPicker = new
				ShowColorPickerDialogClass( ctx );
	}

	@Override
	public void CallCamera() {	
		CallCameraOrGalleryIntent camOrGal = new CallCameraOrGalleryIntent();
		camOrGal.callTheCam();
	}

	@Override
	public void CallGallery() {
		CallCameraOrGalleryIntent camOrGal = new CallCameraOrGalleryIntent();
		camOrGal.callGallery();
	}

	@Override
	public void SaveImage(View v) {
		// TODO Auto-generated method stub
		
	}

}
