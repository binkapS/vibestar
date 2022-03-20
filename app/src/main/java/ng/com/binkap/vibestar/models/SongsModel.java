package ng.com.binkap.vibestar.models;

import android.net.Uri;


public class SongsModel {

    protected String path;

    protected String id;

    protected String title;

    protected String artist;

    protected String album;

    protected String size;

    protected String mimeType;

    protected Uri thumbnail;

    protected String dateAdded;

    protected String lastModified;

    protected String duration;

    public SongsModel(String path, String id, String title, String artist, String album, String size, String mimeType, Uri thumbnail, String dateAdded, String lastModified, String duration) {
        this.path = path;
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.size = size;
        this.mimeType = mimeType;
        this.thumbnail = thumbnail;
        this.dateAdded = dateAdded;
        this.lastModified = lastModified;
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getSize() {
        return size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Uri getThumbnail() {
        return thumbnail;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getDuration() {
        return duration;
    }
}
