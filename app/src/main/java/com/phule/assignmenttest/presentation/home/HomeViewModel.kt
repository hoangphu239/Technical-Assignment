package com.phule.assignmenttest.presentation.home

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phule.assignmenttest.data.remote.Video
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.use_case.UseCase
import com.phule.assignmenttest.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.WeakHashMap
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCase: UseCase,
) : BaseViewModel() {

    private val _dataState = MutableStateFlow<PagingData<Content>>(PagingData.empty())
    val dataState: StateFlow<PagingData<Content>>
        get() = _dataState

    private val playerCache = WeakHashMap<String, ExoPlayer>()
    private val playerStates = WeakHashMap<String, Boolean>()
    private val videoPositions = mutableMapOf<String, Long>()

    init {
        getLocalData()
    }

    private fun getLocalData(startPage: Int = 1) {
        safeLaunch {
            useCase.fetchContentUseCase(startPage).cachedIn(viewModelScope).collect {
                _dataState.emit(it)
            }
        }
    }

    fun refreshLocalData() {
        viewModelScope.launch {
            _dataState.emit(PagingData.empty())
            videoPositions.clear()
            delay(100)
            getLocalData(startPage = 0)
        }
    }

    fun getPlayer(context: Context, video: Video): ExoPlayer {
        return playerCache.getOrPut(video.id) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(video.url))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
                seekTo(getPosition(video.id))
                playWhenReady = playerStates[video.id] ?: false
            }
        }
    }

    fun pauseAllPlayers() {
        playerCache.values.forEach {
            it.playWhenReady = false
        }
    }

    fun resumeAllPlayers() {
        playerCache.values.forEach {
            it.playWhenReady = true
        }
    }

    fun savePosition(videoId: String, position: Long) {
        videoPositions[videoId] = position
    }

    private fun getPosition(videoId: String): Long {
        return videoPositions[videoId] ?: 0L
    }

    fun releasePlayer(videoId: String) {
        playerCache[videoId]?.let {
            playerStates[videoId] = it.playWhenReady
            it.release()
        }
        playerCache.remove(videoId)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch(Dispatchers.IO) {
            playerCache.values.forEach { it.release() }
            playerCache.clear()
        }
    }
}