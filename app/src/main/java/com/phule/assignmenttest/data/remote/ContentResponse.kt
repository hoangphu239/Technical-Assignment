package com.phule.assignmenttest.data.remote

import com.google.gson.annotations.SerializedName
import com.phule.assignmenttest.domain.model.Content
import java.util.UUID

data class ContentResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("video") val video: Video,
    @SerializedName("images") val images: List<Image>
)

fun ContentResponse.toContentList(): List<Content> {
    val contentList = mutableListOf<Content>()
    video.let { video ->
        contentList.add(Content(id = UUID.randomUUID().toString(), images = null, videos = video))
    }

    images.forEach { image ->
        contentList.add(Content(id = UUID.randomUUID().toString(), images = image, videos = null))
    }

    return contentList
}