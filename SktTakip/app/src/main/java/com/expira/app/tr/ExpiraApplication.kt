package com.expira.app.tr

import android.app.Application
import com.expira.app.tr.data.local.AppDatabase
import com.expira.app.tr.worker.NotificationHelper

class ExpiraApplication : Application() {

    val database: AppDatabase by lazy {
        AppDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize Notification Channels
        NotificationHelper.createNotificationChannel(this)

        // Schedule periodic WorkManager for daily expiry monitoring
        NotificationHelper.scheduleDailyCheck(this)
    }
}
