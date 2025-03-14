package com.phule.assignmenttest.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player

@Composable
fun PlayerListener(
    player: Player,
    onEvent: (Int) -> Unit
) {
    DisposableEffect(key1 = player) {
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() {
                super.onRenderedFirstFrame()
                onEvent(Player.EVENT_RENDERED_FIRST_FRAME)
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                onEvent(Player.EVENT_PLAYER_ERROR)
            }

            override fun onPlaybackStateChanged(state: Int) {
                super.onPlaybackStateChanged(state)
                onEvent(Player.EVENT_PLAYBACK_STATE_CHANGED)
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
        }
    }
}