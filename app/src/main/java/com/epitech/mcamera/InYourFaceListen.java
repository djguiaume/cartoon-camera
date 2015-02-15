package com.epitech.mcamera;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.widget.RelativeLayout;

/**
 * Created by vayan on 4/18/14.
 */
public class InYourFaceListen implements Camera.FaceDetectionListener {
    Context ctx;
    MCamera cam;
    RelativeLayout ly;

    public InYourFaceListen(Context context, RelativeLayout relativeLayout) {
       ctx = context;
       ly = relativeLayout;
       Log.d(MySurfaceView.VTAG, "Face Listener created");
        if (ly == null) {
            Log.d(MySurfaceView.VTAG, "Face Listener got a null layout");
        }
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        ly.removeAllViews();
        Log.d(MySurfaceView.VTAG, "onFaceDetection called");
        if (faces.length > 0) {
            Log.d(MySurfaceView.VTAG, "detect at least one face");
            OverlayView ov = new OverlayView(ctx, faces, 1920, 1080);
            ov.setFaces(faces);
            ly.addView(ov);
        }
    }
}
