package ca.bcit.argos;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    Geocoder geoPoint;
    double latitude = 0;
    double longitude = 0;
    private ProgressDialog pDialog;
    private static String SERVICE_URL;
    private String TAG = MainActivity.class.getSimpleName();
    private String weather_api_key = "ddc7532a7f1456ae4fe26a755543d0ae";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView weekday = findViewById(R.id.tvWeekday);
        weekday.setText(getWeekday());

        TextView tvFullDate = findViewById(R.id.tvFullDate);
        tvFullDate.setText(getFullDate());


//        new GetWeather().execute();

    }

    public void showCityName(View view) {
        Toast.makeText(this, "TEST", Toast.LENGTH_SHORT).show();
    }

    private class GetWeather extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr = null;

            // Making a request to url and getting response
            jsonStr = sh.makeServiceCall(SERVICE_URL);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray weatherJsonArray = jsonObj.getJSONArray("items");
                    // looping through All Contacts
                    for (int i = 0; i < weatherJsonArray.length(); i++) {
                        JSONObject j = weatherJsonArray.getJSONObject(i);

                    }
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
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

//       Toast.makeText(MainActivity.this, book.getVolumeInfo().getIndustryIdentifiers().getIdentifier(), Toast.LENGTH_SHORT).show();
        }

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

    private String getCity(double lat, double lon){
        String cityName = "Not Found";
        geoPoint = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geoPoint.getFromLocation(lat, lon, 10);
            if (addresses.size() > 0){
                for(Address a: addresses){
                    if(a.getLocality() != null && a.getLocality().length() > 0){
                        cityName = a.getLocality();
                        break;
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return cityName;
    }

    public void onMapClick(View v) {
        Intent i = new Intent(this, BikeMap.class);
        startActivity(i);
    }

}
