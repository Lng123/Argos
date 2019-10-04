package ca.bcit.argos;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

}
