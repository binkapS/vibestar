package ng.com.binkap.vibestar.helpers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.animation.TranslateAnimation;

public class Transitions {

    public static void translate(View view, int fromXDelta, int toXDelta, int fromYDelta, int toYDelta, int visibility){
        TranslateAnimation animation = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        animation.setDuration(300);
        view.startAnimation(animation);
        view.setVisibility(visibility);
    }

    public static void reveal(View revealView , int animationDuration){
        revealView.setAlpha(0f);
        revealView.setVisibility(View.VISIBLE);
        revealView.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null);
    }

    public static void hide(View hideView, int animationDuration){
        hideView.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });
    }

    public static void crossFade(View hideView, View revealView, int animationDuration){
        hideView.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        hideView.setVisibility(View.GONE);
                    }
                });

        revealView.setAlpha(0f);
        revealView.setVisibility(View.VISIBLE);
        revealView.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .setListener(null);
    }

}
