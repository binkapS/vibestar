package ng.com.binkap.vibestar.screens.sub;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.circularreveal.cardview.CircularRevealCardView;

import java.io.File;
import java.util.LinkedList;
import java.util.Random;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.PlaylistsSongsAdapter;
import ng.com.binkap.vibestar.adapters.SongsAdapter;
import ng.com.binkap.vibestar.database.PlaylistsData;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.helpers.Utils;
import ng.com.binkap.vibestar.models.AlbumsModel;
import ng.com.binkap.vibestar.models.ArtistsModel;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;
import ng.com.binkap.vibestar.screens.SearchScreen;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class SongsScreen extends AppCompatActivity {

    ImageView backButton, searchButton, shufflePlayAll, sortButton;

    TextView headerText;

    RelativeLayout header, toolBar;

    RecyclerView recyclerView;

    ConstraintLayout mainBody;

    CircularRevealCardView headerImage;

    public static LinkedList<SongsModel> songList;

    static boolean usingAlbums;

    static boolean usingArtist;

    static boolean usingPLayList;

    static AlbumsModel album;

    static ArtistsModel artist;

    static String playlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_screen);
        setContentIds();
        setUpScreen();
        setHeaderImage();
    }

    private void setUpScreen(){
        if (usingAlbums){
            headerText.setText(album.getName());
            buildAlbumSongs();
        }else if (usingArtist){
            headerText.setText(artist.getName());
            buildArtistSongs();
        }else if (usingPLayList){
            headerText.setText(playlist);
            buildPlaylistSongs(playlist, getApplicationContext());
        }
        bindRecycler();
    }

    public static void setValues(@Nullable AlbumsModel albumsModel, @Nullable ArtistsModel artistsModel, @Nullable String playlistName, boolean isAlbum, boolean isArtist, boolean isPlaylist){
        usingAlbums = isAlbum;
        usingArtist = isArtist;
        usingPLayList = isPlaylist;
        album = albumsModel;
        artist = artistsModel;
        playlist = playlistName;
    }

    private void buildArtistSongs(){
        String name = artist.getName();
        songList = new LinkedList<>();
        LinkedList<SongsModel> list = new LinkedList<>(MusicPlayerScreen.allSongs);
        int count = Integer.parseInt(artist.getTracks());
        int increment = 0;
        while (songList.size() < count && increment < list.size()){
            SongsModel song = list.get(increment);
            if (song.getArtist().equals(name)){
                songList.add(song);
            }
            increment++;
        }
    }

    private void buildAlbumSongs(){
        String name = album.getName();
        String artist = album.getArtist();
        songList = new LinkedList<>();
        LinkedList<SongsModel> list = new LinkedList<>(MusicPlayerScreen.allSongs);
        int count = Integer.parseInt(album.getCount());
        int increment = 0;
        while (songList.size() < count && increment < list.size()){
            SongsModel song = list.get(increment);
            if (song.getAlbum().equals(name) && song.getArtist().equals(artist)){
                songList.add(song);
            }
            increment++;
        }
    }

    public static void buildPlaylistSongs(String playlist, Context context){
        songList = new LinkedList<>();
        PlaylistsData playlistsData = new PlaylistsData(context, playlist);
        Cursor cursor = playlistsData.getPlaylistData();
        while (songList.size() < playlistsData.getPlaylistCount(playlist) && cursor.moveToNext()){
            SongsModel songData = new SongsModel(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    Uri.parse(cursor.getString(7)),
                    cursor.getString(8),
                    cursor.getString(9),
                    cursor.getString(10)
            );
            if (new File(songData.getPath()).exists()){
                songList.add(songData);
            }
        }
        cursor.close();
    }

    private void setContentIds(){
        mainBody = findViewById(R.id.songs_screen_main_body);
        header = findViewById(R.id.songs_screen_header);
        headerText = findViewById(R.id.songs_screen_header_text);
        backButton = findViewById(R.id.songs_screen_back_button);
        searchButton = findViewById(R.id.songs_screen_search_button);
        toolBar = findViewById(R.id.songs_screen_tool_bar);
        sortButton = findViewById(R.id.songs_screen_sort_button);
        shufflePlayAll = findViewById(R.id.songs_screen_shuffle_play_all);
        recyclerView = findViewById(R.id.songs_screen_recycler);
        headerImage = findViewById(R.id.songs_screen_header_image);

        setClickListeners();
    }

    private void setClickListeners(){
        backButton.setOnClickListener(view -> onBackPressed());
        searchButton.setOnClickListener(view -> {
            SearchScreen.loadToSearchFromList(songList);
            startActivity(new Intent(getApplicationContext(), SearchScreen.class));
        });
        sortButton.setOnClickListener(view ->  sortSongsList());
        shufflePlayAll.setOnClickListener(view -> shufflePlayAll());
    }

    private void sortSongsList(){

    }

    private void shufflePlayAll(){
        SongsModel songData = songList.get(new Random().nextInt(songList.size()));
        MusicPlayerScreen.getMusicPlayerScreen().loadSong(songData, songList.lastIndexOf(songData));
        MusicPlayerService.CURRENT_PLAY_MODE = MusicPlayerService.PLAY_MODE_SHUFFLE;
        UserSettings.setPlayMode(MusicPlayerService.PLAY_MODE_SHUFFLE, getApplicationContext());
        MusicPlayerService.buildShuffleQueue(songList);
        MusicPlayerService.updateSongsList(songList, -1);
    }

    @SuppressLint("SetTextI18n")
    private void bindRecycler(){
        if (songList.size() > 0){
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            if (usingPLayList){
                recyclerView.setAdapter(new PlaylistsSongsAdapter(songList, playlist));
            }else {
                recyclerView.setAdapter(new SongsAdapter(songList));
            }
        }
    }

    private void setHeaderImage(){
        if (usingAlbums){
            useFirst();
        }else if (usingArtist){
            if (Integer.parseInt(artist.getAlbums()) == 1){
                useFirst();
            }else {
                useRandom();
            }
        }else if (usingPLayList){
            useRandom();
        }
    }

    private void useFirst(){
        Utils.with(getApplicationContext())
                .load(songList.getFirst().getThumbnail())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .build()
                .resize(1000, 600)
                .drawable();
        headerImage.setBackground(Utils.getDrawable());
        Palette palette = Palette.from(Utils.getBitmap())
                .generate();
        int darkenColor = Utils.getDarkenColor(palette.getDarkVibrantColor(getColor(R.color.colorPrimary)),
                getColor(R.color.colorPrimary), 0.51f);
        int brightColor = Utils.getDarkenColor(palette.getDarkVibrantColor(getColor(R.color.colorPrimaryVariant)),
                getColor(R.color.colorPrimaryVariant), 0.2f);
        mainBody.setBackgroundColor(darkenColor);
        header.setBackgroundColor(darkenColor);
        toolBar.setBackgroundColor(brightColor);
        setStatusBarColor(darkenColor);
    }

    private void useRandom(){
        Utils.with(getApplicationContext())
                .load(songList.get(MusicPlayerService.random.nextInt(songList.size())).getThumbnail())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .build()
                .resize(1000, 600);
        Palette palette = Palette.from(Utils.getBitmap())
                .generate();
        int darkenColor = Utils.getDarkenColor(palette.getDarkVibrantColor(getColor(R.color.colorPrimary)),
                getColor(R.color.colorPrimary), 0.51f);
        int brightColor = Utils.getDarkenColor(palette.getDarkVibrantColor(getColor(R.color.colorPrimaryVariant)),
                getColor(R.color.colorPrimaryVariant), 0.2f);
        mainBody.setBackgroundColor(darkenColor);
        header.setBackgroundColor(darkenColor);
        toolBar.setBackgroundColor(brightColor);
        headerImage.setBackgroundResource(R.drawable.vibe_star_logo_transparent_bg);
        setStatusBarColor(darkenColor);
    }

    public void setStatusBarColor(int color){
        getWindow().setStatusBarColor(color);
    }
}