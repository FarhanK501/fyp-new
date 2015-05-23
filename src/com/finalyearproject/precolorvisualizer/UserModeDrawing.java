package com.finalyearproject.precolorvisualizer;
import org.opencv.core.Point;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

public class UserModeDrawing extends View {
	public UserModeDrawing(Context context, AttributeSet attrs) {
		super(context, attrs);
		setFocusable(true);
		setFocusableInTouchMode(true);
		DrawPaint = new Paint();
		DrawPaint.setColor(Color.argb(12, 0, 0, 0));
		DrawPaint.setAntiAlias(true);
		DrawPaint.setStrokeWidth(4);

		DrawPaint.setStyle(Style.STROKE);
		this.setDrawingCacheEnabled(true);
	}

	int x, y;
	final int paintColor = Color.BLACK;
	private Paint DrawPaint;
	private Path path = new Path();
	private Bitmap bmp;
	int targetColor;
	int replacementColor = Color.BLUE;
	Canvas c;
	Point p;

	GestureDetector gestures = new GestureDetector(getContext(),
			new OnGestureListener() {

				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					// TODO Auto-generated method stub
					return true;
				}

				@Override
				public void onShowPress(MotionEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public boolean onScroll(MotionEvent e1, MotionEvent e2,
						float distanceX, float distanceY) {
					// TODO Auto-generated method stub
					return true;
				}

				@Override
				public void onLongPress(MotionEvent e) {

					p = new Point();
					p.x = e.getX();
					p.y = e.getY();
					x = (int) p.x;
					y = (int) p.y;
					
					
					bmp = getDrawingCache();
					 
					//bmp = getDrawingCache(true);
					c = new Canvas(bmp);
					
					targetColor = bmp.getPixel(x, y);
				
					replacementColor = UserMode.replacementColor;
					FloodFill fl = new FloodFill();
					fl.floodFill(bmp, p, targetColor, replacementColor);
					Bitmap holdLastOne = bmp;
					UserMode.frontView.setImageBitmap(holdLastOne);
					

				}

				@Override
				public boolean onFling(MotionEvent e1, MotionEvent e2,
						float velocityX, float velocityY) {
					// TODO Auto-generated method stub
					return true;
				}

				@Override
				public boolean onDown(MotionEvent e) {

					return true;
				}
			});

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawPath(path, DrawPaint);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float touchX = event.getX();
		float touchY = event.getY();
		if (UserMode.draw) {
			switch (event.getAction()) {

			case MotionEvent.ACTION_DOWN:
				path.moveTo(touchX, touchY);
				invalidate();
				break;

			case MotionEvent.ACTION_MOVE:
				path.lineTo(touchX, touchY);

				invalidate();
				break;

			default:

				return false;
			}

			invalidate();
		}
		// return true;
		return gestures.onTouchEvent(event);

	}
}
