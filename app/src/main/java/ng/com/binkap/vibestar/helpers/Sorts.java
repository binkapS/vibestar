package ng.com.binkap.vibestar.helpers;

import android.content.Context;
import android.icu.text.Collator;

import java.util.List;

import ng.com.binkap.vibestar.models.SongsModel;

public class Sorts {

    public static final int SORT_ASCENDING = 534063537;

    public static final int SORT_DESCENDING = 583097967;

    public static final String SORT_BY_TITLE = "ng.com.binkap.vibestar.SORT_BY_TITLE";

    public static final String SORT_BY_SIZE = "ng.com.binkap.vibestar.SORT_BY_SIZE";

    public static final String SORT_BY_DATE = "ng.com.binkap.vibestar.SORT_BY_DATE";

    public static final String SORT_BY_DURATION = "ng.com.binkap.vibestar.SORT_BY_DURATION";

    public static void sortSongsList(List<SongsModel> list, String sortBy, int sortOrder, Context context){
        switch (sortBy){
            case Sorts.SORT_BY_TITLE:
                if (sortOrder == Sorts.SORT_DESCENDING){
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songsModel.getTitle(), songModel.getTitle()));
                }else {
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songModel.getTitle(), songsModel.getTitle()));
                }
                break;
            case Sorts.SORT_BY_DATE:
                if (sortOrder == Sorts.SORT_DESCENDING){
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songsModel.getDateAdded(), songModel.getDateAdded()));
                }else {
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songModel.getDateAdded(), songsModel.getDateAdded()));
                }
                break;
            case Sorts.SORT_BY_SIZE:
                if (sortOrder == Sorts.SORT_DESCENDING){
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songsModel.getSize(), songModel.getSize()));
                }else {
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songModel.getSize(), songsModel.getSize()));
                }
                break;
            case Sorts.SORT_BY_DURATION:
                if (sortOrder == Sorts.SORT_DESCENDING){
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songsModel.getDuration(), songModel.getDuration()));
                }else {
                    list.sort((songsModel, songModel) -> Collator.getInstance().compare(songModel.getDuration(), songsModel.getDuration()));
                }
                break;
        }
        UserSettings.setSongsSortBy(sortBy, context);
        UserSettings.setSongsSortOrder(sortOrder, context);
    }

}
