package com.phule.assignmenttest.data.remote.model

import com.google.gson.annotations.SerializedName

data class AdResponse(
    @SerializedName("images") val images: List<Image>
)

