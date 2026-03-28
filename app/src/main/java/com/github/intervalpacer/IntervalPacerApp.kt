package com.github.intervalpacer

import android.app.Application
import com.github.intervalpacer.data.local.AppDatabase
import com.github.intervalpacer.data.repository.HistoryRepositoryImpl
import com.github.intervalpacer.domain.repository.HistoryRepository

class IntervalPacerApp : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getInstance(this)
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepositoryImpl(database)
    }
}
