package vibinvarghese.citizencops;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.List;

/**
 * Created by vibinvarghese on 18/10/16.
 */

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private SurfaceHolder mHolder;
    public Camera mCamera;
    Context mContext;

    Camera.Size optimalSize;

    public CameraView(Context context, Camera camera) {
        super(context);
        this.mContext = context;
        mCamera = camera;
        //get the holder and set this class as the callback, so we can get camera data here
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e) {
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }

        mCamera.setDisplayOrientation(90);

        try {
            //when the surface is created, we can set the camera to draw images in this surfaceholder
            mCamera.setPreviewDisplay(surfaceHolder);
            Camera.Parameters params = mCamera.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();

            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
            List<Camera.Size> sizes = params.getSupportedPreviewSizes();
            optimalSize = sizes.get(0);

            mCamera.setParameters(params);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceCreated " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        mCamera.startPreview();
        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        if (mHolder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            //this will happen when you are trying the camera if it's not running
        }

        cameraSetup(i2, i3);

        //now, recreate the camera preview
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("ERROR", "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        mCamera.stopPreview();
        //mCamera.release();

        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void cameraSetup(int w, int h) {
        // set the camera parameters, including the preview size

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
        double cameraAspectRatio = ((double) optimalSize.width) / optimalSize.height;

        if (((double) h) / w > cameraAspectRatio) {
            lp.width = (int) (h / cameraAspectRatio + 0.5);
            lp.height = h;
        } else {
            lp.height = (int) (w * cameraAspectRatio + 0.5);
            lp.width = w;
            lp.topMargin = (h - lp.height) / 2;
        }
        lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

        setLayoutParams(lp);
        requestLayout();
    }
}