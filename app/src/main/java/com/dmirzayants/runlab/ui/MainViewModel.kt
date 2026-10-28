package com.dmirzayants.runlab.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmirzayants.runlab.prefs.UserPreferencesRepository
import com.dmirzayants.runlab.prefs.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    userPreferences: UserPreferencesRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userPreferences.userProfile
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
