package ng.com.binkap.vibestar.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.database.Playlists;
import ng.com.binkap.vibestar.models.PlaylistsModel;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.screens.MusicControlScreen;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;
import ng.com.binkap.vibestar.screens.sub.SongsScreen;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.ViewHolder> {

    List<PlaylistsModel> playlists;

    boolean musicListUpdated;

    public PlaylistsAdapter(List<PlaylistsModel> list) {
        playlists = new ArrayList<>(list);
        musicListUpdated = false;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.playlist_linear_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlaylistsModel playlist = playlists.get(position);
            holder.playListName.setText(playlist.getName());
            if (playlist.getCount() == 1){
                holder.playListItemCount.setText(String.valueOf(playlist.getCount()).concat(" Song"));
            }else {
                holder.playListItemCount.setText(String.valueOf(playlist.getCount()).concat(" Songs"));
            }
            holder.playListPLayButton.setOnClickListener(view -> {
                if (playlist.getCount() > 0){
                    sendSong(playlist.getName(), view.getContext());
                }else {
                    Toast.makeText(view.getContext(), playlist.getName().concat(" is empty"), Toast.LENGTH_SHORT).show();
                }
            });
            holder.itemView.setOnClickListener(view -> {
                if (playlist.getCount() > 0){
                    loadSongs(playlist.getName(), view.getContext());
                }else {
                    Toast.makeText(view.getContext(), playlist.getName().concat(" is empty"), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void sendSong(String playlist, Context context){
        SongsScreen.buildPlaylistSongs(playlist, context);
        SongsModel songData = SongsScreen.songList.getFirst();
        MusicPlayerScreen.getMusicPlayerScreen().loadSong(songData, SongsScreen.songList.indexOf(songData));
        if (!musicListUpdated){
            MusicPlayerService.updateSongsList(SongsScreen.songList, -1);
            MusicControlScreen.setSongInfo(songData);
            MusicControlScreen.updatePlayList(SongsScreen.songList);
            context.startActivity(new Intent(context, MusicControlScreen.class));
            musicListUpdated = true;
        }
    }

    private void loadSongs(String playlist, Context context){
        SongsScreen.setValues(null, null, playlist, false, false, true);
        context.startActivity(new Intent(context, SongsScreen.class));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        TextView playListName, playListItemCount;

        ImageView playListImage, playListPLayButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            playListName = itemView.findViewById(R.id.playlist_recycler_playlist_name);
            playListItemCount = itemView.findViewById(R.id.playlist_recycler_songs_count);
            playListImage = itemView.findViewById(R.id.playlist_recycler_image);
            playListPLayButton = itemView.findViewById(R.id.playlist_recycler_play_icon);
        }
    }
}
