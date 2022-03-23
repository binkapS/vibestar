package ng.com.binkap.vibestar.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.core.graphics.ColorUtils;

import java.io.IOException;

public class Utils {

    static Bitmap bitmap;

    Context context;

    int resourceReplaceId;

    Uri uri;

    static Drawable drawable;

    public Utils(Context context) {
        this.context = context;
    }

    public static Utils with(Context context){
        return new Utils(context);
    }

    public Utils load(Uri uri){
        this.uri = uri;
        return this;
    }

    public Utils error(int resourceId){
        resourceReplaceId = resourceId;
        return this;
    }

    public Utils build(){
        if (checkBuildBitMap(context, uri)){
            buildBitmapFromResource();
        }
        return this;
    }

    public Utils resize(int width, int height){
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return this;
    }

    public Utils drawable(){
        drawable = new BitmapDrawable(context.getResources(), bitmap);
        return this;
    }

    public static int getDarkenColor(int color1, int color2, float fraction){
        return ColorUtils.blendARGB(color1, color2, fraction);
    }

    public Utils into(ImageView imageView){
        imageView.setImageBitmap(getBitmap());
        return this;
    }

    private void buildBitmapFromResource(){
        bitmap = BitmapFactory.decodeResource(context.getResources(), resourceReplaceId);
    }

    public static boolean checkBuildBitMap(Context context, Uri uri){
        bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
            return false;
        } catch (IOException ioException) {
            Log.v("Bitmap", ioException.getLocalizedMessage());
        }
        return true;
    }

    public static Bitmap getBitmap(){
        return bitmap;
    }

    public static Drawable getDrawable(){
        return drawable;
    }
}
