package com.phule.assignmenttest.presentation.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.transform

abstract class DataState<T> {
    class Success<T>(val data: T) : DataState<T>()
    class Error<T>(val error: Pair<ErrorType, String>) : DataState<T>()
    class Loading<T> : DataState<T>()
    class InitState<T> : DataState<T>()

    inline fun <R : Any> map(transform: (T) -> R): DataState<R> {
        return when (this) {
            is Error -> Error(this.error)
            is Success -> Success(transform(this.data))
            else -> {
                Loading()
            }
        }
    }

    suspend inline fun <R : Any> suspendMap(crossinline transform: suspend (T) -> R): DataState<R> {
        return when (this) {
            is Error -> Error(this.error)
            is Success -> Success(transform(this.data))
            else -> {
                Loading()
            }
        }
    }
}

inline fun <T : Any, R : Any> Flow<DataState<T>>.mapState(
    crossinline transform: suspend (value: T) -> R
): Flow<DataState<R>> = transform { value ->
    return@transform emit(value.suspendMap(transform))
}
