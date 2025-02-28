package com.phule.assignmenttest.presentation.base

import org.json.JSONObject
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException

object HandelError {
    fun getError(throwable: Throwable): Pair<ErrorType, String> {
        Timber.e(throwable)
        if (throwable is IOException ||
            throwable is SocketTimeoutException ||
            throwable is UnknownHostException ||
            throwable is ConnectException
        ) {
            return Pair(ErrorType.INTERNET, throwable.localizedMessage ?: "Couldn't reach database")
        } else {
            if (throwable is HttpException) {
                try {
                    val jsonObject =
                        JSONObject(throwable.response()?.errorBody()?.string()?:"")
                    if (jsonObject.has("status_message")) {
                        val mgs = jsonObject.getString("status_message")
                        if (mgs.isNotEmpty()) {
                            return Pair(ErrorType.FROM_API, mgs)
                        }
                    }
                } catch (_: Exception) {
                }
                return Pair(
                    ErrorType.UNKNOW,
                    throwable.localizedMessage ?: "An unexpected error occurred"
                )
            } else if(throwable is CancellationException) {
                return Pair(
                    ErrorType.CANCELLATION,
                    throwable.localizedMessage ?: "An unexpected error occurred"
                )
            }
            return Pair(ErrorType.UNKNOW, "")
        }
    }
}