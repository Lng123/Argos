package ca.bcit.argos;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ca.bcit.argos.database.BikeRack;
import ca.bcit.argos.database.CrimeData;
import ca.bcit.argos.database.DataHandler;
import ca.bcit.argos.database.HttpHandler;

// For scv, comment out when done.
// import com.opencsv.CSVReader;
// import java.io.IOException;
// import java.io.FileReader;


public class MainActivity extends AppCompatActivity {

    Geocoder geoPoint;
    private String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastLocation;
    private static final String API_KEY = BuildConfig.W_API_KEY;
    DatabaseReference databaseBikeracks;
    DatabaseReference databaseCrime;

    private static String SERVICE_URL
            = "https://opendata.vancouver.ca/api/records/1.0/search/?dataset=bike-racks&rows=2000&facet=bia&facet=year_installed";
    private static String GEOCODE_URL
            = "https://maps.googleapis.com/maps/api/geocode/json?address=";
    private ArrayList<BikeRack> brList;
    private ArrayList<CrimeData> crimeList;
    private DataHandler dataHandler;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseBikeracks = FirebaseDatabase.getInstance().getReference("bikeracks");
        databaseCrime = FirebaseDatabase.getInstance().getReference("crime");

        TextView weekday = findViewById(R.id.tvWeekday);
        weekday.setText(getWeekday());

        TextView tvFullDate = findViewById(R.id.tvFullDate);
        tvFullDate.setText(getFullDate());

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        brList = new ArrayList<BikeRack>();
        crimeList = new ArrayList<CrimeData>();
        dataHandler = new DataHandler(this, null);
        // Code for adding BikeRack data to firebase.
        // Only use it to update the firebase database with new sets of JSON and not to call it
        // for any other database related actions.
        //new GetBikeRacks().execute();
        /*for (BikeRack b : brList) {
            dataHandler.addHandler(b);
        }*/


        // Code for adding Crime Data data to firebase.
        // Only use it to update the firebase database with new sets of JSON and not to call it
        // for any other database related actions.
        /*
        new GetCrimes().execute();
        */

        databaseBikeracks.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot bikeRackSnapshot : dataSnapshot.getChildren()) {
                    BikeRack bikeRack = bikeRackSnapshot.getValue(BikeRack.class);
                    brList.add(bikeRack);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        databaseCrime.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot crimeSnapshot : dataSnapshot.getChildren()) {
                    CrimeData crime = crimeSnapshot.getValue(CrimeData.class);
                    crimeList.add(crime);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }));
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

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr;

            // Making a request to url and getting response
            String WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat="
                                 + mLastLocation.getLatitude() + "&lon="
                                 + mLastLocation.getLongitude() + "&units=metric"
                                 + "&APPID=" + API_KEY;
            jsonStr = sh.makeServiceCall(WEATHER_URL);

            try {

                JSONObject jsonObj = new JSONObject(jsonStr);
                JSONArray weather = jsonObj.getJSONArray("weather");
                JSONObject weatherObj = weather.getJSONObject(0);
                String description = weatherObj.getString("main");
                changeWeatherIcon((ImageView) findViewById(R.id.ivWeatherIcon),description);

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

    private void changeWeatherIcon(final ImageView view, final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (value){
                    case "Clouds":
                        view.setImageResource(R.drawable.weather_clouds);
                        break;
                    case "Clear":
                        view.setImageResource(R.drawable.weather_clear);
                        break;
                    case "Drizzle":
                        view.setImageResource(R.drawable.weather_drizzle);
                        break;
                    case "Rain":
                        view.setImageResource(R.drawable.weather_rain);
                        break;
                    case "Snow":
                        view.setImageResource(R.drawable.weather_snow);
                        break;
                    case "Thunderstorm":
                        view.setImageResource(R.drawable.weather_thunderstorm);
                        break;
                    default:
                        view.setImageResource(R.drawable.weather_unknown);
                }
            }
        });
    }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        if (shouldProvideRationale) {
            showSnackbar(R.string.permission_rationale, android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
            });
        } else {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
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
            if(addresses.size() > 0){
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
        Intent i = new Intent(this, BikeTheftMap.class);
        startActivity(i);
    }

    public void onSettingsClick(View view) {
        Intent i = new Intent(this, AppSettings.class);
        startActivity(i);
    }

    // Class for updating the BikeRacks data to the firebase database.  Do not use this
    // if the firebase database is already in place, this is not the code for calling data
    // from firebase database.
    private class GetBikeRacks extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            //pDialog.show();

        }


        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String jsonStr = null;
            String geojsonStr = null;

            // Making a request to url and getting response
            jsonStr = sh.makeServiceCall(SERVICE_URL);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray brJsonArray = jsonObj.getJSONArray("records");
                    // looping through All Contacts
                    for (int i = 0; i < brJsonArray.length(); i++) {
//                    for (int i = 0; i < brJsonArray.length(); i++) {
                        JSONObject c = brJsonArray.getJSONObject(i);

                        int id = i + 1;
                        int snumber;
                        try {
                            snumber = c.getJSONObject("fields").getInt("street_number");
                        } catch(Exception e) {
                            snumber = 0;
                        }
                        String sname;
                        try {
                            sname = c.getJSONObject("fields").getString("street_name");
                        } catch (Exception e){
                            sname = "";
                        }
                        String sside;
                        try {
                            sside = c.getJSONObject("fields").getString("street_side");
                        } catch (Exception e) {
                            sside = "";
                        }
                        String stsname;
                        try {
                            stsname = c.getJSONObject("fields").getString("skytrain_station_name");
                        } catch(Exception e) {
                            stsname = "";
                        }
                        String bia;
                        try {
                            bia = c.getJSONObject("fields").getString("bia");
                        } catch(Exception e) {
                            bia = "";
                        }
                        int nor;
                        try {
                            nor = c.getJSONObject("fields").getInt("number_of_racks");
                        } catch (Exception e) {
                            nor = 0;
                        }
                        String yi;
                        try {
                            yi = c.getJSONObject("fields").getString("year_installed");
                        } catch (Exception e) {
                            yi = "";
                        }

                        String api = getResources().getString(R.string.google_geocode_key);
                        String geostr = GEOCODE_URL + snumber + sname + ",+Canada,Vancouver,+CA&key="
                                + api;
                        geojsonStr = sh.makeServiceCall(geostr);
                        Log.e(TAG, "Response from url: " + geojsonStr);
                        double lon = 0;
                        double lat = 0;


                        if (geojsonStr != null) {
                            try {
                                JSONObject geojsonObj = new JSONObject(geojsonStr);

                                JSONObject geobrJson = geojsonObj.getJSONArray("results")
                                        .getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                                lon = geobrJson.getDouble("lng");
                                lat = geobrJson.getDouble("lat");

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


                        // tmp hash map for single contact
                        BikeRack bikeRack = new BikeRack();

                        // adding each child node to HashMap key => value
                        bikeRack.setID(id);
                        bikeRack.setStreetNumber(snumber);
                        bikeRack.setStreetName(sname);
                        bikeRack.setStreetSide(sside);
                        bikeRack.setSkytrainStationName(stsname);
                        bikeRack.setBIA(bia);
                        bikeRack.setNumberOfRacks(nor);
                        bikeRack.setYearInstalled(yi);
                        bikeRack.setLongitude(lon);
                        bikeRack.setLatitude(lat);

                        String fbid = databaseBikeracks.push().getKey();
                        Task setValueTask = databaseBikeracks.child(fbid).setValue(bikeRack);

                        setValueTask.addOnSuccessListener(new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                Toast.makeText(MainActivity.this,"BikeRack added.",Toast.LENGTH_LONG).show();
                            }
                        });

                        setValueTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,
                                        "something went wrong.\n" + e.toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                        // adding contact to contact list
                        brList.add(bikeRack);
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

        }
    }


    /*
    private class GetCrimes extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            //pDialog.show();

        }


        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();
            String[] scvrow = null;
            String geojsonStr = null;

            try {
                InputStream csvfile = getResources().openRawResource(R.raw.crimedata_csv_all_years);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(csvfile, Charset.forName("UTF-8")));
                String line = "";
                line = reader.readLine();
                scvrow = line.split(",");
                String type = scvrow[0];
                String year = scvrow[1];
                String hundred_block = scvrow[6];
                String neighborhood = scvrow[7];
                int id = 1;
                while ((line = reader.readLine()) != null) {
                    scvrow = line.split(",");
                    type = scvrow[0];
                    year = scvrow[1];
                    if (type.equals("Theft of Bicycle") && Integer.parseInt(year) >= 2015) {
                        boolean has = false;
                        int crimeIndex = 0;
                        hundred_block = scvrow[6];
                        neighborhood = scvrow[7];
                        for (CrimeData crime: crimeList) {
                            if (crime.getHundredBlock().equals(hundred_block)) {
                                has = true;
                                crimeIndex = crimeList.indexOf(crime);
                            }
                        }
                        if (has) {
                            int count = crimeList.get(crimeIndex).getCount();
                            crimeList.get(crimeIndex).setCount(count + 1);
                        } else {
                            CrimeData c = new CrimeData(id, 1, hundred_block, neighborhood, 0, 0);
                            crimeList.add(c);
                            id++;
                        }
                    }

                }
            } catch (final IOException ex) {
                Log.e(TAG, "CSV parsing error: " + ex.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "CSV parsing error: " + ex.getMessage(),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }


            for (CrimeData c: crimeList) {
                try {
                        String street = c.getHundredBlock();
                        street = street.replace("XX", "50");
                        String api = getResources().getString(R.string.google_geocode_key);
                        String geostr = GEOCODE_URL + street + ",+Canada,Vancouver,+CA&key="
                                + api;
                        geojsonStr = sh.makeServiceCall(geostr);
                        Log.e(TAG, "Response from url: " + geojsonStr);
                        double lon = 0;
                        double lat = 0;


                        if (geojsonStr != null) {
                            try {
                                JSONObject geojsonObj = new JSONObject(geojsonStr);

                                JSONObject geobrJson = geojsonObj.getJSONArray("results")
                                        .getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                                lon = geobrJson.getDouble("lng");
                                lat = geobrJson.getDouble("lat");

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

                        c.setLongitude(lon);
                        c.setLatitude(lat);

                        String cid = databaseCrime.push().getKey();
                        Task setValueTask = databaseCrime.child(cid).setValue(c);

                        setValueTask.addOnSuccessListener(new OnSuccessListener() {
                            @Override
                            public void onSuccess(Object o) {
                                Toast.makeText(MainActivity.this,"Crime data added.",Toast.LENGTH_LONG).show();
                            }
                        });

                        setValueTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(MainActivity.this,
                                        "something went wrong.\n" + e.toString(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                } catch (final Exception e) {
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
            }
            for (CrimeData crime : crimeList) {
                String test = "id: " + crime.getID() + ", count: " + crime.getCount() +
                        ", hundredblock: " + crime.getHundredBlock() + ", ngh: " + crime.getNeighbourhood() +
                        ", lonlan" + crime.getLongitude() + ", " + crime.getLatitude();
                Log.e(TAG, test);

            }




            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

        }
    }*/

}
