package ng.com.binkap.vibestar.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ng.com.binkap.vibestar.BuildConfig;
import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.ProfileOptionsAdapter;
import ng.com.binkap.vibestar.helpers.Strings;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.models.ProfileOptionsModel;
import ng.com.binkap.vibestar.screens.sub.AboutScreen;
import ng.com.binkap.vibestar.screens.sub.FeedBackScreen;
import ng.com.binkap.vibestar.screens.sub.SettingsScreen;

public class ProfileScreen extends AppCompatActivity {

    TextView appVersion, deviceVersion, deviceName;

    ImageView backButton;

    RelativeLayout header;

    ConstraintLayout mainBody;

    RecyclerView recyclerView;

    List<ProfileOptionsModel> options;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_screen);

        setContentIds();
        buildOptionsList();
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
        recyclerView = findViewById(R.id.profile_screen_recycler);

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

    private void buildOptionsList(){
        options = new ArrayList<>();
        options.add(new ProfileOptionsModel(
                R.drawable.ic_round_settings_24,
                Strings.SETTINGS,
                true,
                SettingsScreen.class
        ));
        options.add(new ProfileOptionsModel(
                R.drawable.ic_round_feedback_24,
                Strings.FEEDBACK,
                true,
                FeedBackScreen.class
        ));
        options.add(new ProfileOptionsModel(
                R.drawable.ic_round_thumb_up_24,
                Strings.RATE,
                false,
                null
        ));
        options.add(new ProfileOptionsModel(
                R.drawable.ic_round_share_24,
                Strings.SHARE,
                false,
                null
        ));
        options.add(new ProfileOptionsModel(
                R.drawable.ic_round_info_24,
                Strings.ABOUT,
                true,
                AboutScreen.class
        ));
        bindRecycler();
    }

    private void bindRecycler(){
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setAdapter(new ProfileOptionsAdapter(options));
    }
}