package com.epitech.mcamera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by vayan on 4/18/14.
 */
public class OverlayView extends View {

	private Camera.Face[] faces;
	public static final float COORDINATE_NORMALIZE = 2000;
	public static final int ZERO_NORMALIZE = 1000;
	private int width;
	private int height;

	public OverlayView(Context context, Camera.Face[] f, int width, int height) {
		super(context);
		setWillNotDraw(false);
		faces = f;
		this.width = width;
		this.height = height;
	}

	public OverlayView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setFaces(Camera.Face[] f) {

		Log.d(MySurfaceView.VTAG, "Receive new faces");
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// handle orientation and stuff

		Paint mPaint = new Paint();
		mPaint.setColor(Color.GREEN);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(3);
		Log.d(MySurfaceView.VTAG, "onDraw : Draw overlay");
		// canvas.drawRect(1,1,40,40, mPaint);
		if (faces != null) {
			for (Camera.Face face : faces) {
				Log.d(MySurfaceView.VTAG, face.toString());
				if (face != null && face.score > 90) {
					canvas.drawRect(
							new Rect((face.rect.left + ZERO_NORMALIZE),
									(face.rect.top + ZERO_NORMALIZE)
											- face.rect.height(),
									(face.rect.right + ZERO_NORMALIZE),
									(face.rect.bottom + ZERO_NORMALIZE)
											- face.rect.height()), mPaint);
				}
			}
		}
		canvas.save();
	}

}
