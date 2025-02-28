package com.phule.assignmenttest.data.repository

import android.content.Context
import com.phule.assignmenttest.R
import com.phule.assignmenttest.data.remote.AdResponse
import com.phule.assignmenttest.data.remote.Image
import com.phule.assignmenttest.data.remote.ContentResponse
import com.phule.assignmenttest.data.remote.toContentList
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.repository.Repository
import com.phule.assignmenttest.presentation.utils.AppUtils
import javax.inject.Inject


class RepositoryImpl @Inject constructor(
    private val context: Context,
) : Repository {

    override suspend fun fetchContent(page: Int): List<Content> {
        val response = loadNextPage(page) ?: return emptyList()
        return response.toContentList()
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
            else -> R.raw.next
        }
        val jsonString = AppUtils.loadJsonFromRaw(context, nextPageId)
        return AppUtils.parseJson(jsonString, ContentResponse::class.java)
    }
}