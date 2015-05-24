package com.finalyearproject.precolorvisualizer;

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

public class ShowColorPickerDialogClass {
	Context currentContext;

	public ShowColorPickerDialogClass(Context ctx) {
		currentContext = ctx;
	}

	int replacemantColor = Color.BLACK;

	protected void ColorPicker() {
		AmbilWarnaDialog dg = new AmbilWarnaDialog(currentContext, Color.BLACK,
				true, new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int colors) {
						setReplacemantColor( colors );

					}
					@Override
					public void onCancel(AmbilWarnaDialog dialog) {
						Toast.makeText( currentContext ,
								"You did not choose any Color",
								Toast.LENGTH_SHORT).show();

					}
				});

		dg.show();
	}

	public int getReplacemantColor() {
		return replacemantColor;
	}

	public void setReplacemantColor(int replacemantColor) {
		this.replacemantColor = replacemantColor;
	}
}
