package com.phule.assignmenttest.data.remote.model

import androidx.compose.runtime.Immutable

@Immutable
data class Image(
    val id: String,
    val url: String,
    val tag_align: String?,
    val price: String?
)