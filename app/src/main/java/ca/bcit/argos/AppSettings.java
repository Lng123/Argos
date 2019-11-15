package ca.bcit.argos;

import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AppSettings extends AppCompatActivity {
    Spinner spinner;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        spinner = findViewById(R.id.bgSpinner);

        sp = getSharedPreferences("background", MODE_PRIVATE);
        String bg = sp.getString("colour", "Pink");

        switch (bg){
            case "Pink":
                spinner.setSelection(0);
                break;
            case "Blue":
                spinner.setSelection(1);
                break;
            case "Green":
                spinner.setSelection(2);
                break;
            case "Purple":
                spinner.setSelection(3);
                break;
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                TextView item = (TextView) view;
                ConstraintLayout settings = findViewById(R.id.settings);
                switch (item.getText().toString()){
                    case "Pink":
                        settings.setBackgroundResource(R.drawable.bg_pink);
                        break;
                    case "Blue":
                        settings.setBackgroundResource(R.drawable.bg_blue);
                        break;
                    case "Green":
                        settings.setBackgroundResource(R.drawable.bg_green);
                        break;
                    case "Purple":
                        settings.setBackgroundResource(R.drawable.bg_purple);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        changeBackground();
    }

    public void onSaveBackground(View view) {
        sp = getSharedPreferences("background", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("colour", spinner.getSelectedItem().toString());
        editor.apply();
        Toast.makeText(this, "Background changed.", Toast.LENGTH_SHORT).show();
    }

    public void changeBackground(){
        sp = getSharedPreferences("background", MODE_PRIVATE);
        String bg = sp.getString("colour", "Pink");
        ConstraintLayout settings = findViewById(R.id.settings);
        switch (bg){
            case "Pink":
                settings.setBackgroundResource(R.drawable.bg_pink);
                break;
            case "Blue":
                settings.setBackgroundResource(R.drawable.bg_blue);
                break;
            case "Green":
                settings.setBackgroundResource(R.drawable.bg_green);
                break;
            case "Purple":
                settings.setBackgroundResource(R.drawable.bg_purple);
                break;
        }
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("colour", bg);
        editor.apply();
    }
}
