/**
 * ***************************************************************************
 * Copyright (C) 2015  EngrZhou,engrzhou@gmail.com                            *
 * *
 * See file CREDITS for list of people who contributed to this project.       *
 * *
 * This program is free software; you can redistribute it and/or              *
 * modify it under the terms of the GNU General Public License                *
 * as published by the Free Software Foundation; either version 2             *
 * of the License, or (at your option) any later version.                     *
 * *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 * *
 * You should have received a copy of the GNU General Public License          *
 * along with this program; if not, write to the Free Software                *
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * ****************************************************************************
 */

package com.bbtree.cardreader.view.anim;

import android.view.View;
import android.view.animation.Interpolator;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by xTools Team
 * User:       EngrZhou
 * Date:       2015/04/01
 * Time:       上午11:41
 * Description:
 */
public abstract class BaseAnimator<T> {

    //Sets the length of the animation.
    private static long DURATION = 1000;
    private AnimatorSet mAnimatorSet;

    public AnimatorSet getmAnimatorSet() {
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
        }
        return mAnimatorSet;
    }

    /**
     * Get the length of the animation.
     *
     * @return The length of the animation, in milliseconds.
     */
    public long getDuration() {
        return DURATION;
    }

    /**
     * Sets the length of the animation.
     *
     * @param DURATION The length of the animation, in milliseconds.
     */
    public BaseAnimator setDuration(long DURATION) {
        BaseAnimator.DURATION = DURATION;
        return this;
    }

    protected abstract BaseAnimator<? extends BaseAnimator> build(View target);

    /**
     * start animation
     */
    public void start() {
        mAnimatorSet.setDuration(DURATION);
        mAnimatorSet.start();
    }

    /**
     * cancel animation
     */
    public void cancel() {
        mAnimatorSet.cancel();
    }

    /**
     * end animation
     */
    public void end() {
        mAnimatorSet.end();
    }

    /**
     * is running
     *
     * @return the animation is running,in boolean
     */
    public boolean isRunning() {
        return mAnimatorSet.isRunning();
    }

    /**
     * is started
     *
     * @return the animation is started,in boolean
     */
    public boolean isStarted() {
        return mAnimatorSet.isStarted();
    }


    /**
     * Adds a listener to the set of listeners that are sent events through the life of an
     * animation, such as start, repeat, and end.
     *
     * @param listener the listener to be added to the current set of listeners for this animation.
     */
    public BaseAnimator addAnimListener(Animator.AnimatorListener listener) {
        mAnimatorSet.addListener(listener);
        return this;
    }

    /**
     * Removes a listener from the set listening to this animation.
     *
     * @param listener the listener to be removed from the current set of listeners for this
     *                 animation.
     */
    public void removeAnimatorListener(Animator.AnimatorListener listener) {
        mAnimatorSet.removeListener(listener);
    }

    /**
     * Removes all listeners from this object. This is equivalent to calling
     * <code>getListeners()</code> followed by calling <code>clear()</code> on the
     * returned list of listeners.
     */
    public void removeAllListener() {
        mAnimatorSet.removeAllListeners();
    }

    /**
     * Sets the TimeInterpolator for all current {@link #getChildAnimations() child animations}
     * of this AnimatorSet.
     *
     * @param interpolator the interpolator to be used by each child animation of this AnimatorSet
     */
    public BaseAnimator setInterpolator(Interpolator interpolator) {
        mAnimatorSet.setInterpolator(interpolator);
        return this;
    }

    /**
     * Returns the current list of child Animator objects controlled by this
     * AnimatorSet. This is a copy of the internal list; modifications to the returned list
     * will not affect the AnimatorSet, although changes to the underlying Animator objects
     * will affect those objects being managed by the AnimatorSet.
     *
     * @return ArrayList<Animator> The list of child animations of this AnimatorSet.
     */
    public ArrayList<Animator> getChildAnimations() {
        return mAnimatorSet.getChildAnimations();
    }

    /**
     * Sets up this AnimatorSet to play all of the supplied animations at the same time.
     *
     * @param items The animations that will be started simultaneously.
     */
    public BaseAnimator playTogether(Animator... items) {
        mAnimatorSet.playTogether(items);
        return this;
    }

    /**
     * Sets up this AnimatorSet to play all of the supplied animations at the same time.
     *
     * @param items The animations that will be started simultaneously.
     */
    public BaseAnimator playTogether(Collection<Animator> items) {
        mAnimatorSet.playTogether(items);
        return this;
    }

    /**
     * Sets up this AnimatorSet to play each of the supplied animations when the
     * previous animation ends.
     *
     * @param items The animations that will be started one after another.
     */
    public BaseAnimator playSequentially(Animator... items) {
        mAnimatorSet.playSequentially(items);
        return this;
    }

    /**
     * Sets up this AnimatorSet to play each of the supplied animations when the
     * previous animation ends.
     *
     * @param items The animations that will be started one after another.
     */
    public BaseAnimator playSequentially(List<Animator> items) {
        mAnimatorSet.playSequentially(items);
        return this;
    }

    /**
     * The amount of time, in milliseconds, to delay starting the animation after
     * {@link #start()} is called.
     *
     * @return the number of milliseconds to delay running the animation
     */
    public long getStartDelay() {
        return mAnimatorSet.getStartDelay();
    }

    /**
     * The amount of time, in milliseconds, to delay starting the animation after
     * {@link #start()} is called.
     *
     * @param startDelay The amount of the delay, in milliseconds
     */
    public BaseAnimator setStartDelay(long startDelay) {
        mAnimatorSet.setStartDelay(startDelay);
        return this;
    }

    /**
     * Reset the view to default status
     *
     * @param target target view
     */
    public void reset(View target) {
        ViewHelper.setAlpha(target, 1);
        ViewHelper.setScaleX(target, 1);
        ViewHelper.setScaleY(target, 1);
        ViewHelper.setTranslationX(target, 0);
        ViewHelper.setTranslationY(target, 0);
        ViewHelper.setRotation(target, 0);
        ViewHelper.setRotationY(target, 0);
        ViewHelper.setRotationX(target, 0);
        ViewHelper.setPivotX(target, target.getMeasuredWidth() / 2.0f);
        ViewHelper.setPivotY(target, target.getMeasuredHeight() / 2.0f);
    }


}
