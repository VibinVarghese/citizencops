package vibinvarghese.citizencops;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vibinvarghese on 14/10/16.
 */

public class CameraFragment extends Fragment {

    String response;

    Camera camera;

    private Camera mCamera = null;
    private CameraView mCameraView = null;

    Camera.PictureCallback rawCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback jpegCallback;

    RelativeLayout parentLayout;

    ProgressBar progressBar;

    LinearLayout row1, row2, row3, pointLayout, trendingCategoriesLayout, imageHolder;
    FrameLayout opaqueScreen;
    ImageView settingsIcon, image1, image2, image3, resetImage1, resetImage2, resetImage3;
    boolean isSetImage1 = false, isSetImage2 = false, isSetImage3 = false;

    FloatingActionButton cameraFab, proceedFab;

    Bitmap imageBitmap1, imageBitmap2, imageBitmap3;

    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    CameraView cameraView;
    int index = 0;

    FrameLayout camera_view;

    Context context;

    ArrayList<String> selectedPrefs = new ArrayList<>();

    boolean doRotate = true;

    public CameraFragment() {
    }

    @SuppressLint("ValidFragment")
    public CameraFragment(String response, Context context) {
        this.response = response;
        this.context = context;
    }

    @SuppressLint("ValidFragment")
    public CameraFragment(int index, Context context, Bitmap imageBitmap1, Bitmap imageBitmap2, Bitmap imageBitmap3) {
        this.context = context;
        this.index = index;
        this.imageBitmap1 = imageBitmap1;
        this.imageBitmap2 = imageBitmap2;
        this.imageBitmap3 = imageBitmap3;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.camera_fragment, container, false);

        parentLayout = (RelativeLayout) rootView.findViewById(R.id.parent_layout);
        row1 = (LinearLayout) rootView.findViewById(R.id.row1);
        row2 = (LinearLayout) rootView.findViewById(R.id.row2);
        row3 = (LinearLayout) rootView.findViewById(R.id.row3);
        imageHolder = (LinearLayout) rootView.findViewById(R.id.image_holder);
        pointLayout = (LinearLayout) rootView.findViewById(R.id.points_layout);
        trendingCategoriesLayout = (LinearLayout) rootView.findViewById(R.id.trending_categories_layout);
        opaqueScreen = (FrameLayout) rootView.findViewById(R.id.opaque_screen);
        image1 = (ImageView) rootView.findViewById(R.id.image1);
        image2 = (ImageView) rootView.findViewById(R.id.image2);
        image3 = (ImageView) rootView.findViewById(R.id.image3);
        resetImage1 = (ImageView) rootView.findViewById(R.id.reset_image1);
        resetImage2 = (ImageView) rootView.findViewById(R.id.reset_image2);
        resetImage3 = (ImageView) rootView.findViewById(R.id.reset_image3);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);

        resetImage1.setOnClickListener(resetImageListener);
        resetImage2.setOnClickListener(resetImageListener);
        resetImage3.setOnClickListener(resetImageListener);

        camera_view = (FrameLayout) rootView.findViewById(R.id.camera_view);

        if (index > 0) {
            if (imageBitmap1 != null && index != 1) {
                image1.setImageBitmap(imageBitmap1);
                image1.setAlpha(255);
                isSetImage1 = true;
            }
            if (imageBitmap2 != null && index != 2) {
                image2.setImageBitmap(imageBitmap2);
                image2.setAlpha(255);
                isSetImage2 = true;
            }
            if (imageBitmap3 != null && index != 3) {
                image3.setImageBitmap(imageBitmap3);
                image3.setAlpha(255);
                isSetImage3 = true;
            }

            launchCameraMode();
        }

        cameraView = new CameraView(context, mCamera);//create a SurfaceView to show camera data

        //if (mCamera != null) {
        mCameraView = cameraView;
        FrameLayout camera_view = (FrameLayout) rootView.findViewById(R.id.camera_view);
        camera_view.addView(mCameraView);//add the SurfaceView to the layout
        //}


        rawCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.d("Log", "onPictureTaken - raw");
            }
        };

        /** Handles data for jpeg picture */
        shutterCallback = new Camera.ShutterCallback() {
            public void onShutter() {
                Log.i("Log", "onShutter'd");
            }
        };
        jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {

                FileOutputStream outStream = null;
                try {
                    String filePath = String.format(
                            "/sdcard/%d.jpg", System.currentTimeMillis());

                    outStream = new FileOutputStream(filePath);
                    outStream.write(data);
                    outStream.close();
                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);

                    File file = new File(filePath);
                    Bitmap thumbnail = BitmapFactory.decodeByteArray(data, 0, data.length);

                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;

                    if (doRotate) {
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                            // Notice that width and height are reversed
                            Bitmap scaled = Bitmap.createScaledBitmap(thumbnail, screenHeight, screenWidth, true);
                            int w = scaled.getWidth();
                            int h = scaled.getHeight();
                            // Setting post rotate to 90
                            Matrix mtx = new Matrix();
                            mtx.postRotate(90);
                            // Rotating Bitmap
                            thumbnail = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                        } else {// LANDSCAPE MODE
                            //No need to reverse width and height
                            Bitmap scaled = Bitmap.createScaledBitmap(thumbnail, screenWidth, screenHeight, true);
                            thumbnail = scaled;
                        }
                    }

                    String path = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), thumbnail, "loadImage", null);

                    ImageView selectedImage = null;
                    if (!isSetImage1)
                        selectedImage = image1;
                    else if (!isSetImage2)
                        selectedImage = image2;
                    else if (!isSetImage3)
                        selectedImage = image3;


                    selectedImage.setImageBitmap(thumbnail);

                    proceedFab.setVisibility(View.VISIBLE);

                    if (selectedImage.equals(image1)) {
                        isSetImage1 = true;
                        resetImage1.setVisibility(View.VISIBLE);
                        imageBitmap1 = thumbnail;
                    }
                    if (selectedImage.equals(image2)) {
                        isSetImage2 = true;
                        resetImage2.setVisibility(View.VISIBLE);
                        imageBitmap2 = thumbnail;
                    }
                    if (selectedImage.equals(image3)) {
                        isSetImage3 = true;
                        resetImage3.setVisibility(View.VISIBLE);
                        imageBitmap3 = thumbnail;
                    }

                    progressBar.setVisibility(View.GONE);
                    cameraFab.setVisibility(View.VISIBLE);

                    if (index > 0) {
                        launchPostFragment();
                        return;
                    }

                    if (isSetImage1 && isSetImage2 && isSetImage3) {
                        launchPostFragment();
                        return;
                    }

                    cameraView.mCamera.stopPreview();
                    cameraView.mCamera.startPreview();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }
                Log.d("Log", "onPictureTaken - jpeg");
            }
        };




        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.registerListener(new SensorEventListener() {
            int orientation = -1;
            ;

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.values[1] < 6.5 && event.values[1] > -6.5) {
                    if (orientation != 1) {
                        Log.d("Sensor", "Landscape");
                        doRotate = false;
                    }
                    orientation = 1;
                } else {
                    if (orientation != 0) {
                        Log.d("Sensor", "Portrait");
                    }
                    doRotate = true;
                    orientation = 0;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // TODO Auto-generated method stub

            }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);

        populateCategories();

        launchCameraMode();

        return rootView;
    }

    View.OnClickListener resetImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.reset_image1) {
                image1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.image_place_holder));
                isSetImage1 = false;
                imageBitmap1 = null;
                v.setVisibility(View.GONE);
            }
            if (v.getId() == R.id.reset_image2) {
                image2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.image_place_holder));
                isSetImage2 = false;
                imageBitmap2 = null;
                v.setVisibility(View.GONE);
            }
            if (v.getId() == R.id.reset_image3) {
                image3.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.image_place_holder));
                isSetImage3 = false;
                imageBitmap3 = null;
                v.setVisibility(View.GONE);
            }
        }
    };

    private void moveViewToScreenCenter(View view) {

        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int statusBarOffset = dm.heightPixels - parentLayout.getMeasuredHeight();

        int originalPos[] = new int[2];
        view.getLocationOnScreen(originalPos);

        int xDest = dm.widthPixels / 2;
        xDest -= (view.getMeasuredWidth() / 2);
        int yDest = dm.heightPixels / 2 - (view.getMeasuredHeight() / 2) - statusBarOffset;

        TranslateAnimation anim = new TranslateAnimation(0, xDest - originalPos[0], 0, yDest - originalPos[1]);
        anim.setDuration(300);
        anim.setFillAfter(true);
        view.startAnimation(anim);
    }


    private void captureImage() {
        // TODO Auto-generated method stub
        mCameraView.mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }


    protected void launchCameraMode() {
        //pointLayout.setVisibility(View.GONE);
        //trendingCategoriesLayout.setVisibility(View.GONE);
        opaqueScreen.setVisibility(View.GONE);
        //settingsIcon.setVisibility(View.GONE);
        //cameraFab.setVisibility(View.VISIBLE);
        imageHolder.setVisibility(View.VISIBLE);

        Animation hideAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_categories);
        trendingCategoriesLayout.startAnimation(hideAnimation);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                trendingCategoriesLayout.setVisibility(View.GONE);

                Animation slidUpAnimationImage1 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_image1);
                image1.setVisibility(View.VISIBLE);
                image1.startAnimation(slidUpAnimationImage1);

                Animation slidUpAnimationImage2 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_image2);
                image2.setVisibility(View.VISIBLE);
                image2.startAnimation(slidUpAnimationImage2);

                Animation slidUpAnimationImage3 = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_image3);
                image3.setVisibility(View.VISIBLE);
                image3.startAnimation(slidUpAnimationImage3);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        hideAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_categories);
        pointLayout.startAnimation(hideAnimation);
        hideAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                pointLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    protected void launchPostFragment() {
        onDetach();
        ((CameraActivity) getActivity()).launchPostFragment(response, imageBitmap1, imageBitmap2, imageBitmap3);
    }

    protected void populateCategories() {

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
