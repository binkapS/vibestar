package ng.com.binkap.vibestar.models;

public class PlaylistsModel {

    String name;

    int count;

    public PlaylistsModel(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }
}
