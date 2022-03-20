package ng.com.binkap.vibestar.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class AlbumsModel {

    protected String id;

    protected String name;

    protected String artist;

    protected String count;

    protected Uri albumArt;

    public AlbumsModel(String id, String name, String artist, String count, Uri albumArt) {
        this.id = id;
        this.name = name;
        this.artist = artist;
        this.count = count;
        this.albumArt = albumArt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getCount() {
        return count;
    }

    public Uri getAlbumArt() {
        return albumArt;
    }
}
