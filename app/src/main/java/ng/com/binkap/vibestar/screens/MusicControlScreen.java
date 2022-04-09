package ng.com.binkap.vibestar.screens;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;

import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.ControlScreenAdapter;
import ng.com.binkap.vibestar.helpers.Converter;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.listeners.OnTouchListener;
import ng.com.binkap.vibestar.helpers.Utils;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class MusicControlScreen extends AppCompatActivity {

    public TextView songName, songArtist, currentTimeProgress, totalTimeProgress, playlistViewToggle,
        miniSongName;

    public ImageView playModeButton, prevButton, playPauseButton, nextButton, favoriteButton,
            backButton, shareButton, songArtCover;

    AppCompatSeekBar seekBar;

    RelativeLayout controlsBody, miniPlayer;

    ConstraintLayout mainBody;

    RecyclerView playlist;

    ImageView playlistOpen, miniPlayPauseBtn, miniNextBtn, miniArtCover;

    MaterialCardView footer;

    CircularRevealCardView playPauseBackground;

    boolean playListActive = false;

    static SongsModel songInfo;

    static LinkedList<SongsModel> currentPLayList = new LinkedList<>();

    @SuppressLint("StaticFieldLeak")
    static MusicControlScreen musicControlScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicControlScreen = this;
        setContentView(R.layout.activity_music_control_screen);
        setContentIds();
        if (MusicPlayerService.mediaPlayer.isPlaying()){
            updateScreen(songInfo, PlaybackState.STATE_PLAYING, true);
        }else{
            updateScreen(songInfo, PlaybackState.STATE_PAUSED, true);
        }
        updatePlayModeButton();
        updateFavoriteButton();
        bindRecycler();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MusicPlayerService.mediaPlayer != null && MusicPlayerService.mediaPlayer.isPlaying()){
                    seekBar.setProgress(MusicPlayerService.mediaPlayer.getCurrentPosition());
                    currentTimeProgress.setText(Converter.convertTime(String.valueOf(MusicPlayerService.mediaPlayer.getCurrentPosition())));
                }
                new Handler().postDelayed(this, 1000);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (playListActive){
            playListToggleClicked();
        }else {
            super.onBackPressed();
        }
    }

    @SuppressLint("SetTextI18n")
    private void setContentIds(){
        songName = findViewById(R.id.music_control_screen_song_name);
        songArtist = findViewById(R.id.music_control_screen_artist_name);
        currentTimeProgress = findViewById(R.id.music_control_screen_current_progress_time);
        totalTimeProgress = findViewById(R.id.music_control_screen_total_progress_time);
        songName.setSelected(true);
        seekBar = findViewById(R.id.music_control_screen_seek_bar);
        playModeButton = findViewById(R.id.music_control_screen_play_mode);
        prevButton = findViewById(R.id.music_control_screen_prev_button);
        playPauseButton = findViewById(R.id.music_control_screen_play_pause_button);
        playPauseBackground = findViewById(R.id.music_control_screen_play_pause_button_frame);
        nextButton = findViewById(R.id.music_control_screen_next_button);
        favoriteButton = findViewById(R.id.music_control_screen_favorite_toggle);
        backButton = findViewById(R.id.music_control_screen_back_button);
        shareButton = findViewById(R.id.music_control_screen_share_button);
        songArtCover = findViewById(R.id.music_control_screen_song_art_cover);
        currentTimeProgress.setText("00:00");
        playlist = findViewById(R.id.music_control_screen_recycler);
        controlsBody = findViewById(R.id.music_control_screen_body);
        playlistViewToggle = findViewById(R.id.music_control_screen_playlist_toggle_text);
        playlistOpen = findViewById(R.id.music_control_screen_playlist_toggle);
        miniPlayer = findViewById(R.id.control_screen_mini_player);
        miniNextBtn = findViewById(R.id.control_screen_mini_player_next_button);
        miniPlayPauseBtn = findViewById(R.id.control_screen_mini_player_play_pause_button);
        miniSongName = findViewById(R.id.control_screen_mini_player_song_name);
        miniSongName.setSelected(true);
        miniArtCover = findViewById(R.id.control_screen_mini_player_art);
        mainBody = findViewById(R.id.music_control_screen_main_body);
        footer = findViewById(R.id.music_control_screen_footer);

        setClickListeners();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners(){
        backButton.setOnClickListener(view -> onBackPressed());
        nextButton.setOnClickListener(view -> sendBroadcast(new Intent(MusicPlayerService.PLAY_NEXT_MEDIA)));
        playPauseButton.setOnClickListener(view -> sendBroadcast(new Intent(MusicPlayerService.PLAY_PAUSE_MEDIA)));
        prevButton.setOnClickListener(view -> sendBroadcast(new Intent(MusicPlayerService.PLAY_PREV_MEDIA)));
        playModeButton.setOnClickListener(view -> sendBroadcast(new Intent(MusicPlayerService.CHANGE_PLAY_MODE)));
        playlistViewToggle.setOnClickListener(view -> playListToggleClicked());
        playlistOpen.setOnClickListener(view -> playListToggleClicked());
        miniNextBtn.setOnClickListener(view -> sendBroadcast(new Intent(MusicPlayerService.PLAY_NEXT_MEDIA)));
        miniPlayPauseBtn.setOnClickListener(view -> sendBroadcast(new Intent(MusicPlayerService.PLAY_PAUSE_MEDIA)));
        favoriteButton.setOnClickListener(view -> sendBroadcast(new Intent(MusicPlayerService.TOGGLE_FAVORITE)));
        shareButton.setOnClickListener(view -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(songInfo.getMimeType());
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(songInfo.getPath()));
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            setResult(Activity.RESULT_OK, shareIntent);
            startActivity(Intent.createChooser(shareIntent, songInfo.getTitle()));
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int seekPosition;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b){
                    seekPosition = i;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicPlayerService.mediaPlayer.seekTo(seekPosition);
            }
        });
        footer.setOnTouchListener(new OnTouchListener(getApplicationContext(), 10, 50) {

            @Override
            public void onSwipeDown() {
                playListToggleClicked();
            }

            @Override
            public void onSwipeUp() {
                playListToggleClicked();
            }

            @Override
            public void onTap() {
                playListToggleClicked();
            }
        });

        songArtCover.setOnTouchListener(new OnTouchListener(getApplicationContext(), 100, 100) {
            @Override
            public void onSwipeRight() {
                sendBroadcast(new Intent(MusicPlayerService.PLAY_PREV_MEDIA));
            }

            @Override
            public void onSwipeLeft() {
                sendBroadcast(new Intent(MusicPlayerService.PLAY_NEXT_MEDIA));
            }

            @Override
            public boolean doubleTaped() {
                sendBroadcast(new Intent(MusicPlayerService.PLAY_PAUSE_MEDIA));
                return true;
            }
        });
    }

    private void playListToggleClicked(){
        if (playListActive){
            playListActive = false;
            toggleVisibility(playlist, View.GONE);
            toggleVisibility(miniPlayer,  View.GONE);
            toggleVisibility(controlsBody, View.VISIBLE);
        }else {
            playlist.smoothScrollToPosition(currentPLayList.indexOf(songInfo));
            playListActive = true;
            toggleVisibility(controlsBody, View.GONE);
            toggleVisibility(playlist, View.VISIBLE);
            toggleVisibility(miniPlayer, View.VISIBLE);
        }
    }

    private void toggleVisibility(View view, int visibility){
        view.setVisibility(visibility);
    }

    private void bindRecycler(){
        playlist.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        playlist.setAdapter(new ControlScreenAdapter(currentPLayList));
        playlist.scrollToPosition(currentPLayList.lastIndexOf(songInfo));
    }

    public void updatePlayModeButton(){
        playModeButton.setImageResource(MusicPlayerService.getPlayModeIcon());
    }

    public void updateFavoriteButton(){
        favoriteButton.setImageResource(MusicPlayerService.getFavoriteIcon(getApplicationContext()));
    }

    public static void setSongInfo(SongsModel song){
        songInfo = song;
    }

    public static void updatePlayList(LinkedList<SongsModel> list){
        currentPLayList.clear();
        currentPLayList.addAll(list);
    }

    public void updateScreen(SongsModel songData, int playState, boolean updateAllScreen){
        if (playState == PlaybackState.STATE_PAUSED){
            playPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24);
            miniPlayPauseBtn.setImageResource(R.drawable.ic_round_play_arrow_24);
        }else {
            playPauseButton.setImageResource(R.drawable.ic_round_pause_24);
            miniPlayPauseBtn.setImageResource(R.drawable.ic_round_pause_24);
        }
        if (updateAllScreen){
            songInfo = songData;
            totalTimeProgress.setText(Converter.convertTime(songData.getDuration()));
            try {
                seekBar.setMax(Integer.parseInt(songData.getDuration()));
            }catch (NumberFormatException ignored){

            }
            songName.setText(songData.getTitle());
            songArtist.setText(songData.getArtist());
            miniSongName.setText(songData.getTitle());
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(songData.getThumbnail())
                    .error(R.drawable.vibe_star_logo_transparent_bg)
                    .centerCrop()
                    .into(miniArtCover);
            Utils.with(getApplicationContext())
                    .load(songData.getThumbnail())
                    .error(R.drawable.vibe_star_logo_transparent_bg)
                    .build()
                    .resize(1000, 1000)
                    .into(songArtCover);
            Palette palette = Palette.from(Utils.getBitmap())
                    .generate();
            int primaryColor = UserSettings.getColorPrimary(getApplicationContext());
            int primaryColorVariant = UserSettings.getColorPrimaryVariant(getApplicationContext());
            int darkenColor = Utils.getDarkenColor(palette.getDarkVibrantColor(primaryColor
            ), primaryColor, 0.51f);
            int brightColor = Utils.getDarkenColor(palette.getDarkVibrantColor(primaryColorVariant),
                    primaryColorVariant, 0.2f);
            mainBody.setBackgroundColor(darkenColor);
            setStatusBarColor(darkenColor);
            playPauseBackground.setCardBackgroundColor(brightColor);
            footer.setCardBackgroundColor(brightColor);
            updateFavoriteButton();
        }
    }

    public void setStatusBarColor(int color){
        getWindow().setStatusBarColor(color);
    }

    public static MusicControlScreen getMusicControlScreen() {
        return musicControlScreen;
    }

}