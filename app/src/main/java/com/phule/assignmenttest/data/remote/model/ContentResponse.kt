package com.phule.assignmenttest.data.remote.model

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
        contentList.add(Content(id = UUID.randomUUID().toString(), image = null, video = video))
    }

    images.forEach { image ->
        contentList.add(Content(id = UUID.randomUUID().toString(), image = image, video = null))
    }

    return contentList
}