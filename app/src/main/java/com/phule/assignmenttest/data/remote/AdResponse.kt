package com.phule.assignmenttest.data.remote

import com.google.gson.annotations.SerializedName

data class AdResponse(
    @SerializedName("images") val images: List<Image>
)

