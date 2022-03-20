package ng.com.binkap.vibestar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import ng.com.binkap.vibestar.screens.MusicPlayerScreen;


public class MainActivity extends AppCompatActivity {

    TextView versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        versionName = findViewById(R.id.app_splash_version_name);
        versionName.setText("v".concat(BuildConfig.VERSION_NAME));
        new Handler().postDelayed(() -> {
                    startActivity(new Intent(getApplicationContext(), MusicPlayerScreen.class));
                    finish();
                }
                , 1000);
    }
}