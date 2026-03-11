package com.sanghyuk.feature.settings.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val reminderManager = ReminderManager(context.applicationContext)
        reminderManager.showNotification()
        reminderManager.rescheduleIfNeeded()
    }
}