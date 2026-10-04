package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicatorDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CenterLoadingBox(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    indicatorColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    var containerShape by remember {
        mutableStateOf(LoadingIndicatorDefaults.IndeterminateIndicatorPolygons.first())
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            containerShape = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons.random()
            delay(500)
        }
    }

    Box(modifier = modifier) {
        Crossfade(
            containerShape,
            modifier =
                Modifier
                    .wrapContentSize()
                    .align(Alignment.Center),
        ) { shape ->
            ContainedLoadingIndicator(
                modifier =
                    Modifier
                        .size(size),
                polygons = LoadingIndicatorDefaults.IndeterminateIndicatorPolygons,
                containerColor = containerColor,
                indicatorColor = indicatorColor,
                containerShape = shape.toShape(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CenterLoadingPullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    maxDistance: Dp = PullToRefreshDefaults.PositionalThreshold,
    size: Dp = 56.dp,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    indicatorColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val isVisible by remember(state, isRefreshing) {
        derivedStateOf { state.distanceFraction > 0f || isRefreshing }
    }

    Box(
        modifier =
            modifier
                .size(size)
                .drawWithContent {
                    clipRect(
                        top = 0f,
                        left = -Float.MAX_VALUE,
                        right = Float.MAX_VALUE,
                        bottom = Float.MAX_VALUE,
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.placeWithLayer(
                            0,
                            0,
                            layerBlock = {
                                val showElevation = state.distanceFraction > 0f || isRefreshing
                                translationY =
                                    state.distanceFraction * maxDistance.toPx() -
                                        placeable.height.toFloat()
                                shadowElevation = if (showElevation) 6.dp.toPx() else 0f
                                shape = CircleShape
                                clip = false
                            },
                        )
                    }
                },
        contentAlignment = Alignment.Center,
    ) {
        if (isVisible) {
            CenterLoadingBox(
                modifier = Modifier.size(size),
                size = size,
                containerColor = containerColor,
                indicatorColor = indicatorColor,
            )
        }
    }
}