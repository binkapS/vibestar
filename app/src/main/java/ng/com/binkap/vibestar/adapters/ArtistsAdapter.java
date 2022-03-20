package ng.com.binkap.vibestar.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

import ng.com.binkap.vibestar.R;
import ng.com.binkap.vibestar.fragments.ArtistsFragment;
import ng.com.binkap.vibestar.models.ArtistsModel;
import ng.com.binkap.vibestar.screens.sub.SongsScreen;

public class ArtistsAdapter extends RecyclerView.Adapter<ArtistsAdapter.ViewHolder> {

    LinkedList<ArtistsModel> artists = ArtistsFragment.artistsList;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.artists_linear_recycler_view, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ArtistsModel artistData = artists.get(position);
        holder.artistName.setText(artistData.getName());
        String albums;
        if (Integer.decode(artistData.getAlbums()) == 1){
            albums = "Album";
        }else{
            albums = "Albums";
        }
        if (Integer.decode(artistData.getTracks()) == 1){
            holder.songsCount.setText(artistData.getTracks().concat(" Song - ")
                    .concat(artistData.getAlbums().concat(" ").concat(albums)));
        }else {
            holder.songsCount.setText(artistData.getTracks().concat(" Songs - ")
                    .concat(artistData.getAlbums().concat(" ").concat(albums)));
        }
        holder.itemView.setOnClickListener(view -> loadSongs(artistData, view.getContext()));
    }

    private void loadSongs(ArtistsModel artistData, Context context){
        SongsScreen.setValues(null, artistData, null, false, true, false);
        context.startActivity(new Intent(context, SongsScreen.class));
    }

    @Override
    public int getItemCount() {
        return artists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView artistName, songsCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            artistName = itemView.findViewById(R.id.artists_linear_artist_name);
            songsCount = itemView.findViewById(R.id.artists_linear_artist_songs_count);
        }
    }
}
