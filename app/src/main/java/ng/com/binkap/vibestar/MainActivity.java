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

    String[] permissions = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    boolean permissionRequested = false;

    int milliSeconds;

    int maxWaitTime = 60;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        versionName = findViewById(R.id.app_splash_version_name);
        versionName.setText("v".concat(BuildConfig.VERSION_NAME));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (permissionsGranted()){
                    startActivity(new Intent(getApplicationContext(), MusicPlayerScreen.class));
                    finish();
                }else {
                    if (!permissionRequested){
                        checkPermissions();
                        permissionRequested = true;
                    }
                    if (milliSeconds >= maxWaitTime){
                        finishAndRemoveTask();
                    }
                    milliSeconds++;
                    new Handler().postDelayed(this, 500);
                }
            }
        });
    }

    protected void checkPermissions(){
        for (String permission: permissions) {
            if (permissionDenied(permission)){
                requestPermission(permission);
            }
        }
    }

    protected boolean permissionDenied(String permission){
        return checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED;
    }

    protected boolean permissionsGranted(){
        for (String permission: permissions) {
            if (permissionDenied(permission)){
                return false;
            }
        }
        return true;
    }

    protected void requestPermission(String permission){
        if (shouldShowRequestPermissionRationale(permission)){
            String message;
            if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)){
                message = "Allow Read Storage Permission From Settings To Continue";
            }else {
                message = "";
            }
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(this::finishAndRemoveTask, 2500);
        }else {
            requestPermissions(new String[]{permission}, Universal.PERMISSIONS_REQUEST_CODE);
        }
    }
}