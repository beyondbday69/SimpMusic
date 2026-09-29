package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
class HeartBurstState internal constructor(
    private val scope: CoroutineScope,
) {
    val scale = Animatable(1f)

    /** Tactile spring bounce on like tap */
    fun fire() {
        scope.launch {
            scale.animateTo(0.72f, tween(durationMillis = 60))
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
        }
    }
}

@Composable
fun rememberHeartBurstState(): HeartBurstState {
    val scope = rememberCoroutineScope()
    return remember { HeartBurstState(scope) }
}

@Composable
fun Modifier.heartBurst(
    state: HeartBurstState,
    colors: List<Color> = HeartBurstDefaults.colors,
): Modifier {
    return this.graphicsLayer {
        scaleX = state.scale.value
        scaleY = state.scale.value
    }
}

object HeartBurstDefaults {
    val colors: List<Color> = emptyList()
}
