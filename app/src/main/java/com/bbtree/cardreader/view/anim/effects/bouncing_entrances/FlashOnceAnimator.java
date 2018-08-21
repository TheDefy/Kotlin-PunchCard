/**
 * ***************************************************************************
 * Copyright (C) 2015  xTools Team                                            *
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

package com.bbtree.cardreader.view.anim.effects.bouncing_entrances;

import android.view.View;

import com.bbtree.cardreader.view.anim.BaseAnimator;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlashOnceAnimator extends BaseAnimator {
    @Override
    public FlashOnceAnimator build(View target) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(target, "alpha", 1, 0);
        getmAnimatorSet().playTogether(
                objectAnimator
        );
        return this;
    }
}
