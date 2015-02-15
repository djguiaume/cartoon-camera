package com.epitech.mcamera;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.hardware.Camera.Parameters;

import com.epitech.mcamera.ZoomPlugin;
import com.epitech.mcamera.R;

public class MainActivity extends Activity {

	private OnSharedPreferenceChangeListener listener = null;
	private SharedPreferences prefs = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);

		UpdatePref();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs,
					String key) {
				UpdatePref();
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(listener);

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.settings) {
			showUserSettings();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showUserSettings() {
		startActivity(new Intent(MainActivity.this, UserSettingsActivity.class));
	}
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		//super.onConfigurationChanged(newConfig);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private MySurfaceView mPreview = null;
		private boolean mIsRecording = false;
		private View rootView;
		private static String TAG = "PlaceholderFragment";

		List<plugin> plugins;

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			Log.d(TAG, "ON CREATE VIEW");

			Button photoButton = (Button) rootView
					.findViewById(R.id.button_photo);
			photoButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					// get an image from the camera
					Log.d(ACCOUNT_SERVICE, "onClick");
					mPreview.takePicture();
				}
			});

			ImageButton videoButton = (ImageButton) rootView
					.findViewById(R.id.button_video);
			videoButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(ACCOUNT_SERVICE, "onClick VIDEO");
					mPreview.takeVideo();

					ImageButton vButton = (ImageButton) v;
					Button pButton = (Button) rootView
							.findViewById(R.id.button_photo);
					// too slow to get updated
					//mIsRecording = mPreview.isRecording();
					mIsRecording = !mIsRecording;
					if (mIsRecording) {
						vButton.setImageResource(R.drawable.record_stop);
						pButton.setEnabled(false);
					}
					else {
						vButton.setImageResource(R.drawable.record_record);
						pButton.setEnabled(true);
					}
				}
			});

            ImageButton changeCameraButton = (ImageButton) rootView
                    .findViewById(R.id.change_camera);
            changeCameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(ACCOUNT_SERVICE, "onClick change Camera");
                    ImageButton vButton = (ImageButton) v;
                    Button pButton = (Button) rootView
                            .findViewById(R.id.button_photo);
                    mPreview.changeCamera();
                }
            });

			Button aeButton = (Button) rootView.findViewById(R.id.button_ae);
			aeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Button aeButton = (Button) v.findViewById(R.id.button_ae);
					// get an image from the camera
					Log.d(ACCOUNT_SERVICE, "onClick");
					Parameters p = mPreview.getCamera().getParameters();
					if (aeButton.getText() == "AE off") {
						aeButton.setText("AE on");
						p.setAutoExposureLock(true);
					} else {
						aeButton.setText("AE off");
						p.setAutoExposureLock(false);
					}
					mPreview.getCamera().setParameters(p);
				}
			});
			return rootView;
		}

		@Override
		public void onResume() {
			Log.d(TAG, "ON RESUME VIEW");
			super.onResume();

			mPreview = new MySurfaceView(getActivity(),
					(RelativeLayout) rootView.findViewById(R.id.relavmain));

			FrameLayout preview = (FrameLayout) rootView
					.findViewById(R.id.camera_preview);
			preview.addView(mPreview);
			mPreview.startPreview();

			plugins = new ArrayList<MainActivity.PlaceholderFragment.plugin>();
			plugins.add(new ZoomPlugin());

			for (int i = 0; i < plugins.size(); ++i) {
				plugins.get(i).askFeature(mPreview, rootView);
			}

		}

		@Override
		public void onPause() {
			super.onPause();
			Log.d(TAG, "ONPAUSE");

			FrameLayout preview = (FrameLayout) rootView
					.findViewById(R.id.camera_preview);
			preview.removeView(mPreview);
			mPreview.destroyPreview();
			mPreview = null;
			plugins.clear();
			plugins = null;
		}

		public interface plugin {
			public void askFeature(MySurfaceView preview, View rootview);
		}

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            Log.d(TAG, "ORIENTATION = " + newConfig.orientation);
            if (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                mPreview.getCamera().setDisplayOrientation(90);
            } else {
                mPreview.getCamera().setDisplayOrientation(0);
            }
            //super.onConfigurationChanged(newConfig);
        }

	}

	public void UpdatePref() {
		// get latest settings from the xml config file
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this);
	}
}
