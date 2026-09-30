package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

/**
 * How the search bar enters and leaves, shared by every screen that has one.
 *
 * The offset is `-it`, a full height rather than the default `-it / 2`: at half a height the bar
 * appears already halfway down and the first part of the movement is missing, which reads as a
 * jump rather than as something sliding in from above.
 *
 * Uses Material 3 motion physics with ease-in/ease-out:
 * Natural spatial spring deceleration on entrance, clean swift dismissal on exit.
 */
val SearchBarEnter: EnterTransition =
    fadeIn(tween(durationMillis = 180, easing = FastOutSlowInEasing)) +
        slideInVertically(
            animationSpec =
                spring(
                    dampingRatio = 0.82f,
                    stiffness = 380f,
                ),
        ) { -it }

/** Leaving is quicker than arriving: a control being dismissed should not hold the eye. */
val SearchBarExit: ExitTransition =
    fadeOut(tween(durationMillis = 140, easing = FastOutSlowInEasing)) +
        slideOutVertically(
            animationSpec =
                spring(
                    dampingRatio = 1.0f,
                    stiffness = 500f,
                ),
        ) { -it }
