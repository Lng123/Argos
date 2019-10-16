package ca.bcit.argos;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    Geocoder geoPoint;
    double latitude = 0;
    double longitude = 0;
    private String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private static final String API_KEY = BuildConfig.W_API_KEY;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView weekday = findViewById(R.id.tvWeekday);
        weekday.setText(getWeekday());

        TextView tvFullDate = findViewById(R.id.tvFullDate);
        tvFullDate.setText(getFullDate());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        } else {
            getLastLocation();
        }
    }

    /**
     * Asynchronous method for getting the book data from a JSON.
     */
    @SuppressLint("StaticFieldLeak")
    private class GetWeather extends AsyncTask<Void, Void, Void> {


        /**
         * Called before the asynchronous method begins.  Displays a "Please wait..." message so
         * the user knows the data is being read.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Showing progress dialog
//            pDialog = new ProgressDialog(MainActivity.this);
//            pDialog.setMessage("Please wait...");
//            pDialog.setCancelable(false);
//            pDialog.show();

        }

        /**
         * Grabs JSON data from the given url, gets the JSON object and stores the needed data into
         * BookVolume where it is appended to an ArrayList.
         * @param arg0;
         */
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr;

//            String SERVICE_URL = "https://www.googleapis.com/books/v1/volumes?q=harry+potter";
            // Making a request to url and getting response
            String SERVICE_URL = "https://api.openweathermap.org/data/2.5/weather?lat="
                                 + mLastLocation.getLatitude() + "&lon="
                                 + mLastLocation.getLongitude() + "&units=metric"
                                 + "&APPID=" + API_KEY;
            jsonStr = sh.makeServiceCall(SERVICE_URL);
            Log.e(TAG, "Response from url: " + jsonStr);

            try {

                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray weather = jsonObj.getJSONArray("weather");
                JSONObject weatherObj = weather.getJSONObject(0);
                String description = weatherObj.getString("main");

                JSONObject main = jsonObj.getJSONObject("main");
                String temp = (int) main.getDouble("temp") + "°";

                setText((TextView) findViewById(R.id.tvTemperature), temp);

            } catch (final JSONException e) {
                Log.e(TAG, "Json parsing error: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Json parsing error: " + e.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        /**
         * After all the data is saved, the BookAdapter is used to fill out the details of each list
         * item.
         * @param result;
         */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
//            if (pDialog.isShowing())
//                pDialog.dismiss();
        }

    }

    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

                            TextView tvCity = findViewById(R.id.tvCity);
                            tvCity.setText(getCityName(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                            new GetWeather().execute();

                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());
                            Toast.makeText(MainActivity.this, "No location detected", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setText(final TextView view,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(value);
            }
        });
    }

    public void toastAddress(View view) {

        Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
    }

    public void showCoordinates(View view) {
        getLastLocation();
        String coord = mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude();
        Toast.makeText(this, coord, Toast.LENGTH_SHORT).show();
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.permission_rationale,
                        android.R.string.ok,
                        new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                getLastLocation();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    public String getCityName(double lat, double lng) {
        geoPoint = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        String cityName = "N/A";
        try {
            addresses = geoPoint.getFromLocation(lat, lng, 1);
            if(addresses.get(0).getLocality() != null && addresses.size() > 0){
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    private String getWeekday() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.US);
        Date d = new Date();
        return sdf.format(d);
    }

    private String getFullDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        Date d = new Date();
        return sdf.format(d);
    }

    public void onMapClick(View v) {
        Intent i = new Intent(this, BikeMap.class);
        startActivity(i);
    }
}
