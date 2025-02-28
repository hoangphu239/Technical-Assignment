package com.phule.assignmenttest.domain.repository

import com.phule.assignmenttest.data.remote.Image
import com.phule.assignmenttest.domain.model.Content


interface Repository {
    suspend fun fetchContent(page: Int): List<Content>
    suspend fun fetchAdvertisement(): List<Image>
}