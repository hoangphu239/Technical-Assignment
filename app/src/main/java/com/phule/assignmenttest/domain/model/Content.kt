package com.phule.assignmenttest.domain.model

import androidx.compose.runtime.Immutable
import com.phule.assignmenttest.data.remote.model.Image
import com.phule.assignmenttest.data.remote.model.Video

@Immutable
data class Content(
    val id: String,
    val image: Image?,
    val video: Video?,
)
