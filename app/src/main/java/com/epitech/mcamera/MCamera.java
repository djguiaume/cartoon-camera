package com.epitech.mcamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.location.Location;
import android.media.ExifInterface;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.RelativeLayout;

public class MCamera {
	private static String TAG = "MCamera";
	private static String mSaveDir = "MyCameraApp";
	private Camera mCamera = null;
	private Location location = null;
	private MediaRecorder mMediaRecorder;
	private boolean mIsRecording = false;
	private Context mContext = null;
	public InYourFaceListen faces;
	private OnSharedPreferenceChangeListener listener = null;
	private SharedPreferences prefs = null;

	public MCamera() {

	}

	public void setLocation(Location loc) {
		location = loc;
	}

	public boolean init(Context context, RelativeLayout ly, int CameraId) {
		mContext = context;

		mContext = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences prefs,
					String key) {
				UpdatePref(mContext);
			}
		};
		prefs.registerOnSharedPreferenceChangeListener(listener);

		if (mCamera != null) {
			Log.d(TAG, "already init.");
			return false;
		}
		if (checkCameraHardware(context) == false)
			return false;
		mCamera = getCameraInstance(CameraId);

		if (ly == null) {
			Log.d(MySurfaceView.VTAG,
					"Main Layout is null impossible to draw face");
		}

		if (prefs.getBoolean("face_switch", false)) {
			Log.d(MySurfaceView.VTAG, "FACE DETECT Enable");
			mCamera.stopFaceDetection();
			mCamera.setFaceDetectionListener(new InYourFaceListen(
					this.mContext, ly));
			try {
				mCamera.startFaceDetection();
			} catch (IllegalArgumentException i) {
				Log.d(MySurfaceView.VTAG, "FACE DETECT Probably no supported");
			}
			if (mCamera.getParameters().getMaxNumDetectedFaces() <= 0) {
				Log.d(MySurfaceView.VTAG,
						"YEP FACE DETECT NOT SUPPORTED ON YOUR DEVICE");
				mCamera.stopFaceDetection();
				SharedPreferences.Editor edd = prefs.edit();
				edd.remove("face_switch");
				edd.apply();
			}
		} else {
			Log.d(MySurfaceView.VTAG, "FACE DETECT disable in settings");
		}

		if (mCamera == null)
			return false;
		setTorchLight();
		return true;
	}

	public void destroy() {
		Log.d(TAG, "destroy called.");

		stopVideoRecording();
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private void releaseMediaRecorder() {
		if (mMediaRecorder == null)
			return;
		mMediaRecorder.reset();
		mMediaRecorder.release();
		mMediaRecorder = null;
		mCamera.lock();
	}

	public Camera getCamera() {
		if (mCamera == null)
			Log.w(TAG, "getCamera called without init.");
		return mCamera;
	}

	private void SetExifGPSData(File fn) {
		if (location == null)
			return;
		ExifInterface exif;
		double glat = location.getLatitude();
		double glong = location.getLongitude();

		Log.d(MySurfaceView.VTAG, "setting exif data lat : " + glat
				+ " long : " + glong);

		int num1Lat = (int) Math.floor(glat);
		int num2Lat = (int) Math.floor((glat - num1Lat) * 60);
		double num3Lat = (glat - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;

		int num1Lon = (int) Math.floor(glong);
		int num2Lon = (int) Math.floor((glong - num1Lon) * 60);
		double num3Lon = (glong - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;

		try {
			exif = new ExifInterface(fn.getAbsolutePath());
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, num1Lat + "/1,"
					+ num2Lat + "/1," + num3Lat + "/1000");
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, num1Lon + "/1,"
					+ num2Lon + "/1," + num3Lon + "/1000");
			exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF,
					(glat > 0) ? "N" : "S");
			exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF,
					(glong > 0) ? "E" : "W");
			exif.saveAttributes();
		} catch (IOException e) {
			Log.d(MySurfaceView.VTAG, "Error set exif");
		}

	}

	public static Camera getCameraInstance(int id) {

		Camera c = null;
		try {
			c = Camera.open(id); // attempt to get a Camera instance
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		return c;
	}

	private static File getOutputMediaFile(int type) throws Exception {

		if (!(Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())))
			throw new Exception("SDCard not properly mounted.");

		File mediaStorageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				mSaveDir);

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs())
				throw new Exception("failed to create directory " + mSaveDir);
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile;
		if (type == FileColumns.MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "IMG_" + timeStamp + ".jpg");
		} else if (type == FileColumns.MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator
					+ "VID_" + timeStamp + ".mp4");
		} else {
			throw new Exception("Unknown media file type");
		}

		return mediaFile;
	}

	private boolean checkCameraHardware(Context context) {
		Log.d(TAG, "CheckCameraHardware mPicture=" + mPicture + "mCamera="
				+ mCamera);
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA))
			return true;
		else
			return false;
	}

	public void takePicture() {
		TakePictureTask takePictureTask = new TakePictureTask();
		takePictureTask.execute();
	}

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			Log.d(TAG, "onPictureTaken mPicture=" + mPicture + "mCamera="
					+ mCamera);
			File pictureFile = null;
			try {
				pictureFile = getOutputMediaFile(FileColumns.MEDIA_TYPE_IMAGE);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(TAG, e.getMessage());
			}
			if (pictureFile == null) {
				Log.d(TAG,
						"Error creating media file, check storage permissions.");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
                // convert byte array to bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                //apply effects
                //bitmap = PictureEffect.applyMeanRemoval(bitmap);
                bitmap = PictureEffect.sharpen(bitmap, 30);
                bitmap = PictureEffect.doColorFilter(bitmap, 70, 70, 70);
				Log.d(TAG, "fos=" + fos);
				Log.d(TAG, "data=" + data);
				//fos.write(data);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
				fos.close();
				SetExifGPSData(pictureFile);
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}

			ContentValues image = new ContentValues();

			image.put(Images.Media.TITLE, pictureFile.getName());
			image.put(Images.Media.DISPLAY_NAME, pictureFile.getName());
			image.put(Images.Media.MIME_TYPE, "image/jpg");
			if (location != null) {
				image.put(Images.Media.LATITUDE, location.getLatitude());
				image.put(Images.Media.LONGITUDE, location.getLongitude());
			}
			image.put(Images.Media.ORIENTATION, 0);

			File parent = pictureFile.getParentFile();
			String path = parent.toString().toLowerCase();
			String name = parent.getName().toLowerCase();
			image.put(Images.ImageColumns.BUCKET_ID, path.hashCode());
			image.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, name);
			image.put(Images.Media.SIZE, pictureFile.length());

			image.put(Images.Media.DATA, pictureFile.getAbsolutePath());

			mContext.getContentResolver().insert(
					MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
		}
	};

	private class TakePictureTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {
			mCamera.startPreview();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mCamera.takePicture(null, null, mPicture);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private class TakeVideoTask extends AsyncTask<SurfaceHolder, Void, Boolean> {

		@Override
		protected void onPostExecute(Boolean recording) {
			if (recording)
				return;
			releaseMediaRecorder();
			mIsRecording = false;
		}

		@Override
		protected Boolean doInBackground(SurfaceHolder... holder) {
			if (prepareVideoRecorder(holder[0])) {
				try {
					mMediaRecorder.start();
					mIsRecording = true;
				} catch (IllegalStateException e) {
					Log.d(TAG, "IllegalStateException starting MediaRecorder: "
							+ e.getMessage());
					return false;
				}
			} else
				return false;
			return true;
		}
	}

	private boolean prepareVideoRecorder(SurfaceHolder holder) {
		Log.d("VIDEO", "mCamera =" + mCamera);
		if (mCamera == null)
			return false;
		int quality = CamcorderProfile.QUALITY_HIGH;
		mMediaRecorder = new MediaRecorder();
		mCamera.unlock();
		mMediaRecorder.setCamera(mCamera);

		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		boolean format = sharedPref.getBoolean("switch_size", true);
		Log.e("FORMAT", "format = " + format);

		if (format == false)
			quality = CamcorderProfile.QUALITY_LOW;

		mMediaRecorder.setProfile(CamcorderProfile.get(quality));

		try {
			mMediaRecorder.setOutputFile(getOutputMediaFile(
					FileColumns.MEDIA_TYPE_VIDEO).toString());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}

		// TODO: Do in MySurfaceView?
		mMediaRecorder.setPreviewDisplay(holder.getSurface());

		try {
			mMediaRecorder.prepare();
		} catch (IllegalStateException e) {
			Log.d(TAG,
					"IllegalStateException preparing MediaRecorder: "
							+ e.getMessage());
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		return true;
	}

	public boolean startVideoRecording(SurfaceHolder holder) {
		if (mIsRecording) {
			Log.w(TAG, "Video already recording!");
			return true;
		}
		new TakeVideoTask().execute(holder);
		return mIsRecording;
	}

	public void stopVideoRecording() {
		if (!mIsRecording)
			return;
		mMediaRecorder.stop();
		releaseMediaRecorder();
		mCamera.lock();
		mIsRecording = false;
	}

	public boolean isRecording() {
		return mIsRecording;
	}

	public void setTorchLight() {
		if (mCamera != null) {
			Parameters p = mCamera.getParameters();
            if (p.getFlashMode() == null) {
                return;
            }
            List<String> supportedFlashModes = p.getSupportedFlashModes();
            if (supportedFlashModes == null || supportedFlashModes.isEmpty()
                    || supportedFlashModes.size() == 1
                    && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
                return;
            }
			if (prefs.getBoolean("flash_switch", true)) {
				p.setFlashMode(Parameters.FLASH_MODE_ON);
				mCamera.setParameters(p);
			} else {
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
				mCamera.setParameters(p);
			}
		} else
			Log.d(TAG, "mCamera pas la ");
	}

	public void UpdatePref(Context context) {
		// get latest settings from the xml config file
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(context);

	}
}
