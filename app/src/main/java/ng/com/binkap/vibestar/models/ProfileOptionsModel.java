package ng.com.binkap.vibestar.models;

import androidx.annotation.Nullable;

public class ProfileOptionsModel {

    int icon;

    String name;

    boolean isActivity;

    Class activity;

    public ProfileOptionsModel(int icon, String name, boolean isActivity, @Nullable Class activity) {
        this.icon = icon;
        this.name = name;
        this.isActivity = isActivity;
        this.activity = activity;
    }

    public int getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }

    public boolean isActivity() {
        return isActivity;
    }

    public Class getActivity() {
        return activity;
    }
}
