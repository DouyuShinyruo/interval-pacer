package com.github.intervalpacer.presentation.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.github.intervalpacer.IntervalPacerApp
import com.github.intervalpacer.data.model.WorkoutRecord
import com.github.intervalpacer.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val historyRepository: HistoryRepository =
        (application as IntervalPacerApp).historyRepository

    val allRecords: Flow<List<WorkoutRecord>> = historyRepository.getAllRecords()

    fun deleteRecord(id: String) {
        viewModelScope.launch { historyRepository.deleteRecord(id) }
    }
}
