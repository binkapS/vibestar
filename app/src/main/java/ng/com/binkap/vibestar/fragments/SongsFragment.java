package ng.com.binkap.vibestar.fragments;

import android.annotation.SuppressLint;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Random;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.adapters.SongsAdapter;
import ng.com.binkap.vibestar.helpers.Sorts;
import ng.com.binkap.vibestar.helpers.UserSettings;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class SongsFragment extends Fragment {

    RecyclerView recyclerView;

    MaterialCardView toolBar;

    TextView noItemsFound, shuffleNo;

    ImageView shufflePlayIcon, sortIcon;

    SwipeRefreshLayout swipeRefreshLayout;

    int sortByActiveId;

    String sortByValue;

    public SongsFragment() {
        // Required empty public constructor
    }

    public static SongsFragment newInstance() {
        SongsFragment fragment = new SongsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContentIds(view);
        Sorts.sortSongsList(MusicPlayerScreen.allSongs, UserSettings.getSongsSortBy(getContext()),
                UserSettings.getSongsSortOrder(getContext()), getContext());
        bindRecycler();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        applySettings();
    }

    protected void setContentIds(View view){
        recyclerView = view.findViewById(R.id.songs_fragment_recycler);
        noItemsFound = view.findViewById(R.id.songs_fragment_no_item_found);
        toolBar = view.findViewById(R.id.songs_fragment_tool_bar);
        sortIcon = view.findViewById(R.id.songs_fragment_tool_bar_sort);
        shufflePlayIcon = view.findViewById(R.id.songs_fragment_tool_bar_play);
        shuffleNo = view.findViewById(R.id.songs_fragment_tool_shuffle_text);
        swipeRefreshLayout = view.findViewById(R.id.songs_fragment_swipe_refresh);

        setClickListeners();
    }

    protected void setClickListeners(){
        shufflePlayIcon.setOnClickListener(view -> shufflePlayIconClicked());
        shuffleNo.setOnClickListener(view -> shufflePlayIconClicked());
        sortIcon.setOnClickListener(view -> sortIconClicked());
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            MusicPlayerScreen.getMusicPlayerScreen().scanLoadMusic();
            Sorts.sortSongsList(MusicPlayerScreen.allSongs, UserSettings.getSongsSortBy(getContext()),
                    UserSettings.getSongsSortOrder(getContext()), getContext());
            bindRecycler();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void sortIconClicked(){
        switch (UserSettings.getSongsSortBy(getContext())){
            case Sorts.SORT_BY_DATE:
                sortByActiveId = 1;
                break;
            case Sorts.SORT_BY_SIZE:
                sortByActiveId = 2;
                break;
            case Sorts.SORT_BY_DURATION:
                sortByActiveId = 3;
                break;
            default: sortByActiveId = 0;
        }
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sort by")
                .setSingleChoiceItems(new String[]{
                        "Name",
                        "Date",
                        "Size",
                        "Duration"
                }, sortByActiveId, (dialogInterface, i) -> {
                    switch (i){
                        case 1:
                            sortByValue = Sorts.SORT_BY_DATE;
                            break;
                        case 2:
                            sortByValue = Sorts.SORT_BY_SIZE;
                            break;
                        case 3:
                            sortByValue = Sorts.SORT_BY_DURATION;
                            break;
                        default: sortByValue = Sorts.SORT_BY_TITLE;
                    }
                })
                .setNegativeButton("Descending", (dialogInterface, i) -> {
                    if (sortByValue == null){
                        sortByValue = UserSettings.getSongsSortBy(getContext());
                    }
                    Sorts.sortSongsList(MusicPlayerScreen.allSongs, sortByValue, Sorts.SORT_DESCENDING, getContext());
                    bindRecycler();
                    dialogInterface.dismiss();
                })
                .setPositiveButton("Ascending", (dialogInterface, i) -> {
                    if (sortByValue == null){
                        sortByValue = UserSettings.getSongsSortBy(getContext());
                    }
                    Sorts.sortSongsList(MusicPlayerScreen.allSongs, sortByValue, Sorts.SORT_ASCENDING, getContext());
                    bindRecycler();
                    dialogInterface.dismiss();
                })
                .setNeutralButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .create().show();
    }

    private void shufflePlayIconClicked(){
        SongsModel songData = MusicPlayerScreen.allSongs.get(new Random().nextInt(MusicPlayerScreen.allSongs.size()));
        MusicPlayerScreen.getMusicPlayerScreen().loadSong(songData, MusicPlayerScreen.allSongs.lastIndexOf(songData));
        MusicPlayerService.CURRENT_PLAY_MODE = MusicPlayerService.PLAY_MODE_SHUFFLE;
        UserSettings.setPlayMode(MusicPlayerService.PLAY_MODE_SHUFFLE, getContext());
        MusicPlayerService.buildShuffleQueue(MusicPlayerScreen.allSongs);
        MusicPlayerService.updateSongsList(MusicPlayerScreen.allSongs, -1);
    }

    @SuppressLint("SetTextI18n")
    public void bindRecycler(){
        if (MusicPlayerScreen.allSongs.size() > 0){
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerView.setAdapter(new SongsAdapter(MusicPlayerScreen.allSongs));
            MusicPlayerService.updateSongsList(MusicPlayerScreen.allSongs, -1);
            noItemsFound.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            toolBar.setVisibility(View.VISIBLE);
            shuffleNo.setText("Shuffle All".concat(" (".concat(String.valueOf(MusicPlayerScreen.allSongs.size())).concat(")")));
        }else {
            noItemsFound.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            toolBar.setVisibility(View.GONE);
        }
    }

    private void applySettings(){
        int colorPrimaryVariant = UserSettings.getColorPrimaryVariant(getContext());

        toolBar.setCardBackgroundColor(colorPrimaryVariant);
    }
}