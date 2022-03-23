package ng.com.binkap.vibestar.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.database.PlaylistsData;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.screens.MusicControlScreen;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class PlaylistsSongsAdapter extends RecyclerView.Adapter<PlaylistsSongsAdapter.ViewHolder> {

    LinkedList<SongsModel> songList;

    private boolean musicListUpdated;

    public static final int REMOVE = R.id.playlist_remove_option;

    public static final int FILE_INFO = R.id.playlist_file_info;

    String playlist;

    public PlaylistsSongsAdapter(LinkedList<SongsModel> songList, String playlist) {
        this.songList = new LinkedList<>(songList);
        this.playlist = playlist;
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
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view, Gravity.END);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popupMenu.setForceShowIcon(true);
            }
            popupMenu.inflate(R.menu.playlist_options_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == REMOVE){
                    PlaylistsData playlistsData = new PlaylistsData(view.getContext(), playlist);
                    if (playlistsData.existsInPlaylist(songData) && playlistsData.removeFromPlaylist(songData)){
                        songList.remove(songData);
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                }else {
                    item.getItemId();
                }
                return true;
            });
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
