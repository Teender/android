package com.pluscubed.crush.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link AnimatorChangeHandler} that will slide either slide a new View up or slide an old View down,
 * depending on whether a push or pop change is happening.
 */
public class MimicActivityChangeHandler extends AnimatorChangeHandler {

    private Context context;

    public MimicActivityChangeHandler() {
    }

    public MimicActivityChangeHandler(Context context) {
        this.context = context;
    }

    @Override
    @NonNull
    protected Animator getAnimator(@NonNull ViewGroup container, @Nullable View from, @Nullable View to, boolean isPush, boolean toAddedToContainer) {
        AnimatorSet animator = new AnimatorSet();
        List<Animator> viewAnimators = new ArrayList<>();

        if (isPush && to != null) {
            AnimatorSet set = new AnimatorSet();

            Animator alpha = ObjectAnimator.ofFloat(to, View.ALPHA, 0, 1).setDuration(200);
            alpha.setInterpolator(new DecelerateInterpolator(2.5f));

            Animator translation = ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, getYFraction(0.08f), 0).setDuration(350);
            translation.setInterpolator(new DecelerateInterpolator(2.5f));

            to.setBackgroundColor(Color.parseColor("#FAFAFA"));

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    to.setBackground(null);
                }
            });

            set.playTogether(alpha, translation);

            // viewAnimators.add(ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, to.getHeight(), 0));
            viewAnimators.add(set);
        } else if (!isPush && from != null) {
            AnimatorSet set = new AnimatorSet();

            Animator alpha = ObjectAnimator.ofFloat(from, View.ALPHA, 1f, 0f).setDuration(150);
            alpha.setInterpolator(new LinearInterpolator());

            Animator translation = ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, 0, getYFraction(0.08f)).setDuration(250);
            translation.setInterpolator(new AccelerateInterpolator(2.5f));

            from.setBackgroundColor(Color.parseColor("#FAFAFA"));

            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    from.setBackground(null);
                }
            });

            set.playTogether(alpha, translation);

            viewAnimators.add(ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, from.getHeight()));
        }

        animator.playTogether(viewAnimators);
        return animator;
    }

    public float getYFraction(float yFraction) {
        final WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int height = wm.getDefaultDisplay().getHeight();
        return ((height > 0) ? (yFraction * height) : 0);
    }

    @Override
    protected void resetFromView(@NonNull View from) {
    }

}
