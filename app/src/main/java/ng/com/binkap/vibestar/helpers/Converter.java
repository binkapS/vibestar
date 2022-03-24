package ng.com.binkap.vibestar.helpers;

import android.annotation.SuppressLint;

import java.util.concurrent.TimeUnit;

public class Converter {

    @SuppressLint("DefaultLocale")
    public static String convertTime(String time){
        String value;
        long millis = 1L;
        try {
            millis = Long.parseLong(time);
        }catch (NumberFormatException ignored){

        }
        if (millis >= 3600000){
            value = String.format("%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        }else {
            value = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                    TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
        }
        return value;
    }

    public static String convertSize(String size){
        return size;
    }
}
