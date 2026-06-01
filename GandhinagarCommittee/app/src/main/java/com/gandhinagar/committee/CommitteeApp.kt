package com.gandhinagar.committee

import android.app.Application
import com.gandhinagar.committee.export.BackupWorker

class CommitteeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Schedule the daily 8 PM Excel backup.
        BackupWorker.schedule(this)
    }
}
