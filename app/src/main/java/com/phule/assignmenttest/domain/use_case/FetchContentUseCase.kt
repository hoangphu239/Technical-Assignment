package com.phule.assignmenttest.domain.use_case

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.phule.assignmenttest.domain.model.Content
import com.phule.assignmenttest.domain.repository.Repository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

const val PAGE_SIZE = 20

class FetchContentUseCase @Inject constructor(
    private val repository: Repository,
) {
    operator fun invoke(startPage: Int = 1): Flow<PagingData<Content>> =
        Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                prefetchDistance = 20,
                initialLoadSize = PAGE_SIZE
            ),
            pagingSourceFactory = {
                ContentPagingSource(repository, startPage)
            },
        ).flow
}