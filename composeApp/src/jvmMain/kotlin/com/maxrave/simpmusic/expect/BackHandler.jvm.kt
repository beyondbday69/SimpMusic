package com.maxrave.simpmusic.expect

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onBack: () -> Unit,
) {
    // Desktop has no system back button
}
