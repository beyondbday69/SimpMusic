package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalPlatformContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.ui.component.ExplicitBadge
import com.maxrave.simpmusic.ui.component.HeartCheckBox
import com.maxrave.simpmusic.ui.component.PlayerControlLayout
import com.maxrave.simpmusic.ui.icon.KeyboardArrowDown
import com.maxrave.simpmusic.ui.icon.Lyrics
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.PlaylistAdd
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.screen.LocalNowPlayingArtworkBounds
import com.maxrave.simpmusic.ui.screen.LocalNowPlayingMorphProgress
import com.maxrave.simpmusic.ui.screen.LocalNowPlayingTextBounds
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlinx.coroutines.isActive
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingContentVinyl(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val morphProgress = LocalNowPlayingMorphProgress.current
    val artworkBoundsHolder = LocalNowPlayingArtworkBounds.current
    val textBoundsHolder = LocalNowPlayingTextBounds.current

    val restingAlpha =
        if (morphProgress < 0.85f) 0f
        else if (morphProgress < 1f) ((morphProgress - 0.85f) / 0.15f).coerceIn(0f, 1f)
        else 1f

    var vinylRotation by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(state.controllerState.isPlay) {
        if (!state.controllerState.isPlay) return@LaunchedEffect
        var lastNanos = withFrameNanos { it }
        while (isActive) {
            withFrameNanos { frameTime ->
                val dt = (frameTime - lastNanos) / 1_000_000_000f
                vinylRotation = (vinylRotation + dt * 36f) % 360f
                lastNanos = frameTime
            }
        }
    }

    val tonearmAngle by animateFloatAsState(
        targetValue = if (state.controllerState.isPlay) 24f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "tonearm",
    )

    val dominantColor = state.startColor.value

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF141318),
                        0.4f to dominantColor.copy(alpha = 0.22f),
                        1.0f to Color(0xFF0C0B0E),
                    ),
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Bar
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { actions.onDismiss() }) {
                    Icon(
                        imageVector = SimpIcons.KeyboardArrowDown,
                        contentDescription = "Dismiss",
                        tint = Color.White,
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "VINYL TURNTABLE",
                        style = typo().labelSmall.copy(letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.6f),
                    )
                    Text(
                        text = "${state.currentOrderIndex + 1} / ${state.artworkQueue.size.coerceAtLeast(1)}",
                        style = typo().bodySmall.copy(fontSize = 11.sp),
                        color = Color.White.copy(alpha = 0.4f),
                    )
                }
                IconButton(onClick = { actions.onShowMoreSheet() }) {
                    Icon(
                        imageVector = SimpIcons.MoreVert,
                        contentDescription = "More",
                        tint = Color.White,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Turntable Container
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                // Vinyl LP Record Disc
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize(0.92f)
                            .aspectRatio(1f)
                            .rotate(vinylRotation),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val radius = size.minDimension / 2f

                        // Outer Vinyl Base
                        drawCircle(color = Color(0xFF111114), radius = radius, center = center)

                        // Fine Vinyl Micro-grooves
                        val labelRadius = radius * 0.42f
                        val step = 4.dp.toPx()
                        var r = labelRadius + 8.dp.toPx()
                        while (r < radius - 4.dp.toPx()) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.035f),
                                radius = r,
                                center = center,
                                style = Stroke(width = 1.dp.toPx()),
                            )
                            r += step
                        }

                        // Light Sheen Refraction (Dual Specular Arcs)
                        drawArc(
                            color = Color.White.copy(alpha = 0.055f),
                            startAngle = 40f,
                            sweepAngle = 45f,
                            useCenter = true,
                        )
                        drawArc(
                            color = Color.White.copy(alpha = 0.055f),
                            startAngle = 220f,
                            sweepAngle = 45f,
                            useCenter = true,
                        )

                        // Outer Rim Highlight
                        drawCircle(
                            color = Color.White.copy(alpha = 0.14f),
                            radius = radius - 1.dp.toPx(),
                            center = center,
                            style = Stroke(width = 1.5.dp.toPx()),
                        )
                    }

                    // Center Album Artwork Label (Registered for Morphing)
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize(0.42f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .alpha(restingArtworkAlpha)
                                .onGloballyPositioned { coordinates ->
                                    artworkBoundsHolder?.value = coordinates.boundsInRoot()
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        val artworkUrl = state.screenData.thumbnailURL
                        AsyncImage(
                            model =
                                ImageRequest.Builder(LocalPlatformContext.current)
                                    .data(artworkUrl)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .crossfade(false)
                                    .build(),
                            contentDescription = "Vinyl Label",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )

                        // Center Spindle Hole
                        Box(
                            modifier =
                                Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(Color.Transparent),
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = Color(0xFFC0C0C8),
                                        radius = size.minDimension / 2f - 1.dp.toPx(),
                                        style = Stroke(width = 2.dp.toPx()),
                                    )
                                }
                            }
                        }
                    }
                }

                // Turntable Tonearm (Top-Right)
                Canvas(
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(width = 110.dp, height = 150.dp)
                            .rotate(tonearmAngle),
                ) {
                    val pivot = Offset(size.width * 0.78f, size.height * 0.16f)
                    val elbow = Offset(size.width * 0.52f, size.height * 0.65f)
                    val head = Offset(size.width * 0.32f, size.height * 0.92f)

                    // Base pivot assembly
                    drawCircle(color = Color(0xFF333338), radius = 18.dp.toPx(), center = pivot)
                    drawCircle(color = Color(0xFF90909A), radius = 10.dp.toPx(), center = pivot)

                    // Metallic arm rod
                    drawLine(
                        color = Color(0xFFD4D4DC),
                        start = pivot,
                        end = elbow,
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                    drawLine(
                        color = Color(0xFFC0C0C8),
                        start = elbow,
                        end = head,
                        strokeWidth = 3.5.dp.toPx(),
                        cap = StrokeCap.Round,
                    )

                    // Cartridge headshell
                    drawCircle(color = Color(0xFFFF4081), radius = 4.dp.toPx(), center = head)
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Track Info Row (Registered for Morphing)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .alpha(restingTextAlpha)
                            .onGloballyPositioned { coordinates ->
                                textBoundsHolder?.value = coordinates.boundsInRoot()
                            },
                ) {
                    Text(
                        text = state.screenData.nowPlayingTitle,
                        style = typo().titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.screenData.isExplicit) {
                            ExplicitBadge(modifier = Modifier.size(18.dp).padding(end = 4.dp))
                        }
                        Text(
                            text = state.screenData.artistName,
                            style = typo().bodyMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable { actions.onNavigateToArtist() },
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                HeartCheckBox(checked = state.controllerState.isLiked, size = 32) {
                    actions.onUIEvent(UIEvent.ToggleLike)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Slider
            Slider(
                value = (state.sliderValue / 100f).coerceIn(0f, 1f),
                onValueChange = { actions.onSliderChange(it * 100f) },
                onValueChangeFinished = { actions.onSliderChangeFinished() },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFFFFB74D),
                        inactiveTrackColor = Color.White.copy(alpha = 0.16f),
                    ),
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        thumbSize = DpSize(12.dp, 12.dp),
                        colors = SliderDefaults.colors(thumbColor = Color.White),
                    )
                },
            )

            // Duration Labels
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = formatDuration((state.timelineState.total * (state.sliderValue / 100f)).roundToLong()),
                    style = typo().bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
                Text(
                    text = formatDuration(state.timelineState.total),
                    style = typo().bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls
            PlayerControlLayout(state.controllerState) {
                actions.onUIEvent(it)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Action Utilities
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { actions.onShowQueue() }) {
                    Icon(imageVector = SimpIcons.QueueMusic, contentDescription = "Queue", tint = Color.White.copy(alpha = 0.7f))
                }
                IconButton(onClick = { actions.onShowFullscreenLyrics() }) {
                    Icon(imageVector = SimpIcons.Lyrics, contentDescription = "Lyrics", tint = Color.White.copy(alpha = 0.7f))
                }
                IconButton(onClick = { actions.onShowAddToPlaylist() }) {
                    Icon(imageVector = SimpIcons.PlaylistAdd, contentDescription = "Add to playlist", tint = Color.White.copy(alpha = 0.7f))
                }
            }
        }
    }
}
