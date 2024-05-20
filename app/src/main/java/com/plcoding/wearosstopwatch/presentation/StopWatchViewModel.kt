@file:OptIn(ExperimentalCoroutinesApi::class)

package com.plcoding.wearosstopwatch.presentation

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class StopWatchViewModel: ViewModel() {

    private val _elapsedTime = MutableStateFlow(0L)
    private val _timerState = MutableStateFlow(TimerState.RESET)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS")
    val stopWatchText = _elapsedTime
        .map { millis ->
            LocalTime.ofNanoOfDay(millis * 1_000_000).format(formatter)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            "00:00:00:000"
        )

    // Set a timer limit (e.g., 1 minute in milliseconds)
    private val timerLimitInMillis: Long = 60000

    init {
        _timerState
            .flatMapLatest { timerState ->
                getTimerFlow(
                    isRunning = timerState == TimerState.RUNNING
                )
            }
            .onEach { timeDiff ->
                _elapsedTime.update { it + timeDiff }
                if (_elapsedTime.value >= timerLimitInMillis) {
                    // Notify when the timer completes
                    onTimerFinish()
                }
            }
            .launchIn(viewModelScope)
    }

    fun toggleIsRunning() {
        when(timerState.value) {
            TimerState.RUNNING -> _timerState.update { TimerState.PAUSED }
            TimerState.PAUSED,
            TimerState.RESET -> _timerState.update { TimerState.RUNNING }
        }
    }

    fun resetTimer() {
        _timerState.update { TimerState.RESET }
        _elapsedTime.update { 0L }
    }

    private fun getTimerFlow(isRunning: Boolean): Flow<Long> {
        return flow {
            var startMillis = System.currentTimeMillis()
            while (isRunning) {
                val currentMillis = System.currentTimeMillis()
                val timeDiff = if (currentMillis > startMillis) {
                    currentMillis - startMillis
                } else 0L
                emit(timeDiff)
                startMillis = System.currentTimeMillis()
                delay(10L)
            }
        }
    }

    private fun onTimerFinish() {
        _timerState.update { TimerState.RESET }
        _elapsedTime.update { 0L }
        // Call to send a notification (you will need to pass context)
        viewModelScope.launch {
            sendNotification()
        }
    }

    suspend fun sendNotification() {
        // Implementation to be handled in MainActivity
    }
}
