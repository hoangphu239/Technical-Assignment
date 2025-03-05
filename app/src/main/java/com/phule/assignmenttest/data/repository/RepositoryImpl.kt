package com.phule.assignmenttest.data.repository

import android.content.Context
import com.phule.assignmenttest.R
import com.phule.assignmenttest.data.remote.model.AdResponse
import com.phule.assignmenttest.data.remote.model.ContentResponse
import com.phule.assignmenttest.data.remote.model.Image
import com.phule.assignmenttest.data.remote.model.toContentList
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.repository.Repository
import com.phule.assignmenttest.presentation.utils.AppUtils
import javax.inject.Inject


class RepositoryImpl @Inject constructor(
    private val context: Context
) : Repository {

    override suspend fun fetchContent(page: Int): List<Content> {
        val response = loadNextPage(page)
        return response?.toContentList() ?: emptyList()
    }

    override suspend fun fetchAdvertisement(): List<Image> {
        val jsonString = AppUtils.loadJsonFromRaw(context, R.raw.advertisement)
        val response = AppUtils.parseJson(jsonString, AdResponse::class.java)
        return response?.images ?: emptyList()
    }

    private fun loadNextPage(page: Int): ContentResponse? {
        val nextPageId = when (page) {
            0 -> R.raw.prev
            1 -> R.raw.current
            2 -> R.raw.next
            else -> R.raw.current
        }
        val jsonString = AppUtils.loadJsonFromRaw(context, nextPageId)
        return AppUtils.parseJson(jsonString, ContentResponse::class.java)
    }
}