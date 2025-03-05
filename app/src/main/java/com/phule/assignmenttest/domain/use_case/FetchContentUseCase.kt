package com.phule.assignmenttest.domain.use_case

import ContentPagingSource
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

const val PAGE_SIZE = 21

class FetchContentUseCase @Inject constructor(private val repository: Repository) {
    operator fun invoke(
        isPullRefreshed: Boolean = false
    ): Flow<PagingData<Content>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 10,
                initialLoadSize = PAGE_SIZE
            ),
            pagingSourceFactory = {
                ContentPagingSource(repository, isPullRefreshed)
            },
        ).flow
}