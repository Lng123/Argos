package ca.bcit.argos;

import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
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

import ca.bcit.argos.database.BikeRack;
import ca.bcit.argos.database.DataHandler;
import ca.bcit.argos.database.HttpHandler;


public class MainActivity extends AppCompatActivity {

    Geocoder geoPoint;
    double latitude = 0;
    double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView weekday = findViewById(R.id.tvWeekday);
        weekday.setText(getWeekday());

        TextView tvFullDate = findViewById(R.id.tvFullDate);
        tvFullDate.setText(getFullDate());

        TextView tvCity= findViewById(R.id.tvCity);
//        tvCity.setText(getCity());

        brList = new ArrayList<BikeRack>();
        dataHandler = new DataHandler(this, null);
        new GetBikeRacks().execute();

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

    private String getCity(){
         geoPoint = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geoPoint.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0){
                Address address = addresses.get(0);
                return address.getLocality();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return "Not Found";
    }

    public void onMapClick(View v) {
        Intent i = new Intent(this, BikeMap.class);
        startActivity(i);
    }


    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;

    private ListView lv;
    private static String SERVICE_URL
            = "https://opendata.vancouver.ca/api/records/1.0/search/?dataset=bike-racks&rows=2000&facet=bia&facet=year_installed";
    private ArrayList<BikeRack> brList;
    private DataHandler dataHandler;

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

            // Making a request to url and getting response
            jsonStr = sh.makeServiceCall(SERVICE_URL);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    JSONArray brJsonArray = jsonObj.getJSONArray("records");
                    // looping through All Contacts
                    for (int i = 0; i < brJsonArray.length(); i++) {
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
                        System.out.println("ID " + id);
                        System.out.println("Snum " + snumber);
                        System.out.println("Sname " + sname + " \n");

                        // adding contact to contact list
                        brList.add(bikeRack);
                        dataHandler.addHandler(bikeRack);
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

}
