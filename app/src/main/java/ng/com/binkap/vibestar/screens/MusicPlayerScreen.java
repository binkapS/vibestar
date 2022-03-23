package ng.com.binkap.vibestar.screens;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import java.io.File;
import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.ViewPagerAdapter;
import ng.com.binkap.vibestar.fragments.OnlineFragment;
import ng.com.binkap.vibestar.fragments.ProfileFragment;
import ng.com.binkap.vibestar.helpers.Universal;
import ng.com.binkap.vibestar.helpers.UsageInfo;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class MusicPlayerScreen extends AppCompatActivity {

    @SuppressLint("StaticFieldLeak")
    public static MusicPlayerScreen musicPlayerScreen;

    public MaterialCardView miniPlayer, header;

    public TextView miniPlayerSongName, miniPlayerSongArtist;

    public ImageView songArtCover, playPauseButton, nextButton;

    ImageView searchButton;

    ConstraintLayout mainBody;

    BottomNavigationView navigationView;

    ContentLoadingProgressBar progressBar;

    FrameLayout frameLayout;

    RelativeLayout songsView;

    ViewPager2 viewPager2;

    TabLayout tabLayout;

    ProfileFragment profileFragment = ProfileFragment.newInstance();

    OnlineFragment onlineFragment = OnlineFragment.newInstance();

    Fragment currentFragment;

    RelativeLayout exitDialog, exitDialogBody;

    MaterialButton exitButton;

    protected final int LOCAL_MUSIC_ICON = R.id.player_screen_local_music_icon;

    protected final int ONLINE_MUSIC_ICON = 23456789;

    protected final int PROFILE_ICON = R.id.player_screen_profile_icon;

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

    public static LinkedList<SongsModel> allSongs;

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
        checkPermissions();
        musicPlayerScreen = this;
        setContentIds();
        if (!SONGS_LIST_INITIALIZED){
            scanLoadMusic();
            if (hasLastSongPlayed()){
                setResumeMediaInfo();
            }
        }
        bindViewPager();
        navigationView.setOnItemSelectedListener(item -> {
           handleBottomNavigation(item.getItemId());
           return false;
       });
       navigationView.setOnItemReselectedListener(item -> handleBottomNavigation(item.getItemId()));
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
       if (getSupportFragmentManager().getBackStackEntryCount() > 0){
           super.onBackPressed();
       } else {
           exitDialog.setVisibility(View.VISIBLE);
           navigationView.setVisibility(View.GONE);
           exitButton.setOnClickListener(view -> {
               finishAndRemoveTask();
               super.onBackPressed();
           });
       }
    }

    public void scanLoadMusic(){
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
        navigationView = findViewById(R.id.player_screen_bottom_navigation);
        miniPlayer = findViewById(R.id.player_screen_bottom_player);
        header = findViewById(R.id.player_screen_header);
        miniPlayerSongName = findViewById(R.id.mini_player_song_name);
        miniPlayerSongArtist = findViewById(R.id.mini_player_song_artist);
        miniPlayerSongName.setSelected(true);
        songArtCover = findViewById(R.id.mini_player_song_art_cover);
        playPauseButton = findViewById(R.id.mini_player_play_pause_button);
        nextButton = findViewById(R.id.mini_player_next_button);
        frameLayout = findViewById(R.id.player_screen_frame_layout);
        tabLayout = findViewById(R.id.player_screen_tab_layout);
        viewPager2 = findViewById(R.id.player_screen_view_pager2);
        songsView = findViewById(R.id.player_screen_songs_view);
        searchButton = findViewById(R.id.playerScreen_search_icon);
        exitDialog = findViewById(R.id.music_player_screen_exit_dialog);
        exitDialogBody = findViewById(R.id.music_player_screen_exit_dialog_body);
        exitButton = findViewById(R.id.exit_button);
        progressBar = findViewById(R.id.mini_player_song_progress_bar);
        setClickListeners();
    }

    protected void setClickListeners(){
        playPauseButton.setOnClickListener(view -> handlePlayPause());
        nextButton.setOnClickListener(view -> nextMedia());
        searchButton.setOnClickListener(view -> {
            SearchScreen.loadToSearchFromList(allSongs);
            startActivity(new Intent(getApplicationContext(), SearchScreen.class));
        });
        miniPlayer.setOnClickListener(view -> {
            MusicControlScreen.setSongInfo(MusicPlayerService.currentSong);
            MusicControlScreen.updatePlayList(MusicPlayerService.allSongsLIst);
            startActivity(new Intent(getApplicationContext(), MusicControlScreen.class));
        });
        exitDialog.setOnClickListener(view -> {
            exitDialog.setVisibility(View.GONE);
            navigationView.setVisibility(View.VISIBLE);
        });
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

    protected void checkPermissions(){
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.MANAGE_EXTERNAL_STORAGE
            };
        }else {
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }
        for (String permission: permissions) {
            if (!hasPermission(permission)){
                requestPermission(permission);
            }
        }
    }

    protected boolean hasPermission(String permission){
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    protected void requestPermission(String permission){
        if (shouldShowRequestPermissionRationale(permission)){
            Toast.makeText(getApplicationContext(), "Grant ".concat(permission).concat(" from Settings"), Toast.LENGTH_SHORT).show();
        }else {
            requestPermissions(new String[]{permission}, Universal.PERMISSIONS_REQUEST_CODE);
        }
    }

    protected void handleBottomNavigation(int menuItemId){
        switch (menuItemId){
            case LOCAL_MUSIC_ICON:
                header.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);
                songsView.setVisibility(View.VISIBLE);
                break;
            case ONLINE_MUSIC_ICON:
                loadFragment(onlineFragment);
                break;
            case PROFILE_ICON:
                loadFragment(profileFragment);
                break;
        }
    }

    protected void bindViewPager(){
        viewPager2.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position){
                case 0:
                    tab.setText("Songs");
                    break;
                case 1:
                    tab.setText("Albums");
                    break;
                case 2:
                    tab.setText("Artists");
                    break;
                case 3:
                    tab.setText("Playlist");
            }
        }).attach();
    }

    public void loadFragment(Fragment fragment){
        if (fragment != currentFragment){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.player_screen_frame_layout, fragment, null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .addToBackStack(fragment.toString())
                    .commit();
            currentFragment = fragment;
        }
        header.setVisibility(View.GONE);
        songsView.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);
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
        navigationView.setBackgroundColor(colorPrimaryVariant);
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
