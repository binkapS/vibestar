package ng.com.binkap.vibestar.fragments;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.ArtistsAdapter;
import ng.com.binkap.vibestar.models.ArtistsModel;

public class ArtistsFragment extends Fragment {

    String[] artistsProjection = {
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS
    };

    Cursor cursor;

    public static LinkedList<ArtistsModel> artistsList = new LinkedList<>();

    RecyclerView recyclerView;

    TextView noItemsFound;

    SwipeRefreshLayout swipeRefreshLayout;

    public static boolean ARTISTS_LIST_INITIALIZED = false;

    public ArtistsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    public static ArtistsFragment newInstance() {
        ArtistsFragment fragment = new ArtistsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentIds(view);
        if (!ARTISTS_LIST_INITIALIZED) {
            scanLoadArtists();
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
        ARTISTS_LIST_INITIALIZED = true;
    }

    private void setContentIds(View view){
      recyclerView = view.findViewById(R.id.artists_fragment_recycler);
      noItemsFound = view.findViewById(R.id.artists_fragment_no_item_found);
      swipeRefreshLayout = view.findViewById(R.id.artists_fragment_swipe_refresh);

      swipeRefreshLayout.setOnRefreshListener(() -> {
          swipeRefreshLayout.setRefreshing(true);
          scanLoadArtists();
          bindRecycler();
          swipeRefreshLayout.setRefreshing(false);
      });
    }

    private void scanLoadArtists(){
        artistsList.clear();
        cursor = requireActivity().getContentResolver().query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                artistsProjection, null, null, MediaStore.Audio.Artists.DEFAULT_SORT_ORDER);
        while (cursor.moveToNext()){
            String artist = cursor.getString(1).equals("<unknown>")
                    ? getResources().getString(R.string.unknown_artist) : cursor.getString(1);
            ArtistsModel artistData = new ArtistsModel(
                    cursor.getString(0),
                    artist,
                    cursor.getString(2),
                    cursor.getString(3)
            );
            artistsList.add(artistData);
        }
        cursor.close();
    }

    protected void bindRecycler(){
        if (artistsList.size() > 0){
            noItemsFound.setVisibility(View.GONE);
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(new ArtistsAdapter());
        }else {
            noItemsFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}