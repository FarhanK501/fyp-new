package com.finalyearproject.precolorvisualizer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

public class Segmentation extends AsyncTask<Void, Void, Boolean>{
	private ProgressDialog p;
	private File destination;
	private String filename;
	private Bitmap encodedBitmap;
	private Context context;
	

	public Segmentation(File destination,String filename,Bitmap encodedBitmap,Context context){

	    this.destination = destination;
	    this.filename = filename;
	    this.encodedBitmap = encodedBitmap;
	    this.context = context;
	    this.p = new ProgressDialog(context);


	}

	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    p.setMessage("Saving image to SD Card");
	    p.setIndeterminate(false);
	    p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    p.setCancelable(false);
	    p.show();

	}

	@Override
	protected Boolean doInBackground(Void... voids) {



	    try {
	        FileOutputStream out = new FileOutputStream(destination);
	        encodedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);


	        out.flush();
	        out.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	@Override
	protected void onPostExecute(Boolean aBoolean) {
	    super.onPostExecute(aBoolean);
	    p.dismiss();
	    if(aBoolean)
	    {
	        Toast.makeText(context, "Download complete", Toast.LENGTH_SHORT).show();
	    }else{
	        Toast.makeText(context,"Failed to save image",Toast.LENGTH_SHORT).show();
	    }
	}
}
