package ng.com.binkap.vibestar.listeners;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    private int SWIPE_THRESHOLD;

    private int SWIPE_VELOCITY_THRESHOLD;

    public OnTouchListener(Context context, int swipeThreshold, int swipeVelocity) {
        this.gestureDetector = new GestureDetector(context, new GestureListener());
        this.SWIPE_THRESHOLD = swipeThreshold;
        this.SWIPE_VELOCITY_THRESHOLD = swipeVelocity;
    }

    public OnTouchListener(Context context) {
        this.gestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return gestureDetector.onTouchEvent(motionEvent);
    }

    public void onSwipeUp() {
    }

    public void onSwipeDown(){
    }

    public void onSwipeRight(){
    }

    public void onSwipeLeft(){
    }

    public boolean doubleTaped(){
        return false;
    }

    public void onTap(){
    }

    public void onLongClick(){
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return doubleTaped();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            onTap();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            onLongClick();
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)){
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD){
                    if (diffX > 0){
                        onSwipeRight();
                    }else {
                        onSwipeLeft();
                    }
                    return true;
                }
            }else {
                if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD){
                    if (diffY > 0){
                        onSwipeDown();
                    }else {
                        onSwipeUp();
                    }
                    return true;
                }
            }
            return false;
        }
    }
}
