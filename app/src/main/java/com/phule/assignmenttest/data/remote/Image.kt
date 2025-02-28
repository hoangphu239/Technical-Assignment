package com.phule.assignmenttest.data.remote

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
data class Image(
    val id: String,
    val url: String,
    val tag_align: String?,
    val price: String?
)