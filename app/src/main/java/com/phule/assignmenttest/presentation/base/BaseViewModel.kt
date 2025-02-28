package com.phule.assignmenttest.presentation.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


abstract class BaseViewModel : ViewModel() {

    private val handler = CoroutineExceptionHandler { _, exception ->
        handleError(HandelError.getError(exception))
    }

    open fun handleError(error: Pair<ErrorType, String>) {}

    protected fun safeLaunch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(handler, block = block)
    }
}
