package ng.com.binkap.vibestar.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.database.PlaylistsData;
import ng.com.binkap.vibestar.helpers.Settings;
import ng.com.binkap.vibestar.helpers.Universal;
import ng.com.binkap.vibestar.helpers.UsageInfo;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.helpers.Utils;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.screens.MusicControlScreen;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;

public class MusicPlayerService extends MediaBrowserService implements AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener
{

    public static final String PLAY_NEW_AUDIO = "ng.com.binkap.vibestar.services.PLAY_NEW_AUDIO";

    public static final String PLAY_NEXT_MEDIA = "ng.com.binkap.vibestar.services.PLAY_NEXT_MEDIA";

    public static final String PLAY_PREV_MEDIA = "ng.com.binkap.vibestar.services.PLAY_PREV_MEDIA";

    public static final String PLAY_PAUSE_MEDIA = "ng.com.binkap.vibestar.services.PLAY_PAUSE_MEDIA";

    public static final String STOP_MEDIA = "ng.com.binkap.vibestar.services.STOP_MEDIA";

    public static final String CHANGE_PLAY_MODE = "ng.com.binkap.vibestar.services.CHANGE_PLAY_MODE";

    public static final String TOGGLE_FAVORITE = "ng.com.binkap.vibestar.services.TOGGLE_FAVORITE";

    public static final String NOTIFICATION_CHANNEL_ID = "ng.com.binkap.vibestar.NOTIFICATION_CHANNEL_ID";

    public static final String VIBE_STAR_ROOT_ID = "ng.com.binkap.vibestar.services.VIBE_STAR_ROOT_ID";

    public static final String VIBE_STAR_EMPTY_ROOT_ID = "ng.com.binkap.vibestar.services.VIBE_STAR_EMPTY_ROOT_ID";

    public static final int NOTIFICATION_ID = 637484;

    public static final int PLAY_MODE_SHUFFLE = 10000010;

    public static final int PLAY_MODE_LOOP_SINGLE = 10011001;

    public static final int PLAY_MODE_LOOP_ALL = 10010010;

    public static final int REQUEST_CODE = 3456789;

    public static int CURRENT_PLAY_MODE;

    public static boolean MEDIA_PLAYER_INITIALIZED = false;

    public static boolean PLAY_NEXT_ADDED = false;

    public static MediaPlayer mediaPlayer = new MediaPlayer();

    public AudioManager audioManager;

    MediaSession mediaSession;

    MediaSessionManager mediaSessionManager;

    MediaController.TransportControls transportControls;

    PlaybackState.Builder builder;

    AudioFocusRequest audioFocusRequest;

    AudioAttributes audioAttributes;

    AudioFocusRequest.Builder audioFocusBuilder;

    NotificationChannel channel;

    NotificationManager manager;

    private final IBinder iBinder = new MusicBinder();

    public static final Random random = new Random();

    public static LinkedList<SongsModel> allSongsLIst = new LinkedList<>();

    public static Queue<SongsModel> queuedSongs = new LinkedList<>();

    public static Queue<SongsModel> shuffleQueued = new LinkedList<>();

    public static  Stack<SongsModel> shuffleDequeued = new Stack<>();

    public static Stack<SongsModel> shuffleEnqueued = new Stack<>();

    public static SongsModel currentSong;

    public static int currentSongIndex;

    public static int playNextSongIndex;

    RemoteViews remoteViewBig;

    RemoteViews remoteViewsSmall;

    private String NOTIFICATION_MODE;

    boolean pausedByUser;

    public static boolean autoPlayAllowed;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case PLAY_NEW_AUDIO:
                    prepareMedia();
                    break;
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    pauseMedia(false);
                    break;
                case PLAY_PAUSE_MEDIA:
                    handlePLayPause();
                    break;
                case PLAY_NEXT_MEDIA:
                    playNextMedia(true);
                    break;
                case PLAY_PREV_MEDIA:
                    playPrevMedia();
                    break;
                case STOP_MEDIA:
                    stopMedia();
                    break;
                case TOGGLE_FAVORITE:
                    favouriteToggled();
                    break;
                case CHANGE_PLAY_MODE:
                    changePlayMode();
                    break;
            }
        }
    };

    private final MediaSession.Callback callback = new MediaSession.Callback() {

        @Override
        public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {
            return super.onMediaButtonEvent(mediaButtonIntent);
        }

        @Override
        public void onPlay() {
            super.onPlay();
            playMedia();
        }

        @Override
        public void onPause() {
            super.onPause();
            pauseMedia(true);
        }

        @Override
        public void onSkipToNext() {
            super.onSkipToNext();
            playNextMedia(true);
        }

        @Override
        public void onSkipToPrevious() {
            super.onSkipToPrevious();
            playPrevMedia();
        }

        @Override
        public void onStop() {
            super.onStop();
            stopMedia();
        }

        @Override
        public void onSeekTo(long pos) {
            super.onSeekTo(pos);
            mediaPlayer.seekTo((int) pos);
        }
    };

    private final String[] INTENT_FILTERS = {
            AudioManager.ACTION_AUDIO_BECOMING_NOISY,
            PLAY_PAUSE_MEDIA,
            PLAY_NEXT_MEDIA,
            PLAY_PREV_MEDIA,
            STOP_MEDIA,
            PLAY_NEW_AUDIO,
            CHANGE_PLAY_MODE,
            TOGGLE_FAVORITE
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String s, int i, @Nullable Bundle bundle) {
        return new BrowserRoot(VIBE_STAR_ROOT_ID, bundle);
    }

    @Override
    public void onLoadChildren(@NonNull String s, @NonNull Result<List<MediaBrowser.MediaItem>> result) {
        if (s.equals(VIBE_STAR_EMPTY_ROOT_ID)){
            result.sendResult(null);
            return;
        }
        List<MediaBrowser.MediaItem> mediaItems = new LinkedList<>();
        if (s.equals(VIBE_STAR_ROOT_ID)){
            result.sendResult(mediaItems);
        }
    }

    @Override
    public void onAudioFocusChange(int focusState) {
        switch (focusState){
            case AudioManager.AUDIOFOCUS_GAIN:
                if (pausedByUser){
                    break;
                }else {
                    raiseMediaVolume();
                    playMedia();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pauseMedia(false);
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                reduceMediaVolume();
                break;
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                raiseMediaVolume();
                break;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNextMedia(false);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (autoPlayAllowed){
            playMedia();
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(getApplicationContext(), "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK".concat(String.valueOf(extra)), Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Toast.makeText(getApplicationContext(), "MEDIA_ERROR_SERVER_DIED".concat(String.valueOf(extra)), Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(getApplicationContext(), "MEDIA_ERROR_UNKNOWN".concat(String.valueOf(extra)), Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
                Toast.makeText(getApplicationContext(), "MEDIA_ERROR_IO".concat(String.valueOf(extra)), Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mediaSessionManager == null) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
            mediaSession = new MediaSession(getApplicationContext(), getPackageName());
            transportControls = mediaSession.getController().getTransportControls();
            mediaSession.setActive(true);
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);
            builder = new PlaybackState.Builder()
                    .setActions(
                            PlaybackState.ACTION_SKIP_TO_NEXT |
                                    PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                                    PlaybackState.ACTION_PLAY |
                                    PlaybackState.ACTION_PAUSE |
                                    PlaybackState.ACTION_STOP |
                                    PlaybackState.ACTION_SEEK_TO
                    );
            mediaSession.setCallback(callback);
        }
        if (mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
        }
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        if (audioAttributes == null){
            audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
        }
        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        MEDIA_PLAYER_INITIALIZED = true;
        for (String INTENT_FILTER: INTENT_FILTERS) {
            IntentFilter filter = new IntentFilter(INTENT_FILTER);
            registerReceiver(broadcastReceiver, filter);
        }
        NOTIFICATION_MODE = UserSettings.getNotificationMode(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        buildAudioFocus();
        requestAudioFocus();
        if (autoPlayAllowed){
            prepareMedia();
        }
        return START_STICKY;
    }

    public static void updateResumeMediaInfo(int resumePosition) {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.seekTo(resumePosition);
        autoPlayAllowed = false;
    }

    private void buildAudioFocus(){
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioAttributes == null){
            audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
             audioFocusBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(this)
                    .setAudioAttributes(audioAttributes)
                    .setFocusGain(AudioManager.AUDIOFOCUS_GAIN)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                audioFocusBuilder.setForceDucking(true);
            }
            audioFocusRequest = audioFocusBuilder.build();
        }
    }

    private void requestAudioFocus(){
        if (audioFocusRequest == null){
            buildAudioFocus();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(audioFocusRequest);
        }else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    private void abandonAudioFocus(){
       audioManager.abandonAudioFocus(this);
    }

    private boolean hasAudioFocus(){
        if (audioManager == null){
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(audioFocusRequest);
        else return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        saveLastCurrentSongInfo();
        unregisterReceiver(broadcastReceiver);
        releaseMediaPlayer();
        removeNotification();
        abandonAudioFocus();
    }

    private void saveLastCurrentSongInfo(){
        UsageInfo.setLastSongPlayTitle(currentSong, getApplicationContext());
        UsageInfo.setLastSongPlayedPosition(mediaPlayer.getCurrentPosition(), getApplicationContext());
    }

    private PendingIntent getPendingIntent(String action){
        return PendingIntent.getBroadcast(getApplicationContext(),
                REQUEST_CODE, new Intent(action), PendingIntent.FLAG_IMMUTABLE);
    }


    private void setUpRemoteView(RemoteViews remoteViews, int who){
        if (who == 0){
            remoteViews.setOnClickPendingIntent(R.id.notification_small_next_btn, getPendingIntent(PLAY_NEXT_MEDIA));
            remoteViews.setOnClickPendingIntent(R.id.notification_small_prev_btn, getPendingIntent(PLAY_PREV_MEDIA));
            remoteViews.setOnClickPendingIntent(R.id.notification_small_play_pause_btn, getPendingIntent(PLAY_PAUSE_MEDIA));
        }else {
            remoteViews.setOnClickPendingIntent(R.id.notification_big_next_btn, getPendingIntent(PLAY_NEXT_MEDIA));
            remoteViews.setOnClickPendingIntent(R.id.notification_big_prev_btn, getPendingIntent(PLAY_PREV_MEDIA));
            remoteViews.setOnClickPendingIntent(R.id.notification_big_play_pause_btn, getPendingIntent(PLAY_PAUSE_MEDIA));
            remoteViews.setOnClickPendingIntent(R.id.notification_big_play_mode_btn, getPendingIntent(CHANGE_PLAY_MODE));
            remoteViews.setOnClickPendingIntent(R.id.notification_big_fav_button, getPendingIntent(TOGGLE_FAVORITE));
        }
    }

    private void updateRemoteViews(RemoteViews remoteViews, int playbackState){
        Utils.with(getApplicationContext())
                .load(currentSong.getThumbnail())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .build()
                .resize(250, 250);
        Palette palette = Palette.from(Utils.getBitmap())
                .generate();
        int color = Utils.getDarkenColor(palette.getDarkVibrantColor(getColor(R.color.colorPrimaryVariant)),
                getColor(R.color.colorPrimaryVariant), 0.51f);
        remoteViews.setInt(R.id.notification_big, "setBackgroundColor", color);
        remoteViews.setInt(R.id.notification_small, "setBackgroundColor", color);
        remoteViews.setImageViewBitmap(R.id.notification_big_icon, Utils.getBitmap());
        remoteViews.setImageViewBitmap(R.id.notification_small_icon, Utils.getBitmap());
        remoteViews.setImageViewResource(R.id.notification_big_play_mode_btn, getPlayModeIcon());
        remoteViews.setImageViewResource(R.id.notification_big_fav_button, getFavoriteIcon(getApplicationContext()));
        switch (playbackState){
            case PlaybackState.STATE_PLAYING:
                remoteViews.setTextViewText(R.id.notification_song_name, currentSong.getTitle());
                remoteViews.setTextViewText(R.id.notification_big_artist_name, currentSong.getArtist());
                remoteViews.setTextViewText(R.id.notification_small_song_name, currentSong.getTitle());
                remoteViews.setTextViewText(R.id.notification_small_artist_name, currentSong.getArtist());
                remoteViews.setImageViewIcon(R.id.notification_big_play_pause_btn, Icon.createWithResource(getApplicationContext(), R.drawable.ic_round_pause_24));
                remoteViews.setImageViewIcon(R.id.notification_small_play_pause_btn, Icon.createWithResource(getApplicationContext(), R.drawable.ic_round_pause_24));
                break;
            case PlaybackState.STATE_PAUSED:
                remoteViews.setImageViewIcon(R.id.notification_big_play_pause_btn, Icon.createWithResource(getApplicationContext(), R.drawable.ic_round_play_arrow_24));
                remoteViews.setImageViewIcon(R.id.notification_small_play_pause_btn, Icon.createWithResource(getApplicationContext(), R.drawable.ic_round_play_arrow_24));
                break;
        }
    }

    private RemoteViews getSmallContentView(int playbackState){
        if (remoteViewsSmall == null){
            remoteViewsSmall = new RemoteViews(getPackageName(), R.layout.notification_view_small);
            setUpRemoteView(remoteViewsSmall, 0);
        }
        updateRemoteViews(remoteViewsSmall, playbackState);
        return remoteViewsSmall;
    }

    private RemoteViews getBigContentView(int playbackState){
        if (remoteViewBig == null){
            remoteViewBig = new RemoteViews(getPackageName(), R.layout.notification_view_big);
            setUpRemoteView(remoteViewBig, -1);
        }
        updateRemoteViews(remoteViewBig, playbackState);
        return remoteViewBig;
    }

    public void updateMetadata(){
        mediaSession.setMetadata(new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_ARTIST, currentSong.getArtist())
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentSong.getTitle())
                .putString(MediaMetadata.METADATA_KEY_ALBUM, currentSong.getAlbum())
                .putLong(MediaMetadata.METADATA_KEY_DURATION, Long.parseLong(currentSong.getDuration()))
                .putString(MediaMetadata.METADATA_KEY_DATE, currentSong.getDateAdded())
                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, currentSong.getId())
                .build());
    }

    public Notification.Action getNotificationAction(String action, int resource, String title){
        return new Notification.Action.Builder(
                Icon.createWithResource(getApplicationContext(),
                        resource),
                title,
                getPendingIntent(action)
        ).build();
    }

    public static int getPlayModeIcon(){
        switch (CURRENT_PLAY_MODE) {
            case MusicPlayerService.PLAY_MODE_LOOP_ALL:
                return R.drawable.ic_round_repeat_24;
            case MusicPlayerService.PLAY_MODE_SHUFFLE:
                return R.drawable.ic_round_shuffle_24;
            case MusicPlayerService.PLAY_MODE_LOOP_SINGLE:
              return R.drawable.ic_round_repeat_one_24;
            default:return -1;
        }
    }

    public static int getFavoriteIcon(Context context){
        if (new PlaylistsData(context, Universal.FAVORITE_PLAYLIST).existsInPlaylist(currentSong)){
            return R.drawable.ic_round_favorite_24;
        }
        return R.drawable.ic_round_favorite_border_24;
    }

    public void buildNotification(int playbackState){
        Intent notificationIntent = new Intent(getApplicationContext(), MusicPlayerScreen.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), REQUEST_CODE, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager == null || channel == null){
                channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getPackageName(), NotificationManager.IMPORTANCE_LOW);
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                assert manager != null;
                manager.createNotificationChannel(channel);
            }
            if (NOTIFICATION_MODE.equals(Settings.NOTIFICATION_MODE_DEFAULT)){
                loadPostOreoNotification(pendingIntent, playbackState);
            }else {
                loadPreOreoNotification(pendingIntent, playbackState);
            }
        }else {
            loadPreOreoNotification(pendingIntent, playbackState);
        }
        updateMiniPlayer(playbackState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadPostOreoNotification(PendingIntent pendingIntent, int playbackState){
        Utils.with(getApplicationContext())
                .load(currentSong.getThumbnail())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .build()
                .resize(250, 250);
        builder.setState(playbackState,
                mediaPlayer.getCurrentPosition(),
                1f, SystemClock.elapsedRealtime());
        mediaSession.setPlaybackState(builder.build());
        Notification.Builder builder = new Notification.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        builder.setCategory(Notification.CATEGORY_TRANSPORT)
                .setContentIntent(pendingIntent)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.vibe_star_logo_transparent_bg)
                .setLargeIcon(Utils.getBitmap())
                .setContentTitle(currentSong.getTitle())
                .setSubText(currentSong.getArtist())
                .addAction(getNotificationAction(CHANGE_PLAY_MODE, getPlayModeIcon(), "ChangePlayMode"))
                .addAction(getNotificationAction(PLAY_PREV_MEDIA, R.drawable.ic_round_skip_previous_24, "Previous"))
                .addAction(getNotificationAction(PLAY_PAUSE_MEDIA,
                        playbackState == PlaybackState.STATE_PAUSED ? R.drawable.ic_round_play_arrow_24 :
                                R.drawable.ic_round_pause_24, "PlayPause"))
                .addAction(getNotificationAction(PLAY_NEXT_MEDIA, R.drawable.ic_round_skip_next_24, "Next"))
                .addAction(getNotificationAction(TOGGLE_FAVORITE, getFavoriteIcon(getApplicationContext()), "ToggleFavorite"))
                .setStyle(new Notification.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(1, 2, 3));
        startForeground(NOTIFICATION_ID, builder.build());
    }

    private void loadPreOreoNotification(PendingIntent pendingIntent, int playbackState){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);
        notificationBuilder
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.vibe_star_logo_transparent_bg)
                .setCustomBigContentView(getBigContentView(playbackState))
                .setCustomContentView(getSmallContentView(playbackState));
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }


    public void removeNotification(){
        if (manager == null){
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        manager.cancel(NOTIFICATION_ID);
        manager.cancelAll();
    }

    public void prepareMedia(){
        PLAY_NEXT_ADDED = false;
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        autoPlayAllowed = true;
    }

    public void handlePLayPause(){
        if (mediaPlayer.isPlaying()){
            pauseMedia(true);
        }else {
            playMedia();
        }
    }

    private void playMedia(){
        if (!hasAudioFocus()){
            requestAudioFocus();
        }
        mediaPlayer.start();
        updateMetadata();
        buildNotification(PlaybackState.STATE_PLAYING);
    }

    private void pauseMedia(boolean fromUser){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            pausedByUser = fromUser;
            buildNotification(PlaybackState.STATE_PAUSED);
        }
    }

    public void stopMedia(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        buildNotification(PlaybackState.STATE_PAUSED);
    }

    public void playNextMedia(boolean fromUser){
        if (PLAY_NEXT_ADDED){
            currentSong = allSongsLIst.get(playNextSongIndex);
        }else {
            if (queuedSongs.isEmpty()){
                if (CURRENT_PLAY_MODE == PLAY_MODE_LOOP_ALL){
                    if (allSongsLIst.isEmpty()){
                        updateSongsList(MusicPlayerScreen.allSongs, -1);
                    }
                    if (currentSongIndex == allSongsLIst.indexOf(allSongsLIst.getLast())){
                        currentSongIndex = allSongsLIst.lastIndexOf(allSongsLIst.getFirst());
                    }else {
                        currentSongIndex++;
                    }
                    currentSong = allSongsLIst.get(currentSongIndex);
                }else if (CURRENT_PLAY_MODE == PLAY_MODE_SHUFFLE){
                    if (shuffleEnqueued.isEmpty()){
                        if (shuffleQueued.isEmpty()){
                        cleanShuffleQueue();
                        buildShuffleQueue(allSongsLIst);
                    }
                    shuffleDequeued.push(currentSong);
                    currentSong = shuffleQueued.poll();
                    }else {
                        shuffleDequeued.push(currentSong);
                        currentSong = shuffleEnqueued.pop();
                    }
                }else if (CURRENT_PLAY_MODE == PLAY_MODE_LOOP_SINGLE && fromUser){
                    if (allSongsLIst.isEmpty()){
                        updateSongsList(MusicPlayerScreen.allSongs, -1);
                    }
                    if (currentSongIndex == allSongsLIst.indexOf(allSongsLIst.getLast())){
                        currentSongIndex = allSongsLIst.lastIndexOf(allSongsLIst.getFirst());
                    }else {
                        currentSongIndex++;
                    }
                    currentSong = allSongsLIst.get(currentSongIndex);
                }
            }else {
                currentSong = queuedSongs.poll();
            }
        }
        prepareMedia();
        updateMetadata();
        buildNotification(PlaybackState.STATE_SKIPPING_TO_NEXT);
    }

    public void playPrevMedia(){
        if (CURRENT_PLAY_MODE == PLAY_MODE_LOOP_SINGLE || CURRENT_PLAY_MODE == PLAY_MODE_LOOP_ALL){
            if (allSongsLIst.isEmpty()){
                updateSongsList(MusicPlayerScreen.allSongs, -1);
            }
            if (currentSongIndex == allSongsLIst.indexOf(allSongsLIst.getFirst())){
                currentSongIndex = allSongsLIst.lastIndexOf(allSongsLIst.getLast());
            }else {
                currentSongIndex--;
            }
            currentSong = allSongsLIst.get(currentSongIndex);
        }else if (CURRENT_PLAY_MODE == PLAY_MODE_SHUFFLE){
            if (shuffleDequeued.isEmpty()){
                cleanShuffleDequeue();
                buildShuffleDequeue();
            }
            shuffleEnqueued.push(currentSong);
            currentSong = shuffleDequeued.pop();
        }
        prepareMedia();
        updateMetadata();
        buildNotification(PlaybackState.STATE_SKIPPING_TO_PREVIOUS);
    }

    public static void buildShuffleQueue(List<SongsModel> list){
        LinkedList<SongsModel> loadFromList = new LinkedList<>(list);
        shuffleQueued.offer(loadFromList.remove(random.nextInt(loadFromList.size())));
        CompletableFuture.runAsync(() -> {
            while (!loadFromList.isEmpty()){
                shuffleQueued.offer(loadFromList.remove(random.nextInt(loadFromList.size())));
            }
        });
    }

    public static void buildShuffleDequeue(){
        if (shuffleQueued.isEmpty()){
            buildShuffleQueue(allSongsLIst);
        }
        shuffleDequeued.push(shuffleQueued.poll());
        CompletableFuture.runAsync(() -> {
            while (!shuffleQueued.isEmpty()){
                shuffleDequeued.push(shuffleQueued.poll());
            }
        });
    }

    public static void cleanShuffleQueue(){
        shuffleQueued.clear();
        shuffleDequeued.clear();
    }

    public static void cleanShuffleDequeue(){
        shuffleDequeued.clear();
    }

    public void raiseMediaVolume(){
        mediaPlayer.setVolume(1.0f, 1.0f);
    }

    public void reduceMediaVolume(){
        mediaPlayer.setVolume(0.01f, 0.01f);
    }

    public void releaseMediaPlayer(){
        stopMedia();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    public void changePlayMode(){
        switch (CURRENT_PLAY_MODE){
            case PLAY_MODE_LOOP_ALL:
                buildShuffleQueue(allSongsLIst);
                CURRENT_PLAY_MODE = PLAY_MODE_SHUFFLE;
                UserSettings.setPlayMode(PLAY_MODE_SHUFFLE, getApplicationContext());
                break;
            case PLAY_MODE_SHUFFLE:
                cleanShuffleQueue();
                CURRENT_PLAY_MODE = PLAY_MODE_LOOP_SINGLE;
                UserSettings.setPlayMode(PLAY_MODE_LOOP_SINGLE, getApplicationContext());
                break;
            case PLAY_MODE_LOOP_SINGLE:
                CURRENT_PLAY_MODE = PLAY_MODE_LOOP_ALL;
                UserSettings.setPlayMode(PLAY_MODE_LOOP_ALL, getApplicationContext());
                break;
        }
        if (MusicControlScreen.getMusicControlScreen() != null){
            MusicControlScreen.getMusicControlScreen().updatePlayModeButton();
        }
        buildNotification(mediaPlayer.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);
    }

    public void favouriteToggled(){
        PlaylistsData playlistsData = new PlaylistsData(getApplicationContext(), Universal.FAVORITE_PLAYLIST);
        if (!playlistsData.playlistExists(Universal.FAVORITE_PLAYLIST)){
            playlistsData.createPlaylist(Universal.FAVORITE_PLAYLIST);
        }
        if (playlistsData.existsInPlaylist(currentSong)){
            playlistsData.removeFromPlaylist(currentSong);
        }else {
            playlistsData.addToPlaylist(currentSong);
        }
        if (MusicControlScreen.getMusicControlScreen() != null){
            MusicControlScreen.getMusicControlScreen().updateFavoriteButton();
        }
        buildNotification(mediaPlayer.isPlaying() ? PlaybackState.STATE_PLAYING : PlaybackState.STATE_PAUSED);
    }

    public static void updateSongsList(LinkedList<SongsModel> songList, int possiblePosition){
        if (possiblePosition != -1){
            SongsModel song = allSongsLIst.get(possiblePosition);
            if (!queuedSongs.isEmpty()){
                queuedSongs.remove(song);
            }
            if (!shuffleQueued.isEmpty()){
                shuffleQueued.remove(song);
            }
            if (!shuffleDequeued.isEmpty()){
                shuffleDequeued.remove(song);
            }
            if (!shuffleEnqueued.isEmpty()){
                shuffleEnqueued.remove(song);
            }
        }
        allSongsLIst.clear();
        allSongsLIst = null;
        allSongsLIst = new LinkedList<>(songList);
        MusicControlScreen.updatePlayList(songList);
    }

    public static void updateMiniPlayer(int playState){
            if (MEDIA_PLAYER_INITIALIZED){
                if (MusicControlScreen.getMusicControlScreen() != null){
                    MusicControlScreen.getMusicControlScreen().updateScreen(currentSong, playState, playState == PlaybackState.STATE_PLAYING);
                }
                MusicPlayerScreen.getMusicPlayerScreen().updateMiniPlayer(currentSong, playState);
            }
    }

    public class MusicBinder extends Binder {

        public MusicPlayerService getService(){
            return MusicPlayerService.this;
        }
    }
}
