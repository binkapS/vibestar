package ng.com.binkap.vibestar.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.ViewPagerAdapter;
import ng.com.binkap.vibestar.helpers.UsageInfo;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.listeners.OnTouchListener;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class MusicPlayerScreen extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static MusicPlayerScreen musicPlayerScreen;

    public MaterialCardView miniPlayer, header;

    public TextView miniPlayerSongName, miniPlayerSongArtist;

    public ImageView songArtCover, playPauseButton, nextButton, profileIcon;

    ImageView searchButton;

    ConstraintLayout mainBody;

    ContentLoadingProgressBar progressBar;

    RelativeLayout songsView;

    ViewPager2 viewPager2;

    TabLayout tabLayout;

    RelativeLayout exitDialog, exitDialogBody;

    MaterialButton exitButton;

    protected final String SERVICE_STATE = "ng.com.binkap.vibestar.SERVICE_STATE";

    private boolean PLAYER_SERVICE_BOUNDED = false;

    MusicPlayerService playerService;

    protected final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MusicPlayerService.MusicBinder musicBinder = (MusicPlayerService.MusicBinder) iBinder;
            playerService = musicBinder.getService();
            setPLAYER_SERVICE_BOUNDED(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            setPLAYER_SERVICE_BOUNDED(false);
        }
    };

    public static boolean SONGS_LIST_INITIALIZED = false;

    String[] songsProjection = {
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.DURATION
    };

    Cursor cursor;

    String songsSelection = MediaStore.Audio.Media.IS_MUSIC + "!=0";

    public static LinkedList<SongsModel> allSongs = new LinkedList<>();

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SERVICE_STATE, isPLAYER_SERVICE_BOUNDED());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        setPLAYER_SERVICE_BOUNDED(savedInstanceState.getBoolean(SERVICE_STATE));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player_screen);
        setContentIds();
        musicPlayerScreen = this;
        if (!SONGS_LIST_INITIALIZED){
            scanLoadMusic();
            if (hasLastSongPlayed()){
                setResumeMediaInfo();
            }
        }
        bindViewPager();
        runOnUiThread(new Runnable() {
           @Override
           public void run() {
               if (MusicPlayerService.mediaPlayer.isPlaying()){
                   progressBar.setProgress(MusicPlayerService.mediaPlayer.getCurrentPosition());
               }
               new Handler().postDelayed(this, 1000);
           }
       });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applySettings();
        MusicPlayerService.updateMiniPlayer(-1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SONGS_LIST_INITIALIZED = true;
    }

    @Override
    public void onBackPressed() {
        exitDialog.setVisibility(View.VISIBLE);
        exitButton.setOnClickListener(view -> {
            finishAndRemoveTask();
            super.onBackPressed();
        });
    }

    public void scanLoadMusic(){
        allSongs.clear();
        allSongs = null;
        allSongs = new LinkedList<>();
        cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songsProjection,
                songsSelection, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        while (cursor.moveToNext()){
            String artist = cursor.getString(3).equals("<unknown>")
                    ? getResources().getString(R.string.unknown_artist) : cursor.getString(3);
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(cursor.getString(1)));
            SongsModel songData = new SongsModel(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    artist,
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    uri,
                    cursor.getString(7),
                    cursor.getString(8),
                    cursor.getString(9)
            );
            if (new File(songData.getPath()).exists()){
                allSongs.add(songData);
            }
        }
        cursor.close();
    }

    protected void setContentIds(){
        mainBody = findViewById(R.id.player_screen_main_body);
        miniPlayer = findViewById(R.id.player_screen_bottom_player);
        header = findViewById(R.id.player_screen_header);
        miniPlayerSongName = findViewById(R.id.mini_player_song_name);
        miniPlayerSongArtist = findViewById(R.id.mini_player_song_artist);
        miniPlayerSongName.setSelected(true);
        songArtCover = findViewById(R.id.mini_player_song_art_cover);
        playPauseButton = findViewById(R.id.mini_player_play_pause_button);
        nextButton = findViewById(R.id.mini_player_next_button);
        tabLayout = findViewById(R.id.player_screen_tab_layout);
        viewPager2 = findViewById(R.id.player_screen_view_pager2);
        songsView = findViewById(R.id.player_screen_songs_view);
        searchButton = findViewById(R.id.playerScreen_search_icon);
        exitDialog = findViewById(R.id.music_player_screen_exit_dialog);
        exitDialogBody = findViewById(R.id.music_player_screen_exit_dialog_body);
        exitButton = findViewById(R.id.exit_button);
        progressBar = findViewById(R.id.mini_player_song_progress_bar);
        profileIcon = findViewById(R.id.player_screen_profile_icon);
        setClickListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void setClickListeners(){
        playPauseButton.setOnClickListener(view -> handlePlayPause());
        nextButton.setOnClickListener(view -> nextMedia());
        searchButton.setOnClickListener(view -> {
            SearchScreen.loadToSearchFromList(allSongs);
            startActivity(new Intent(getApplicationContext(), SearchScreen.class));
        });
        miniPlayer.setOnTouchListener(new OnTouchListener(getApplicationContext(), 50, 50) {
            @Override
            public void onSwipeRight() {
                prevMedia();
            }

            @Override
            public void onSwipeLeft() {
                nextMedia();
            }

            @Override
            public boolean doubleTaped() {
                handlePlayPause();
                return true;
            }

            @Override
            public void onTap() {
                MusicControlScreen.setSongInfo(MusicPlayerService.currentSong);
                MusicControlScreen.updatePlayList(MusicPlayerService.allSongsLIst);
                startActivity(new Intent(getApplicationContext(), MusicControlScreen.class));
            }
        });
        exitDialog.setOnClickListener(view -> exitDialog.setVisibility(View.GONE));
        profileIcon.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ProfileScreen.class)));
    }

    public void loadSong(SongsModel song, int songPosition){
        MusicPlayerService.currentSong = song;
        MusicPlayerService.currentSongIndex = songPosition;
        if (!MusicPlayerService.autoPlayAllowed){
            MusicPlayerService.autoPlayAllowed = true;
        }
        if (!isPLAYER_SERVICE_BOUNDED()){
            initialize();
        }
        sendBroadcast(new Intent(MusicPlayerService.PLAY_NEW_AUDIO));
    }

    public void setResumeMediaInfo(){
        MusicPlayerService.updateSongsList(allSongs, -1);
        String songTitle = UsageInfo.getLastSongPlayTitle(getApplicationContext());
        SongsModel song = getLastSongInfo(songTitle);
        if (song != null){
            MusicPlayerService.currentSong = song;
            MusicPlayerService.currentSongIndex = MusicPlayerService.allSongsLIst.indexOf(song);
            updateMiniPlayer(song, PlaybackState.STATE_PAUSED);
            int lastPlayedPosition;
            if (UsageInfo.getLastSongPlayedPosition(getApplicationContext()) == UsageInfo.LAST_SONG_PLAYED_POSITION_DEFAULT_VALUE){
                lastPlayedPosition = 0;
            }else {
                lastPlayedPosition = UsageInfo.getLastSongPlayedPosition(getApplicationContext());
            }
            initialize();
            progressBar.setMax(Integer.parseInt(song.getDuration()));
            progressBar.setProgress(lastPlayedPosition);
            MusicPlayerService.updateResumeMediaInfo(lastPlayedPosition);
        }
    }

    private SongsModel getLastSongInfo(String songTitle){
        SongsModel song = null;
        for (SongsModel songInfo: allSongs) {
            if (songInfo.getTitle().equals(songTitle)){
                song = songInfo;
                break;
            }
        }
        return song;
    }

    public boolean hasLastSongPlayed(){
        return !UsageInfo.getLastSongPlayTitle(getApplicationContext()).equals(UsageInfo.LAST_SONG_PLAY_DEFAULT_VALUE);
    }

    public void initialize(){
        Intent playIntent = new Intent(getApplicationContext(), MusicPlayerService.class);
        if (bindService(playIntent, serviceConnection, Context.BIND_IMPORTANT)){
            MusicPlayerService.CURRENT_PLAY_MODE = UserSettings.getPlayMode(getApplicationContext());
            startService(playIntent);
        }
    }

    public void handlePlayPause(){
        sendBroadcast(new Intent(MusicPlayerService.PLAY_PAUSE_MEDIA));
    }

    public void nextMedia(){
        sendBroadcast(new Intent(MusicPlayerService.PLAY_NEXT_MEDIA));
    }

    public void prevMedia(){
        sendBroadcast(new Intent(MusicPlayerService.PLAY_PREV_MEDIA));
    }

    protected void bindViewPager(){
        viewPager2.setAdapter(new ViewPagerAdapter(this));
        viewPager2.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        String[] titles = new String[]{
               "Songs",
               "Albums",
               "Artist",
               "Playlists"
        };
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            tab.setText(titles[position]);
        }).attach();
    }

    public void updateMiniPlayer(SongsModel currentSong, int playState){
        if (playState == -1){
            if (MusicPlayerService.mediaPlayer.isPlaying()){
                playPauseButton.setImageResource(R.drawable.ic_round_pause_24);
            }else {
                playPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24);
            }
        }else {
            if (playState == PlaybackState.STATE_PAUSED){
                playPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24);
            }else  {
                playPauseButton.setImageResource(R.drawable.ic_round_pause_24);
            }
        }
        miniPlayerSongName.setText(currentSong.getTitle());
        miniPlayerSongArtist.setText(currentSong.getArtist());
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(currentSong.getThumbnail())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(songArtCover);
        miniPlayer.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        if (playState == PlaybackState.STATE_SKIPPING_TO_NEXT || playState == PlaybackState.STATE_SKIPPING_TO_PREVIOUS){
            progressBar.setProgress(0);
        }
        if (playState != PlaybackState.STATE_PAUSED){
            progressBar.setMax(Integer.parseInt(currentSong.getDuration()));
        }
    }

    private void applySettings(){
        int colorPrimary = UserSettings.getColorPrimary(getApplicationContext());
        int colorPrimaryVariant = UserSettings.getColorPrimaryVariant(getApplicationContext());

        mainBody.setBackgroundColor(colorPrimary);
        header.setCardBackgroundColor(colorPrimary);
        tabLayout.setBackgroundColor(colorPrimary);
        miniPlayer.setCardBackgroundColor(colorPrimaryVariant);
        exitDialogBody.setBackgroundColor(colorPrimaryVariant);
        exitButton.setBackgroundColor(colorPrimary);
        progressBar.setBackgroundColor(colorPrimaryVariant);
        setStatusBarColor(colorPrimary);
    }

    public void setStatusBarColor(int color){
        getWindow().setStatusBarColor(color);
    }

    public boolean isPLAYER_SERVICE_BOUNDED() {
        return PLAYER_SERVICE_BOUNDED;
    }

    public void setPLAYER_SERVICE_BOUNDED(boolean PLAYER_SERVICE_BOUNDED) {
        this.PLAYER_SERVICE_BOUNDED = PLAYER_SERVICE_BOUNDED;
    }

    public static MusicPlayerScreen getMusicPlayerScreen() {
        return musicPlayerScreen;
    }
}
