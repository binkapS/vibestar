package ng.com.binkap.vibestar.fragments;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.AlbumsAdapter;
import ng.com.binkap.vibestar.models.AlbumsModel;

public class AlbumsFragment extends Fragment {

    String[] albumsProjection = {
            MediaStore.Audio.Albums.ALBUM_ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
    };

    Cursor cursor;

    public static LinkedList<AlbumsModel> albumsList = new LinkedList<>();

    RecyclerView recyclerView;

    TextView noItemsFound;

    SwipeRefreshLayout swipeRefreshLayout;

    public static boolean ALBUMS_LIST_INITIALIZED = false;

    public AlbumsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_albums, container, false);
    }

    public static AlbumsFragment newInstance() {
        AlbumsFragment fragment = new AlbumsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentIds(view);
        if (!ALBUMS_LIST_INITIALIZED) {
            scanLoadAlbums();
        }
        bindRecycler();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        ALBUMS_LIST_INITIALIZED = true;
    }

    private void setContentIds(View view){
        recyclerView = view.findViewById(R.id.albums_fragment_recycler);
        noItemsFound = view.findViewById(R.id.albums_fragment_no_item_found);
        swipeRefreshLayout = view.findViewById(R.id.albums_fragment_swipe_refresh);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            scanLoadAlbums();
            bindRecycler();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void scanLoadAlbums(){
        albumsList.clear();
        cursor = requireActivity().getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                albumsProjection, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        while (cursor.moveToNext()){
            String artist = cursor.getString(2).equals("<unknown>")
            ? getResources().getString(R.string.unknown_artist) : cursor.getString(2);
            Uri uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), Long.parseLong(cursor.getString(0)));
            AlbumsModel albumData = new AlbumsModel(
                    cursor.getString(0),
                    cursor.getString(1),
                    artist,
                    cursor.getString(3),
                    uri
            );
            albumsList.add(albumData);
        }
        cursor.close();
    }

    protected void bindRecycler(){
        if (albumsList.size() > 0){
            noItemsFound.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
            recyclerView.setAdapter(new AlbumsAdapter());
        }else{
            noItemsFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}