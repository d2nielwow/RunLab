package com.dmirzayants.runlab.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmirzayants.runlab.prefs.UserPreferencesRepository
import com.dmirzayants.runlab.prefs.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferences: UserPreferencesRepository
) : ViewModel() {

    val profile: StateFlow<UserProfile?> = userPreferences.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun save(input: ProfileInput, onSaved: () -> Unit) {
        val weight = input.parsedWeight ?: return
        viewModelScope.launch {
            userPreferences.saveProfile(input.name.trim(), weight)
            onSaved()
        }
    }
}

data class ProfileInput(val name: String = "", val weight: String = "") {
    val parsedWeight: Float?
        get() = weight.replace(',', '.').toFloatOrNull()?.takeIf { it in 20f..400f }

    val isNameValid: Boolean get() = name.isNotBlank()
    val isValid: Boolean get() = isNameValid && parsedWeight != null
}
