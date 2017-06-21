package vibinvarghese.citizencops;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by vibinvarghese on 14/10/16.
 */

public class CameraActivity extends AppCompatActivity implements LocationListener {

    FrameLayout mainContent;
    ProgressBar progressBar;

    PostFragment postFragment;

    SharedPreferences prefs;

    Location location = null;

    protected LocationManager locationManager;

    Place place;

    final int PLACE_PICKER_REQUEST = 1000;
    private GoogleApiClient mClient;

    boolean isPlaceNameDialog = false;

    // Storage for camera image URI components
    private final static String CAPTURED_PHOTO_PATH_KEY = "mCurrentPhotoPath";
    private final static String CAPTURED_PHOTO_URI_KEY = "mCapturedImageURI";

    // Required for camera operations in order to save the image file on resume.
    private String mCurrentPhotoPath = null;
    private Uri mCapturedImageURI = null;

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mCurrentPhotoPath != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_PATH_KEY, mCurrentPhotoPath);
        }
        if (mCapturedImageURI != null) {
            savedInstanceState.putString(CAPTURED_PHOTO_URI_KEY, mCapturedImageURI.toString());
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_PATH_KEY)) {
            mCurrentPhotoPath = savedInstanceState.getString(CAPTURED_PHOTO_PATH_KEY);
        }
        if (savedInstanceState.containsKey(CAPTURED_PHOTO_URI_KEY)) {
            mCapturedImageURI = Uri.parse(savedInstanceState.getString(CAPTURED_PHOTO_URI_KEY));
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PostFragment postFragment = (PostFragment) getSupportFragmentManager().findFragmentByTag("Post Fragment");
        if (postFragment != null && postFragment.isVisible()) {
            return;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.camera_activity);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        mClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();


        /*Window w = getWindow(); // in Activity's onCreate() for instance
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);*/

        mainContent = (FrameLayout) findViewById(R.id.main_content);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        progressBar.setVisibility(View.GONE);
        launchCameraFragment("");

        launchPostFragment("", null, null, null);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == 456) {
            postFragment.processImageCapture(requestCode, resultCode, data);
        } else if (resultCode == RESULT_OK) {
            if (requestCode == PLACE_PICKER_REQUEST) {
                place = PlacePicker.getPlace(data, this);

                String addressLocationToUpdate = "";
                addressLocationToUpdate += place.getName() + ", " + place.getAddress();

                postFragment.placeDescription.setText(addressLocationToUpdate);

                if (place.getId().contains("+")) {

                    isPlaceNameDialog = true;
                }
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        isPlaceNameDialog = false;
    }

    protected void launchCameraFragment(String response) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CameraFragment cameraFragment = new CameraFragment(response, this);
        fragmentTransaction.replace(R.id.main_content, cameraFragment);
        fragmentTransaction.commit();
    }

    protected void launchCameraFragmentForEditImage(int index, Context context, Bitmap imageBitmap1, Bitmap imageBitmap2, Bitmap imageBitmap3) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        CameraFragment cameraFragment = new CameraFragment(index, context, imageBitmap1, imageBitmap2, imageBitmap3);
        fragmentTransaction.replace(R.id.main_content, cameraFragment);
        fragmentTransaction.commit();
    }

    protected void launchPostFragment(String response, Bitmap image1, Bitmap image2, Bitmap image3) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        postFragment = new PostFragment(response, image1, image2, image3);
        fragmentTransaction.replace(R.id.main_content, postFragment, "Post Fragment");
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        //launchCameraFragment(prefs.getString(Constants.PREFS, ""));
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int grantResult = grantResults[i];

            /*if (permission.equals(android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        //All location services are disabled
                        Util.showSnackBar("Please enable your location/gps", postFragment.image1);
                    } else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                        Location location = Util.getLocation(this);
                    }
                } else {
                    Util.showSnackBar("Please grant location Permissions to use the app", postFragment.image1);
                }
            }*/
        }
    }
}
