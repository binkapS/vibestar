package ng.com.binkap.vibestar.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    }

    private void setContentIds(View view){
        recyclerView = view.findViewById(R.id.play_list_fragment_recycler);
        noItemsFound = view.findViewById(R.id.play_list_fragment_no_items_found);
        playlistCount = view.findViewById(R.id.play_list_fragment_no_of_playlist);
        addPLayListButton = view.findViewById(R.id.play_list_fragment_add_playlist_button);
        header = view.findViewById(R.id.play_list_fragment_header_options);

        setClickListeners();
    }

    private void setClickListeners(){
        addPLayListButton.setOnClickListener(view -> addPLayListClicked());
    }

    private void addPLayListClicked(){
        String name = "Recently Played";
        if (new Playlists(requireContext()).createPlaylist(name)){
            bindRecycler();
            Toast.makeText(getContext(), name + " Created Successfully", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getContext(), name + " Already Exists", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void bindRecycler(){
        PlaylistsAdapter adapter = new PlaylistsAdapter(playlists);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        playlistCount.setText("Playlist (".concat(String.valueOf(adapter.getItemCount())).concat(")"));
    }

    private void applySettings(){
        int colorPrimaryVariant = UserSettings.getColorPrimaryVariant(getContext());

        header.setCardBackgroundColor(colorPrimaryVariant);
    }
}