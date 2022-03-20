package ng.com.binkap.vibestar.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import ng.com.binkap.vibestar.models.SongsModel;


public class UsageInfo {

    public static final String LAST_SONG_PLAY = "ng.com.binkap.vibestar.helpers.LAST_SONG_PLAY";

    public static final String LAST_SONG_PLAYED_POSITION = "ng.com.binkap.vibestar.helpers.LAST_SONG_PLAYED_POSITION";

    public static final String LAST_SONG_PLAY_DEFAULT_VALUE = "LAST_SONG_PLAY";

    public static final int LAST_SONG_PLAYED_POSITION_DEFAULT_VALUE = -1;

    public static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(UserSettings.VIBE_STAR_PREFERENCE, Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getPreferencesEditor(Context context){
        return getPreferences(context).edit();
    }

    public static void setLastSongPlayTitle(SongsModel lastSongPlay, Context context){
        getPreferencesEditor(context).remove(LAST_SONG_PLAY).putString(LAST_SONG_PLAY, lastSongPlay.getTitle()).apply();
    }

    public static String getLastSongPlayTitle(Context context){
        return getPreferences(context).getString(LAST_SONG_PLAY, LAST_SONG_PLAY_DEFAULT_VALUE);
    }

    public static void setLastSongPlayedPosition(int position, Context context){
        getPreferencesEditor(context).remove(LAST_SONG_PLAYED_POSITION).putInt(LAST_SONG_PLAYED_POSITION, position).apply();
    }

    public static int getLastSongPlayedPosition(Context context){
        return getPreferences(context).getInt(LAST_SONG_PLAYED_POSITION, LAST_SONG_PLAYED_POSITION_DEFAULT_VALUE);
    }

}
