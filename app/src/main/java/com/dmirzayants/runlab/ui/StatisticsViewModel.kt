package com.dmirzayants.runlab.ui

import androidx.lifecycle.ViewModel
import com.dmirzayants.runlab.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    val mainRepository: MainRepository
) : ViewModel() {
}