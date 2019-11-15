package ca.bcit.argos;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.List;

import ca.bcit.argos.database.BikeRack;
import ca.bcit.argos.database.CrimeData;

public class BikeTheftMap extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "BikeRackMap";
    private GoogleMap mMap;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int LOCATION_PERMISSION_CODE = 123;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean locationPermissionGranted = false;
    private static float ZOOM = 16.0f;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private ClusterManager<ClusterItem> mClusterManager;

    private ProgressDialog pDialog;
    private MarkerOptions options = new MarkerOptions();
    private ArrayList<BikeRack> brList = new ArrayList<>();
    private List<WeightedLatLng> heatlist = new ArrayList<>();
    DatabaseReference databaseCrime;
    DatabaseReference databaseBikeracks;
    Switch theftsw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_map);


        databaseBikeracks = FirebaseDatabase.getInstance().getReference("bikeracks");
        databaseCrime = FirebaseDatabase.getInstance().getReference("crime");

        getLocationPermission();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        new AddBikeCrime().execute();
        mClusterManager = new ClusterManager<>(this, mMap);

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        if(locationPermissionGranted) {
            getDeviceLocation();
            mMap.setMyLocationEnabled(true);
        }

        theftsw = findViewById(R.id.switch1);
        theftsw.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(theftsw.isChecked()) {
                    addHeatMap();

                } else {
                    mOverlay.remove();
                }
            }
        });
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class AddBikeCrime extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(BikeTheftMap.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            final BitmapDescriptor icon = bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_marker);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    databaseBikeracks.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot brSnapshot : dataSnapshot.getChildren()) {
                                BikeRack br = brSnapshot.getValue(BikeRack.class);
                                mClusterManager.addItem(br);
                            }
                            mClusterManager.cluster();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });

                    databaseCrime.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot crSnapshot : dataSnapshot.getChildren()) {
                                    CrimeData cr = crSnapshot.getValue(CrimeData.class);
                                   double lat = cr.getLatitude();
                                   double lng = cr.getLongitude();
                                   int count = cr.getCount();
                                   LatLng loc = new LatLng(lat, lng);
                                   WeightedLatLng weighted = new WeightedLatLng(loc, count);
                                   heatlist.add(weighted);

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });

                }
            });

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

    private void addHeatMap() {

        // Create the gradient.
        int[] colors = {
                Color.rgb(50, 78, 168),
                Color.rgb(50, 168, 158),
                Color.rgb(50, 168, 98),
                Color.rgb(64, 168, 50),
                Color.rgb(115, 168, 50),
                Color.rgb(160, 168, 50),
                Color.rgb(160, 141, 50),
                Color.rgb(168, 113, 50),
                Color.rgb(168, 86, 50),
                Color.rgb(168, 50, 50),


        };

        float[] startPoints = {
                0.001f, 0.002f, 0.003f, 0.004f, 0.005f, 0.006f, 0.007f, 0.008f, 0.009f, 0.01f
        };

        Gradient gradient = new Gradient(colors, startPoints);

         mProvider = new HeatmapTileProvider.Builder()
                .weightedData(heatlist).radius(50).gradient(gradient).opacity(0.5)
                .build();

        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));

    }


    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getLocationPermission() {
        String[] permissions ={Manifest.permission.ACCESS_FINE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            initMap();
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_CODE);
        }
    }

    public void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Getting the devices current location
     */
    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(locationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG,"Found Location!");
                            Location currentLocation  = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    ZOOM);
                        } else {
                            Log.d(TAG,"Current location is null");
                            Toast.makeText(BikeTheftMap.this, "Unable to get " +
                                    "current location,", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the cameria to lat:" + latLng.latitude + ", lng:" + latLng.latitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;

        switch(requestCode) {
            case LOCATION_PERMISSION_CODE: {
                if (grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    locationPermissionGranted = true;

                }
            }
        }
    }
}
