package com.phule.assignmenttest.domain.model

import androidx.compose.runtime.Immutable
import com.phule.assignmenttest.data.remote.Image
import com.phule.assignmenttest.data.remote.Video

@Immutable
data class Content(
    val id: String,
    val images: Image?,
    val videos: Video?,
)
