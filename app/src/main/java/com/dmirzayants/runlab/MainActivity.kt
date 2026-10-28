package com.dmirzayants.runlab

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dmirzayants.runlab.constants.Constants
import com.dmirzayants.runlab.ui.MainViewModel
import com.dmirzayants.runlab.ui.RunLabApp
import com.dmirzayants.runlab.ui.theme.RunLabTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val openTrackingRequested = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handleIntent(intent)

        setContent {
            RunLabTheme {
                val profile by viewModel.profile.collectAsStateWithLifecycle()
                val openTracking by openTrackingRequested.collectAsStateWithLifecycle()
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    profile?.let {
                        RunLabApp(
                            profile = it,
                            openTrackingRequested = openTracking,
                            onOpenTrackingHandled = { openTrackingRequested.value = false }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Constants.ACTION_SHOW_TRACKING_SCREEN) {
            openTrackingRequested.value = true
        }
    }
}
