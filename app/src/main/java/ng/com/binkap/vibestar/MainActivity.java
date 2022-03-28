package ng.com.binkap.vibestar;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import ng.com.binkap.vibestar.helpers.Universal;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;


public class MainActivity extends AppCompatActivity {

    TextView versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        versionName = findViewById(R.id.app_splash_version_name);
        versionName.setText("v".concat(BuildConfig.VERSION_NAME));
        startActivity(new Intent(getApplicationContext(), MusicPlayerScreen.class));
        finish();
    }
}