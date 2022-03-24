package ng.com.binkap.vibestar.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.PlaylistsAdapter;
import ng.com.binkap.vibestar.database.Playlists;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.models.PlaylistsModel;


public class PlayListsFragment extends Fragment {

    RecyclerView recyclerView;

    TextView noItemsFound, playlistCount;

    ImageView addPLayListButton;

    MaterialCardView header;

    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<PlaylistsModel> playlists;

    public PlayListsFragment() {

    }

    public static PlayListsFragment newInstance() {
        PlayListsFragment fragment = new PlayListsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_lists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentIds(view);
        buildPlaylist();
        bindRecycler();
    }

    @Override
    public void onResume() {
        super.onResume();
        applySettings();
    }

    private void buildPlaylist(){
        playlists = new ArrayList<>();
        Cursor cursor = new Playlists(getContext()).getPLayLists();
        while (cursor.moveToNext()){
            PlaylistsModel playlistsModel = new PlaylistsModel(cursor.getString(0),
                    cursor.getInt(1));
            playlists.add(playlistsModel);
        }
        cursor.close();
    }

    private void setContentIds(View view){
        recyclerView = view.findViewById(R.id.play_list_fragment_recycler);
        noItemsFound = view.findViewById(R.id.play_list_fragment_no_items_found);
        playlistCount = view.findViewById(R.id.play_list_fragment_no_of_playlist);
        addPLayListButton = view.findViewById(R.id.play_list_fragment_add_playlist_button);
        header = view.findViewById(R.id.play_list_fragment_header_options);
        swipeRefreshLayout = view.findViewById(R.id.play_list_fragment_swipe_refresh);

        setClickListeners();
    }

    private void setClickListeners(){
        addPLayListButton.setOnClickListener(view -> addPLayListClicked());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            buildPlaylist();
            bindRecycler();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void addPLayListClicked(){

    }

    @SuppressLint("SetTextI18n")
    private void bindRecycler(){
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(new PlaylistsAdapter(playlists));
        playlistCount.setText("Playlist (".concat(String.valueOf(playlists.size())).concat(")"));
    }

    private void applySettings(){
        int colorPrimaryVariant = UserSettings.getColorPrimaryVariant(getContext());

        header.setCardBackgroundColor(colorPrimaryVariant);
    }
}