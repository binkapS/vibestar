package ng.com.binkap.vibestar.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.models.SongsModel;
import ng.com.binkap.vibestar.screens.MusicControlScreen;
import ng.com.binkap.vibestar.screens.MusicPlayerScreen;
import ng.com.binkap.vibestar.services.MusicPlayerService;

public class ControlScreenAdapter extends RecyclerView.Adapter<ControlScreenAdapter.ViewHolder> {

    LinkedList<SongsModel> songList = new LinkedList<>();

    public ControlScreenAdapter(LinkedList<SongsModel> songs) {
        songList.clear();
        songList.addAll(songs);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.music_control_screen_linear_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongsModel songData = songList.get(position);
        holder.songName.setText(songData.getTitle());
        holder.songArtist.setText(songData.getArtist());
        holder.itemView.setOnClickListener(view -> sendSong(songData, position));
        holder.removeButton.setOnClickListener(view -> {
            songList.remove(holder.getAdapterPosition());
            MusicControlScreen.updatePlayList(songList);
            notifyItemRemoved(holder.getAdapterPosition());
            MusicPlayerService.updateSongsList(songList, songList.indexOf(songData));
        });
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(songData.getThumbnail())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.artCover);
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    private void sendSong(SongsModel songData, int songPosition){
        MusicPlayerScreen.getMusicPlayerScreen().loadSong(songData, songPosition);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView artCover, removeButton;

        TextView songName, songArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artCover = itemView.findViewById(R.id.control_screen_recycler_art_cover);
            removeButton = itemView.findViewById(R.id.control_screen_recycler_remove_button);
            songName = itemView.findViewById(R.id.control_screen_recycler_song_name);
            songArtist = itemView.findViewById(R.id.control_screen_recycler_song_artist);
        }
    }
}
