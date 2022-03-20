package ng.com.binkap.vibestar.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import ng.com.binkap.vibestar.services.MusicPlayerService;

public class UserSettings {

    public static final String VIBE_STAR_PREFERENCE = "ng.com.binkap.vibestar.helpers.VIBE_STAR_PREFERENCE";

    private static final String HELPER_SORT_ORDER = "ng.com.binkap.vibestar.helpers.HELPER_SORT_ORDER";

    private static final String HELPER_SORT_BY = "ng.com.binkap.vibestar.helpers.HELPER_SORT_BY";

    private static final String PLAY_MODE = "ng.com.binkap.vibestar.helpers.PLAY_MODE";

    private static final String NOTIFICATION_MODE = "ng.com.binkap.vibestar.helpers.NOTIFICATION_MODE";

    public static final String COLOR_PRIMARY = "ng.com.binkap.vibestar.helpers.COLOR_PRIMARY";

    public static final String COLOR_PRIMARY_VARIANT = "ng.com.binkap.vibestar.helpers.COLOR_PRIMARY_VARIANT";

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(VIBE_STAR_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getPreferencesEditor(Context context){
        return getPreferences(context).edit();
    }

    public static int getSongsSortOrder(Context context) {
        return getPreferences(context).getInt(HELPER_SORT_ORDER, Sorts.SORT_DESCENDING);
    }

    public static void setSongsSortOrder(int songsSortOrder, Context context) {
        getPreferencesEditor(context).remove(HELPER_SORT_ORDER).putInt(HELPER_SORT_ORDER, songsSortOrder).apply();
    }

    public static String getSongsSortBy(Context context) {
        return getPreferences(context).getString(HELPER_SORT_BY, Sorts.SORT_BY_TITLE);
    }

    public static void setSongsSortBy(String songsSortBy, Context context) {
        getPreferencesEditor(context).remove(HELPER_SORT_BY).putString(HELPER_SORT_BY, songsSortBy).apply();
    }

    public static int getPlayMode(Context context){
        return getPreferences(context).getInt(PLAY_MODE, MusicPlayerService.PLAY_MODE_LOOP_ALL);
    }

    public static void setPlayMode(int playMode, Context context){
        getPreferencesEditor(context).remove(PLAY_MODE).putInt(PLAY_MODE, playMode).apply();
    }

    public static String getNotificationMode(Context context){
        return getPreferences(context).getString(NOTIFICATION_MODE, Settings.NOTIFICATION_MODE_DEFAULT);
    }

    public static void setNotificationMode(String notificationMode, Context context){
        getPreferencesEditor(context).remove(NOTIFICATION_MODE).putString(NOTIFICATION_MODE, notificationMode).apply();
    }

    public static int getColorPrimary(Context context){
        return getPreferences(context).getInt(COLOR_PRIMARY, context.getColor(Settings.COLOR_PRIMARY_DEFAULT));
    }

    public static void setColorPrimary(int colorPrimary, Context context){
        getPreferencesEditor(context).remove(COLOR_PRIMARY).putInt(COLOR_PRIMARY, colorPrimary).apply();
    }

    public static int getColorPrimaryVariant(Context context){
        return getPreferences(context).getInt(COLOR_PRIMARY_VARIANT, context.getColor(Settings.COLOR_PRIMARY_VARIANT_DEFAULT));
    }

    public static void setColorPrimaryVariant(int colorPrimaryVariant, Context context){
        getPreferencesEditor(context).remove(COLOR_PRIMARY_VARIANT).putInt(COLOR_PRIMARY_VARIANT, colorPrimaryVariant).apply();
    }
}
