package com.finalyearproject.precolorvisualizer;

import org.opencv.android.OpenCVLoader;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {
	int ActivityAcquirePicture = 1;
	int count = 0;
	AlertDialog.Builder builder;
	private static final String TAG = "TAG";

	// Load opencv Library
	static {
		if (!OpenCVLoader.initDebug()) {
			// Handle initialization error
			// TODO handler code
			System.out.println("Error occurred while loading opencv library");
		} else {
			System.out.println("Opencv library Loaded successfully");
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		builder = new AlertDialog.Builder(this);
		builder.setMessage("Choose Mode")
				.setCancelable(false)
				.setPositiveButton("User Mode",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent userIntent = new Intent(
										MainActivity.this, UserMode.class);
								finish();
								startActivity(userIntent);
							}
						})
				.setNeutralButton("Exit", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
						Intent exitIntent = new Intent();
						exitIntent.setAction(Intent.ACTION_MAIN);
						exitIntent.addCategory(Intent.CATEGORY_HOME);
						exitIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						finish();
						startActivity(exitIntent);

					}
				})
				.setNegativeButton("Auto Mode",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent autoMode = new Intent(MainActivity.this,
										AutoMode.class);
								finish();
								startActivity(autoMode);
							}
						});
		AlertDialog alert = builder.create();

		alert.show();
//		 Intent i = new Intent(MainActivity.this, roughWork.class);
//		 		startActivity(i);
//	
		}

}