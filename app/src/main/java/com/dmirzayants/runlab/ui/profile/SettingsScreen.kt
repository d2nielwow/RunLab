package com.dmirzayants.runlab.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dmirzayants.runlab.R
import com.dmirzayants.runlab.ui.common.RunLabTopBar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    var weight by rememberSaveable { mutableStateOf("") }
    var initialized by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.settings_saved)

    LaunchedEffect(profile) {
        val loaded = profile
        if (!initialized && loaded != null) {
            name = loaded.name
            weight = formatWeight(loaded.weightKg)
            initialized = true
        }
    }

    val input = ProfileInput(name, weight)

    Scaffold(topBar = { RunLabTopBar(userName = profile?.name.orEmpty()) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            ProfileForm(
                input = input,
                onInputChange = {
                    name = it.name
                    weight = it.weight
                },
                showErrors = showErrors
            )
            Button(
                onClick = {
                    if (input.isValid) {
                        showErrors = false
                        viewModel.save(input) {
                            scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                        }
                    } else {
                        showErrors = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_apply))
            }
        }
    }
}
