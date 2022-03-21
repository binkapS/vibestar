package ng.com.binkap.vibestar.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import ng.com.binkap.vibestar.models.SongsModel;


public class PlaylistsData extends Playlists {

    public PlaylistsData(Context context, String playlist) {
        super(context);
        this.playlist = playlist;
    }

    public boolean addToPlaylist(SongsModel song){
        ContentValues contentValues = new ContentValues();
        contentValues.put("path", song.getPath());
        contentValues.put("id", song.getId());
        contentValues.put("title", song.getTitle());
        contentValues.put("artist", song.getArtist());
        contentValues.put("album", song.getAlbum());
        contentValues.put("size", song.getSize());
        contentValues.put("mimeType", song.getMimeType());
        contentValues.put("thumbnail", song.getThumbnail().toString());
        contentValues.put("dateAdded", song.getDateAdded());
        contentValues.put("lastModified", song.getLastModified());
        contentValues.put("duration", song.getDuration());
        updatePlaylist(playlist, 1, true);
        return this.getWritableDatabase().insert(playlist, null, contentValues) != FAILED_QUERY;
    }

    public boolean removeFromPlaylist(SongsModel song){
        if (existsInPlaylist(song) && updatePlaylist(playlist, 1, false)){
            return this.getWritableDatabase().delete(playlist, "path = ?", new String[]{song.getPath()}) != FAILED_QUERY;
        }
        return false;
    }

    public boolean existsInPlaylist(SongsModel song){
        Cursor cursor = getPlaylistData();
        while (cursor.moveToNext()){
            if (cursor.getString(2).equals(song.getTitle()) && cursor.getString(3).equals(song.getArtist())){
                return true;
            }
        }
        return false;
    }

    public Cursor getPlaylistData(){
        return this.getReadableDatabase().rawQuery("select * from " + playlist, null);
    }
}
