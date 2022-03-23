package ng.com.binkap.vibestar.adapters;


import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.database.PlaylistsData;
import ng.com.binkap.vibestar.helpers.Universal;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.screens.MusicControlScreen;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.ViewHolder> {

    LinkedList<SongsModel> songList;

    public static final int PLAY_NOW = R.id.music_options_play_now;

    public static final int PLAY_NEXT = R.id.music_options_play_next;

    public static final int ADD_TO_QUEUE = R.id.music_options_add_to_queue;

    public static final int SET_AS_RINGTONE = R.id.music_options_set_ring_tone;

    public static final int ADD_TO_PLAYLIST = R.id.music_options_add_to_playList;

    public static final  int FAVORITE = R.id.music_options_favorite;

    public static final int DELETE = R.id.music_options_delete;

    public static final int FILE_INFO = R.id.music_options_file_info;

    private boolean musicListUpdated;

    public SongsAdapter(LinkedList<SongsModel> songList) {
        this.songList = new LinkedList<>(songList);
        musicListUpdated = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.local_music_linear_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongsModel songData = songList.get(position);
        holder.songName.setText(songData.getTitle());
        holder.artistAlbumName.setText(songData.getArtist().concat(" - ").concat(songData.getAlbum()));
        holder.optionsButton.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(), view, Gravity.END);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true);
            }
            popupMenu.inflate(R.menu.music_options_pop_up_menu);
            popupMenu.setOnMenuItemClickListener(menuItem -> menuClickListener(menuItem, songData, holder.getAdapterPosition(), holder));
            popupMenu.show();
        });
        holder.itemView.setOnClickListener(view -> sendSong(songData, position, view.getContext()));
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(songData.getThumbnail())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.songThumbNail);
    }

    private boolean menuClickListener(MenuItem item, SongsModel songData, int songPosition, ViewHolder holder){
        switch (item.getItemId()){
            case ADD_TO_QUEUE:
                MusicPlayerService.queuedSongs.offer(songData);
                Toast.makeText(holder.itemView.getContext(), "Song Added to Queue", Toast.LENGTH_SHORT).show();
                break;
            case PLAY_NOW:
                sendSong(songData, songPosition, holder.itemView.getContext());
                break;
            case PLAY_NEXT:
                MusicPlayerService.playNextSongIndex = songPosition;
                MusicPlayerService.PLAY_NEXT_ADDED = true;
                Toast.makeText(holder.itemView.getContext(), "Song will Play Next", Toast.LENGTH_SHORT).show();
                break;
            case SET_AS_RINGTONE:

                break;
            case ADD_TO_PLAYLIST:

                break;
            case FAVORITE:
                addToFavorite(songData ,holder.itemView.getContext());
                break;
            case DELETE:
                deleteItem(holder, songData, songPosition);
                break;
            case FILE_INFO:

                break;
        }
        return true;
    }

    private void addToFavorite(SongsModel songData, Context context){
        PlaylistsData playlistsData = new PlaylistsData(context, Universal.FAVORITE_PLAYLIST);
        if (!playlistsData.playlistExists(Universal.FAVORITE_PLAYLIST)){
            playlistsData.createPlaylist(Universal.FAVORITE_PLAYLIST);
        }
        if (!playlistsData.existsInPlaylist(songData) && playlistsData.addToPlaylist(songData)){
            Toast.makeText(context, songData.getTitle().concat(" Added to ").concat(Universal.FAVORITE_PLAYLIST), Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(context, "Already Exists ".concat(Universal.FAVORITE_PLAYLIST), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendSong(SongsModel songData, int songPosition, Context context){
        MusicPlayerScreen.getMusicPlayerScreen().loadSong(songData, songPosition);
        if (!musicListUpdated){
            MusicPlayerService.updateSongsList(songList, -1);
            MusicControlScreen.setSongInfo(songData);
            MusicControlScreen.updatePlayList(songList);
            context.startActivity(new Intent(context, MusicControlScreen.class));
            musicListUpdated = true;
        }
    }

    private void deleteItem(ViewHolder holder, SongsModel songData, int songPosition){
        new MaterialAlertDialogBuilder(holder.itemView.getContext())
                .setTitle("Delete")
                .setIcon(R.drawable.ic_round_delete_24)
                .setMessage("Permanently Delete this file ?")
                .setPositiveButton("Proceed", (dialogInterface, i) -> {
                    File file = new File(songData.getPath());
                    if (file.delete()){
                        Toast.makeText(holder.itemView.getContext(), "File Deleted Successfully", Toast.LENGTH_SHORT).show();
                        songList.remove(songPosition);
                        MusicPlayerScreen.allSongs.remove(songData);
                        notifyItemRemoved(songPosition);
                        MusicPlayerService.updateSongsList(songList, songPosition);
                    }
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.cancel())
                .create().show();
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView songThumbNail, optionsButton;

        TextView songName, artistAlbumName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            songThumbNail = itemView.findViewById(R.id.player_screen_linear_music_icon);
            songName = itemView.findViewById(R.id.player_screen_linear_music_song_name);
            artistAlbumName = itemView.findViewById(R.id.player_screen_linear_music_song_artist);
            optionsButton = itemView.findViewById(R.id.player_screen_linear_music_play_button);
        }
    }
}
