package vibinvarghese.citizencops;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Toast;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by vibinvarghese on 14/10/16.
 */

public class Util {

    public static void showSnackBar(String message, View view) {

        final Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();

    }

    public static Location getLocation(Context context) {

        Location location = null;

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            Toast.makeText(context.getApplicationContext(), "Please grant location permission", Toast.LENGTH_LONG).show();

            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(12.98158);
            location.setLongitude(80.21809);
            return location;
        }

        if (((CameraActivity) context).location != null)
            return ((CameraActivity) context).location;

        LocationManager locationManager =
                (LocationManager) context.getSystemService(LOCATION_SERVICE);

        try {
            locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Util.showSnackBar("Please Enable location", ((CameraActivity) context).postFragment.image1);
            } else {
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1000, (CameraActivity) context);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1000, (CameraActivity) context);
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (location == null)
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location == null)
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location == null)
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        if (location == null || location.getLongitude() == 0)
            location = locationManager.getLastKnownLocation(provider);

        if (location == null) {
            location = new Location(LocationManager.GPS_PROVIDER);
            location.setLatitude(12.98158);
            location.setLongitude(80.21809);
        }

        return location;
    }
}
