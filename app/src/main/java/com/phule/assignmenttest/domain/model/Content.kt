package com.phule.assignmenttest.domain.model

import com.phule.assignmenttest.data.remote.Image
import com.phule.assignmenttest.data.remote.Video

data class Content(
    val id: String,
    val images: Image?,
    val videos: Video?,
)
