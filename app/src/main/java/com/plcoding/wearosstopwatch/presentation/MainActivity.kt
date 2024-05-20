package com.plcoding.wearosstopwatch.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.*
import com.plcoding.wearosstopwatch.R
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val CHANNEL_ID = "timer_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Creating notification channel if needed
        createNotificationChannel()

        // content view using Jetpack Compose
        setContent {
            val viewModel: StopWatchViewModel = viewModel()

            // state from the ViewModel
            val timerState by viewModel.timerState.collectAsStateWithLifecycle()
            val stopWatchText by viewModel.stopWatchText.collectAsStateWithLifecycle()

            // Scaffold to provide structure for the screen
            Scaffold(
                timeText = {
                    TimeText(
                        timeTextStyle = TimeTextDefaults.timeTextStyle(
                            fontSize = 10.sp
                        )
                    )
                },
                vignette = {
                    Vignette(vignettePosition = VignettePosition.TopAndBottom)
                }
            ) {
                // Custom composable function to display the stopwatch UI
                StopWatch(
                    state = timerState,
                    text = stopWatchText,
                    onToggleRunning = viewModel::toggleIsRunning,
                    onReset = viewModel::resetTimer,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Provide the context to the ViewModel for sending notifications
            viewModel.viewModelScope.launch {
                viewModel.sendNotification() = { sendNotification()}
            }
        }
    }

    // Function to create a notification channel (required for Android Oreo and above)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to send a notification
    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}

@Composable
private fun StopWatch(
    state: TimerState,
    text: String,
    onToggleRunning: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Column to arrange elements vertically
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display the stopwatch text
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Row to arrange buttons horizontally
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            // Button to toggle the running state of the timer
            Button(onClick = onToggleRunning) {
                Icon(
                    imageVector = if (state == TimerState.RUNNING) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.width(8.dp))

            // Button to reset the timer
            Button(
                onClick = onReset,
                enabled = state != TimerState.RESET,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null
                )
            }
        }
    }
}
