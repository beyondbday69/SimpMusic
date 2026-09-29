package com.maxrave.simpmusic.ui.screen.player.content.expressive

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.viewModel.SharedViewModel

/**
 * Seekable wavy progress bar for the M3 Expressive Now Playing style, built on material3's
 * [LinearWavyProgressIndicator].
 *
 * Design:
 * - Thick, modern continuous pill progress track with rounded ends and no dot/thumb indicator.
 * - Fluid expansion from 10dp to 12dp while dragging/scrubbing for tactile touch response.
 * - The wave amplitude animates to 1f while playing and flattens to 0f when paused or while
 *   scrubbing.
 *
 * @param progressFraction current playback progress in 0..1 (shell's sliderValue / 100f).
 * @param onSliderChange scrub callback on the 0..100 scale, called during drag and on tap.
 * @param onSliderChangeFinished called once when the interaction ends (commits the seek).
 */
@Composable
fun WavySeekBar(
    progressFraction: Float,
    isPlaying: Boolean,
    activeColor: Color,
    trackColor: Color,
    thumbColor: Color = Color.Unspecified,
    onSliderChange: (Float) -> Unit,
    onSliderChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    waveStyle: String = SharedViewModel.WAVE_STYLE_EXPRESSIVE,
) {
    var isInteracting by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    var widthPx by remember { mutableIntStateOf(0) }

    val displayedFraction = (if (isInteracting) dragFraction else progressFraction).coerceIn(0f, 1f)

    val targetAmplitude = when (waveStyle) {
        SharedViewModel.WAVE_STYLE_FLAT -> 0f
        SharedViewModel.WAVE_STYLE_GENTLE -> if (isPlaying && !isInteracting) 0.5f else 0f
        else -> if (isPlaying && !isInteracting) 1f else 0f
    }

    val density = LocalDensity.current
    val barStrokeWidth by animateDpAsState(
        targetValue = if (isInteracting) 12.dp else 10.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 600f),
        label = "wavySeekBarStrokeWidth",
    )
    val customStroke =
        remember(density, barStrokeWidth) {
            Stroke(
                width = with(density) { barStrokeWidth.toPx() },
                cap = StrokeCap.Round,
            )
        }

    fun fractionAt(x: Float): Float = if (widthPx <= 0) 0f else (x / widthPx).coerceIn(0f, 1f)

    Box(
        contentAlignment = Alignment.CenterStart,
        modifier =
            modifier
                .fillMaxWidth()
                // ~40dp hit area — comfortably taller than the wave itself for easy finger targeting.
                .height(40.dp)
                .onSizeChanged { widthPx = it.width }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val fraction = fractionAt(offset.x)
                        dragFraction = fraction
                        onSliderChange(fraction * 100f)
                        onSliderChangeFinished()
                    }
                }.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isInteracting = true
                            val fraction = fractionAt(offset.x)
                            dragFraction = fraction
                            onSliderChange(fraction * 100f)
                        },
                        onDragEnd = {
                            isInteracting = false
                            onSliderChangeFinished()
                        },
                        onDragCancel = {
                            isInteracting = false
                            onSliderChangeFinished()
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            val fraction = fractionAt(change.position.x)
                            dragFraction = fraction
                            onSliderChange(fraction * 100f)
                        },
                    )
                },
    ) {
        LinearWavyProgressIndicator(
            progress = { displayedFraction },
            color = activeColor,
            trackColor = trackColor,
            stroke = customStroke,
            trackStroke = customStroke,
            // No wave on the empty part of the track, and none at progress 0.
            amplitude = { p -> if (p > 0f) targetAmplitude else 0f },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
        )
    }
}
