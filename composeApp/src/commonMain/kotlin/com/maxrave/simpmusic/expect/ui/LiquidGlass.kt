package com.maxrave.simpmusic.expect.ui

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * The backdrop a glass surface samples from.
 */
typealias PlatformBackdrop = LayerBackdrop

/** Marks a composable as the source layer that sibling surfaces sample. */
fun Modifier.layerBackdrop(backdrop: PlatformBackdrop): Modifier = this

@Composable
fun rememberBackdrop(color: Color): PlatformBackdrop =
    rememberLayerBackdrop { }

fun Modifier.drawBackdropCustomShape(
    backdrop: PlatformBackdrop,
    layer: GraphicsLayer,
    luminanceAnimation: Float,
    shape: Shape,
): Modifier =
    this
        .clip(shape)
        .background(if (luminanceAnimation < 0.5f) Color(0xFF2B2B2B) else Color(0xFFE8E8E8))
