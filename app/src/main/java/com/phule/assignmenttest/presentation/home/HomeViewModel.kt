package com.phule.assignmenttest.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.phule.assignmenttest.data.remote.model.Video
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.use_case.UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCase: UseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _dataState = MutableStateFlow<PagingData<Content>>(PagingData.empty())
    val dataState: StateFlow<PagingData<Content>>
        get() = _dataState

    private val videoPositions = mutableMapOf<String, Long>()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    private var currentPlayingVideoId: String? = null

    init {
        getLocalData()
    }

    private fun getLocalData(isPullRefreshed: Boolean = false) {
        viewModelScope.launch {
            useCase.fetchContentUseCase(isPullRefreshed).cachedIn(viewModelScope).collect {
                _dataState.emit(it)
            }
        }
    }

    fun refreshLocalData() {
        videoPositions.clear()
        getLocalData(isPullRefreshed = true)
        exoPlayer.seekToDefaultPosition()
    }

    fun getSharedPlayer(video: Video, index: Int): ExoPlayer {
        if (currentPlayingVideoId != video.id) {
            exoPlayer.apply {
                if (!videoPositions.containsKey(video.id)) {
                    addMediaItem(index, MediaItem.fromUri(video.url))
                    prepare()
                }
                seekTo(index, getPosition(video.id))
            }
            currentPlayingVideoId = video.id
        }
        return exoPlayer
    }


    fun pausePlayer() {
        exoPlayer.playWhenReady = false
    }

    fun resumePlayer() {
        exoPlayer.playWhenReady = true
    }

    fun savePosition(videoId: String, position: Long) {
        videoPositions[videoId] = position
    }

    private fun getPosition(videoId: String): Long {
        return videoPositions[videoId] ?: 0L
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}
