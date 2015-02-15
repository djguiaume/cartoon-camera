package com.epitech.mcamera;

import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.epitech.mcamera.MainActivity.PlaceholderFragment.plugin;

public class ZoomPlugin implements plugin {
	MySurfaceView mPreview;
	View mRootView;
	private boolean isSmoothZoomAvalaible = false;
	private String TAG = "ZoomPlugin";
	private int maxZoom;
	private int zoomStepNumber = 4;
	private int zoomStepValue;
	Camera.Parameters params;

	@Override
	public void askFeature(MySurfaceView preview, View rootview) {
		mPreview = preview;
		mRootView = rootview;

		Log.v(TAG, "Checks feature");
		if (mPreview.hasFeature(MySurfaceView.ZOOM_FEATURE_NAME)) {
			Log.v(TAG, "On a la feature");
			setFeatureControls(mRootView);
		} else {
			Log.v(TAG, "On a pas la feature");
		}
	}

	private void setFeatureControls(View rootView) {
		Button zoomPlus = (Button) rootView.findViewById(R.id.button_zoom_plus);
		Button zoomMinus = (Button) rootView
				.findViewById(R.id.button_zoom_minus);
		zoomMinus.setOnClickListener(new OnZoomButtonPushedListerner());
		zoomPlus.setOnClickListener(new OnZoomButtonPushedListerner());
		zoomMinus.setVisibility(View.VISIBLE);
		zoomPlus.setVisibility(View.VISIBLE);
		if (mPreview.hasFeature(MySurfaceView.SMOOTHZOOM_FEATURE_NAME)) {
			isSmoothZoomAvalaible = true;
		} else {
			isSmoothZoomAvalaible = false;
		}
		Log.d(TAG, "chatte smooth zoom: " + isSmoothZoomAvalaible);

		params = mPreview.getCamera().getParameters();
		maxZoom = params.getMaxZoom();
		Log.v(TAG, "Smooth zoom Max = " + maxZoom);
		zoomStepValue = maxZoom / zoomStepNumber;

	}

	private void zoom(int zoomTo) {
		if (isSmoothZoomAvalaible) {
			mPreview.getCamera().stopSmoothZoom();
			mPreview.getCamera().startSmoothZoom(zoomTo);
			Log.d(TAG, "smoothzoom");
		} else {
			Log.d(TAG, "Passmoothzoom");
			params.setZoom(zoomTo);
		}
		mPreview.getCamera().setParameters(params);
	}

	public class OnZoomButtonPushedListerner implements OnClickListener {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "un bouton de zoom est clique");
			if (isSmoothZoomAvalaible) {
				mPreview.getCamera().stopSmoothZoom();
			}
			int currentZoom = params.getZoom();
			int newZoom = 0;
			switch (v.getId()) {
			case R.id.button_zoom_plus:
				newZoom = currentZoom + zoomStepValue;
				newZoom = maxZoom < newZoom ? maxZoom : newZoom;
				
				break;
			case R.id.button_zoom_minus:
				newZoom = currentZoom - zoomStepValue;
				newZoom = newZoom < 0 ? 0 : newZoom;
			
				break;
			}
			Log.d(TAG, "newZoom:" + newZoom + " current: " + currentZoom);
			if (newZoom != currentZoom) {
				zoom(newZoom);
			}
		}
	}

}