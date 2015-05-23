package com.finalyearproject.precolorvisualizer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;

/**
 * 
 * @author Farhan Khan
 * Final Year Project
 *
 */
public interface PreColor {

	/*
	 * @param bmp
	 * @return bmp
	 * It's a method to clear the view..
	 * Will clear Edgeds
	 */
	public Bitmap ClearFrontView( Bitmap bmp ); 
	
	/*
	 * 
	 * @param bmp
	 * @return bmp
	 * This Method will get the image in Bitmap
	 * Format and will return the Binary Image
	 */
	public Bitmap ApplyCanny( Bitmap bmp);
	
	/*
	 * This Method will apply Flood Fill
	 * Algo on the Bitmap which will
	 * Fill the color on desired Location
	 * Of the Bitmap
	 */
	public Bitmap ApplyFloodFill ( Bitmap bmp );
	
	/*
	 * Showing A color Picker Dialog
	 * With Alpha Channel with Slides
	 */
	public void ShowColorPickerDialog( Context ctx);
	
	/*
	 * Camera Intent
	 * Related to call Native Camera
	 */
	public void CallCamera();
	
	/*
	 * Gallery Intent
	 * Appear Gallery so user can choose Images
	 */
	public void CallGallery();
	
	/**
	 * 
	 * @param view
	 * Save the current View
	 * Deal as Image
	 * and save it in as .png Format
	 */
	public void SaveImage( View v ); 
}
