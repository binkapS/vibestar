package ng.com.binkap.vibestar.screens.sub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.helpers.Strings;
import ng.com.binkap.vibestar.helpers.UserSettings;

public class SettingsScreen extends AppCompatActivity {

    ConstraintLayout mainBody;

    RelativeLayout header;

    ImageView backButton;

    TextView headerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_screen);

        setContentIds();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
    }

    private void setContentIds(){
        mainBody = findViewById(R.id.settings_screen_main_body);
        header = findViewById(R.id.settings_screen_header);
        headerText = findViewById(R.id.settings_screen_header_text);
        backButton = findViewById(R.id.settings_screen_back_button);


        setValues();
    }

    private void setValues(){
        headerText.setText(Strings.SETTINGS);

        setClickListeners();
    }

    private void setClickListeners(){
        backButton.setOnClickListener(view -> onBackPressed());
    }

    private void applySettings(){
        int colorPrimary = UserSettings.getColorPrimary(getApplicationContext());
        int colorPrimaryVariant = UserSettings.getColorPrimaryVariant(getApplicationContext());

        mainBody.setBackgroundColor(colorPrimary);

        setStatusBarColor(colorPrimary);
    }

    public void setStatusBarColor(int color){
        getWindow().setStatusBarColor(color);
    }
}