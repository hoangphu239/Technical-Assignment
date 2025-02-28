package com.phule.assignmenttest.data.remote

import androidx.compose.runtime.Immutable

@Immutable
data class Image(
    val id: String,
    val url: String,
    val tag_align: String?,
    val price: String?
)