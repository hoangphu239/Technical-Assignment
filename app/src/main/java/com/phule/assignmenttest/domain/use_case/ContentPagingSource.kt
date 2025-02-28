package com.phule.assignmenttest.domain.use_case

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.repository.Repository
import com.phule.assignmenttest.presentation.utils.AppUtils
import javax.inject.Inject

class ContentPagingSource @Inject constructor(
    private val repository: Repository,
    private val startPage: Int = 1
) : PagingSource<Int, Content>() {

    override fun getRefreshKey(state: PagingState<Int, Content>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Content> {
        return try {
            val page = params.key ?: startPage
            var imageIndex = 0

            if (page > 2) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }

            val contentList = repository.fetchContent(page)
            val adList = repository.fetchAdvertisement()

            val combinedList = contentList.mapIndexed { index, content ->
                if (index >= 2 && AppUtils.isFibonacci(index - 1) && imageIndex < adList.size) {
                    content.copy(images = adList[imageIndex++])
                } else {
                    content
                }
            }

            LoadResult.Page(
                data = combinedList,
                prevKey = if (page == startPage) null else page - 1,
                nextKey = page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}