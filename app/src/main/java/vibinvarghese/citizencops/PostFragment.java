package vibinvarghese.citizencops;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by vibinvarghese on 15/10/16.
 */

public class PostFragment extends Fragment {

    String response;
    LinearLayout pickLocationHolder;
    Bitmap imageBitmap1, imageBitmap2, imageBitmap3;
    ImageView image1, image2, image3, resetImage1, resetImage2, resetImage3, imageEdit1, imageEdit2, imageEdit3;
    SharedPreferences prefs;
    TextView placeDescription;
    String[] linkedImages = new String[3];
    int currentImage = 1;
    ProgressBar progressBar;
    EditText content, title;
    Button postButton;
    LinearLayout mainView;
    Spinner categories, bizList;
    int selectedCategory = 0;
    RadioGroup postType;

    final int CAMERA_INTENT_CODE = 456;

    int currentImageSelected = 0;

    ArrayList<String> selectedCategories = new ArrayList<>();
    ArrayList<String> bizIDList;
    ArrayList<String> bizNameList;
    ArrayList<String> shopNameList;
    ArrayList<String> shopAreaList;

    private Uri mImageUri;

    PostFragment context;

    ArrayList<String> selectedPrefs = new ArrayList<>();

    public PostFragment() {
    }

    @SuppressLint("ValidFragment")
    public PostFragment(String response, Bitmap imageBitmap1, Bitmap imageBitmap2, Bitmap imageBitmap3) {
        this.response = response;
        this.imageBitmap1 = imageBitmap1;
        this.imageBitmap2 = imageBitmap2;
        this.imageBitmap3 = imageBitmap3;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.post_fragment, container, false);

        context = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        pickLocationHolder = (LinearLayout) rootView.findViewById(R.id.pick_location_holder);

        image1 = (ImageView) rootView.findViewById(R.id.image1);
        image2 = (ImageView) rootView.findViewById(R.id.image2);
        image3 = (ImageView) rootView.findViewById(R.id.image3);

        postType = (RadioGroup) rootView.findViewById(R.id.offer_type);

        categories = (Spinner) rootView.findViewById(R.id.categories);
        bizList = (Spinner) rootView.findViewById(R.id.shop_dropdown);

        ArrayList<String> categoryList = new ArrayList<>();
        categoryList.add("Men");
        categoryList.add("Women");
        categoryList.add("Consumer Electronics");
        categoryList.add("Home Appliances");
        categoryList.add("Home & Furnitures");
        //categoryList.add("Jewellery");
        //categoryList.add("Books & More");
        categoryList.add("Baby & Kids");
        //categoryList.add("Sports & Fitness");
        //categoryList.add("Automobile & Accessories");

        ArrayAdapter shopAreaAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, categoryList);
        shopAreaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categories.setAdapter(shopAreaAdapter);
        shopAreaAdapter.notifyDataSetChanged();

        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        mainView = (LinearLayout) rootView.findViewById(R.id.parent);

        resetImage1 = (ImageView) rootView.findViewById(R.id.reset_image1);
        resetImage2 = (ImageView) rootView.findViewById(R.id.reset_image2);
        resetImage3 = (ImageView) rootView.findViewById(R.id.reset_image3);

        FrameLayout imageEdit1 = (FrameLayout) rootView.findViewById(R.id.click_1);
        FrameLayout imageEdit2 = (FrameLayout) rootView.findViewById(R.id.click_2);
        FrameLayout imageEdit3 = (FrameLayout) rootView.findViewById(R.id.click_3);

        placeDescription = (TextView) rootView.findViewById(R.id.place_description);
        title = (EditText) rootView.findViewById(R.id.post_title);

        imageEdit1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentImageSelected = 1;
                launchCameraIntent();

            }
        });
        imageEdit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentImageSelected = 2;
                launchCameraIntent();
            }
        });
        imageEdit3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentImageSelected = 3;
                launchCameraIntent();
            }
        });

        resetImage1.setOnClickListener(resetImageListener);
        resetImage2.setOnClickListener(resetImageListener);
        resetImage3.setOnClickListener(resetImageListener);

        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        postButton = (Button) rootView.findViewById(R.id.button_action);
        content = (EditText) rootView.findViewById(R.id.content);


        if (imageBitmap1 != null) {
            image1.setImageBitmap(imageBitmap1);
            resetImage1.setVisibility(View.VISIBLE);
        }

        if (imageBitmap2 != null) {
            image2.setImageBitmap(imageBitmap2);
            resetImage2.setVisibility(View.VISIBLE);
        }

        if (imageBitmap3 != null) {
            image3.setImageBitmap(imageBitmap3);
            resetImage3.setVisibility(View.VISIBLE);
        }

        pickLocationHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    getActivity().startActivityForResult(builder.build(getActivity()), ((CameraActivity) getActivity()).PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Your report has been submitted successfully", Toast.LENGTH_LONG).show();

                Intent camera = new Intent(getActivity(), CameraActivity.class);
                context.startActivity(camera);
            }
        });

        populateCategories();

        makeBizSelectRequest();

        ImageView refresh = (ImageView) rootView.findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeBizSelectRequest();
            }
        });


        return rootView;
    }

    protected void makeBizSelectRequest() {
        /*JSONObject postDataParams = new JSONObject();

        Location location = Util.getLocation(getActivity());

        try {
            postDataParams.put("gpsLatitude", location.getLatitude());
            postDataParams.put("gpsLongitude", location.getLongitude());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PerformAsyncTask performAsyncTask = new PerformAsyncTask(getActivity(), "BizInfosByLocation",
                Constants.SELECT_BIZ_INFOS_BY_LOCATION, postDataParams, "POST");

        performAsyncTask.execute();*/

        /*PerformAsyncTask performAsyncTask = new PerformAsyncTask(getActivity(), "BizInfos",
                Constants.SELECT_BIZ_INFOS, null, "GET");

        performAsyncTask.execute();*/

    }

    protected void launchCameraIntent() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        mImageUri = null;
        intent.setClipData(ClipData.newRawUri(null, mImageUri));
        File photo = null;
        try {
            // place where to store camera taken picture
            photo = createTemporaryFile("picture" + Math.random(), ".jpg");
            photo.delete();
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(getActivity(), "Please check SD card! Image shot is impossible!", Toast.LENGTH_LONG);
        }
        mImageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        //start camera intent
        getActivity().startActivityForResult(intent, CAMERA_INTENT_CODE);
    }

    private File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/temp/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        return File.createTempFile(part, ext, tempDir);
    }

    protected void populateBizInfos(String response, boolean isSuccess) {

        JSONArray bizArray = null;
        try {
            bizArray = new JSONArray(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (bizArray == null || bizArray.length() == 0) {
            return;
        }

        bizNameList = new ArrayList<>();
        bizIDList = new ArrayList<>();
        shopAreaList = new ArrayList<>();
        shopNameList = new ArrayList<>();

        bizNameList.add("Select Store");

        for (int i = 0; i < bizArray.length(); i++) {
            try {
                bizIDList.add(String.valueOf(bizArray.getJSONObject(i).getInt("bizId")));
                bizNameList.add(bizArray.getJSONObject(i).getString("bizName"));
                shopAreaList.add(bizArray.getJSONObject(i).getString("bizArea"));
                shopNameList.add(bizArray.getJSONObject(i).getString("bizName"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ArrayAdapter bizListAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_dropdown_item_1line, bizNameList);
        bizListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bizList.setAdapter(bizListAdapter);
        bizListAdapter.notifyDataSetChanged();

    }

    protected void processImageCapture(int requestCode, int resultCode, Intent data) {
        getActivity().getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = getActivity().getContentResolver();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, mImageUri);

            if (currentImageSelected == 1) {
                image1.setImageBitmap(bitmap);
                imageBitmap1 = bitmap;
            }

            if (currentImageSelected == 2) {
                image2.setImageBitmap(bitmap);
                imageBitmap2 = bitmap;
            }

            if (currentImageSelected == 3) {
                image3.setImageBitmap(bitmap);
                imageBitmap3 = bitmap;
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load", e);
        }
    }

    protected boolean validateInput() {
        if (imageBitmap1 == null && imageBitmap2 == null && imageBitmap3 == null) {
            Util.showSnackBar("Please click atleast one image", postButton);
            return false;
        }
        if (bizList.getSelectedItemPosition() == 0) {
            if (placeDescription.getText().equals("Pick a location")) {
                Util.showSnackBar("Please pick a location", postButton);
                return false;
            }
        }
        if (selectedCategories.size() == 0) {
            Util.showSnackBar("Please select a few categories", postButton);
            return false;
        }

        LocationManager lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled) {
            Util.showSnackBar("Please enable your device's location/gps", postButton);
            return false;
        }

        if (bizList.getSelectedItemPosition() == 0) {
            if (((CameraActivity) getActivity()).place.getAddress().length() == 0) {

                Util.showSnackBar("Please pick a valid location", postButton);
                return false;
            }
        }



        /*if (content.getText().length() == 0) {
            Util.showSnackBar("Please enter a description", postButton);
            return false;
        }*/

        return true;
    }


    protected void postToServer() throws JSONException, UnsupportedEncodingException {

        imageBitmap1 = null;
        imageBitmap2 = null;
        imageBitmap3 = null;
        linkedImages = new String[3];
    }

    View.OnClickListener resetImageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.reset_image1) {
                image1.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.image_place_holder));
                imageBitmap1 = null;
                v.setVisibility(View.GONE);
            }
            if (v.getId() == R.id.reset_image2) {
                image2.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.image_place_holder));
                imageBitmap2 = null;
                v.setVisibility(View.GONE);
            }
            if (v.getId() == R.id.reset_image3) {
                image3.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.image_place_holder));
                imageBitmap3 = null;
                v.setVisibility(View.GONE);
            }
        }
    };


    protected void uploadFailure() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                postButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                Util.showSnackBar("Something went wrong, please try again", postButton);
            }
        });

    }

    protected void recaptureImage(int imagePosition) {
        ((CameraActivity) getActivity()).launchCameraFragmentForEditImage(imagePosition, getActivity(), imageBitmap1, imageBitmap2, imageBitmap3);
    }

    protected void populateCategories() {

    }

}
