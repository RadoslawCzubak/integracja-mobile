package pl.rczubak.stripetest.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<STATE : State, EVENT : Event> : ViewModel() {
    private val initialState by lazy { initialState() }

    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    abstract fun initialState(): STATE

    fun setEvent(event: EVENT) {
        handleEvent(event)
    }

    abstract fun handleEvent(event: EVENT)

    fun setState(changeState: (old: STATE) -> STATE) {
        _uiState.value = changeState(_uiState.value)
    }
}