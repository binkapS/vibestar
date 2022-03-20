package ng.com.binkap.vibestar.adapters;

import android.content.Context;
import android.content.Intent;
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
import ng.com.binkap.vibestar.fragments.AlbumsFragment;
import ng.com.binkap.vibestar.models.AlbumsModel;
import ng.com.binkap.vibestar.screens.sub.SongsScreen;

public class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.ViewHolder> {

    LinkedList<AlbumsModel> albums = AlbumsFragment.albumsList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.albums_grid_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlbumsModel albumData = albums.get(position);
        holder.albumName.setText(albumData.getName());
        holder.albumArtist.setText(albumData.getArtist());
        holder.itemView.setOnClickListener(view -> loadSongs(albumData, view.getContext()));
        Glide.with(holder.itemView.getContext())
                .asBitmap()
                .load(albumData.getAlbumArt())
                .error(R.drawable.vibe_star_logo_transparent_bg)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(holder.albumCover);
    }

    private void loadSongs(AlbumsModel albumData, Context context){
        SongsScreen.setValues(albumData, null, null,true, false, false);
        context.startActivity(new Intent(context, SongsScreen.class));
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCover;

        TextView albumName, albumArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCover = itemView.findViewById(R.id.albums_grid_cover);
            albumName = itemView.findViewById(R.id.albums_grid_album_name);
            albumArtist = itemView.findViewById(R.id.albums_grid_album_artist);
        }
    }
}
