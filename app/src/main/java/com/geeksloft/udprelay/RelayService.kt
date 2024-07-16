package com.geeksloft.udprelay

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class RelayService : Service() {
    private val LOG_TAG = "Relay Service"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(LOG_TAG, "Service Started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(LOG_TAG, "Service Stopped")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}