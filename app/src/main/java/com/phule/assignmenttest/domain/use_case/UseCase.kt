package com.phule.assignmenttest.domain.use_case

import javax.inject.Inject

data class UseCase @Inject constructor(
    val fetchContentUseCase: FetchContentUseCase
)