package ng.com.binkap.vibestar.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import ng.com.binkap.vibestar.helpers.Universal;

public class Playlists extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "ng.com.binkap.vibestar.database.VIBE_STAR_DATABASE";

    public static final int DATABASE_VERSION = 1;

    public static final int FAILED_QUERY = -1;

    public static final String PLAYLIST_TABLE_NAME = "Playlists";

    public String playlist;

    public Playlists(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("create Table "+ Universal.FAVORITE_PLAYLIST +" (path TEXT primary key, id TEXT, title TEXT, artist TEXT, album TEXT, size TEXT, mimeType TEXT, thumbnail TEXT, dateAdded TEXT, lastModified TEXT, duration TEXT)");
        database.execSQL("create Table "+ PLAYLIST_TABLE_NAME +" (name TEXT primary key, count INT)");
    }

    protected void createNewPlaylistTable(SQLiteDatabase database){
        if (playlist != null){
            database.execSQL("create Table "+ playlist +" (path TEXT primary key, id TEXT, title TEXT, artist TEXT, album TEXT, size TEXT, mimeType TEXT, thumbnail TEXT, dateAdded TEXT, lastModified TEXT, duration TEXT)");
        }
    }

    protected void dropPlaylistTable(SQLiteDatabase database){
        if (playlist != null){
            database.execSQL("drop Table if exists " + playlist);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1) {
        database.execSQL("drop Table if exists " + Universal.FAVORITE_PLAYLIST);
        database.execSQL("drop Table if exists " + PLAYLIST_TABLE_NAME);
    }

    public boolean createPlaylist(String name){
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("count", 0);
        return this.getWritableDatabase().insert(PLAYLIST_TABLE_NAME, null, contentValues) != FAILED_QUERY;
    }

    public boolean updatePlaylist(String name, int count, boolean increaseCount){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("Select count from " + PLAYLIST_TABLE_NAME + " where name = ?", new String[]{name});
        if (cursor.moveToNext()) {
            ContentValues contentValues = new ContentValues();
            if (increaseCount){
                contentValues.put("count", cursor.getInt(0) + count);
            }else {
                contentValues.put("count", cursor.getInt(0) - count);
            }
            cursor.close();
            database.update(PLAYLIST_TABLE_NAME, contentValues, "name=?", new String[]{name});
            return database.update(PLAYLIST_TABLE_NAME, contentValues, "name=?", new String[]{name}) != FAILED_QUERY;
        }
        cursor.close();
        return false;
    }

    public boolean deletePlaylist(String name){
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery("select * from " + PLAYLIST_TABLE_NAME + " where name = ?", new String[]{name});
        if (cursor.moveToNext()){
            cursor.close();
           return  database.delete(PLAYLIST_TABLE_NAME, "name = ?", new String[]{name}) != FAILED_QUERY;
        }
        cursor.close();
        return false;
    }

    public int getPlaylistCount(String playlist){
        Cursor cursor = this.getReadableDatabase().rawQuery("select count from " + PLAYLIST_TABLE_NAME + " where name = ?", new String[]{playlist});
        if (cursor.moveToNext()){
            return cursor.getInt(0);
        }
        cursor.close();
        return -1;
    }

    public Cursor getPLayLists(){
        return this.getReadableDatabase().rawQuery("Select * from " + PLAYLIST_TABLE_NAME, null);
    }

    public boolean playlistExists(String name){
        Cursor cursor = getPLayLists();
        while (cursor.moveToNext()){
            if (cursor.getString(0).equals(name)){
                cursor.close();
                return true;
            }
        }
        cursor.close();
        return false;
    }
}
