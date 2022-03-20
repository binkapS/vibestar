package ng.com.binkap.vibestar.models;

public class ArtistsModel {

    protected String id;

    protected String name;

    protected String tracks;

    protected String albums;

    public ArtistsModel(String id, String name, String tracks, String albums) {
        this.id = id;
        this.name = name;
        this.tracks = tracks;
        this.albums = albums;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTracks() {
        return tracks;
    }

    public String getAlbums() {
        return albums;
    }
}
