package ng.com.binkap.vibestar.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ng.com.binkap.vibestar.BuildConfig;
import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.helpers.UserSettings;

public class ProfileScreen extends AppCompatActivity {

    TextView appVersion, deviceVersion, deviceName;

    ImageView backButton;

    RelativeLayout header;

    ConstraintLayout mainBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        setContentIds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
    }

    private void setContentIds(){
        mainBody = findViewById(R.id.profile_screen_main_body);
        header = findViewById(R.id.profile_screen_header);
        appVersion = findViewById(R.id.profile_screen_app_version);
        deviceVersion = findViewById(R.id.profile_screen_device_version);
        deviceName = findViewById(R.id.profile_screen_device_name);
        backButton = findViewById(R.id.profile_screen_back_button);

        setClickListeners();
    }

    private void setClickListeners(){
        backButton.setOnClickListener(view -> onBackPressed());

        setValues();
    }

    @SuppressLint("SetTextI18n")
    private void setValues(){
        appVersion.setText(getResources().getText(R.string.app_name).toString().concat(" ").concat(BuildConfig.VERSION_NAME));
        deviceVersion.setText("Android ".concat(Build.VERSION.RELEASE));
        deviceName.setText(Build.DEVICE);
    }

    private void applySettings(){
        int colorPrimary = UserSettings.getColorPrimary(getApplicationContext());
        int colorPrimaryVariant = UserSettings.getColorPrimaryVariant(getApplicationContext());

        mainBody.setBackgroundColor(colorPrimary);
        header.setBackgroundColor(colorPrimaryVariant);
        setStatusBarColor(colorPrimary);
    }

    public void setStatusBarColor(int color){
        getWindow().setStatusBarColor(color);
    }
}