package com.androhanu.drbattery

import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi

class AlertService : Service() {
    private lateinit var player: Ringtone
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player = RingtoneManager.getRingtone(this@AlertService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
        player.play()
        player.isLooping = true
        return START_STICKY
    }

    override fun onDestroy() {

        player.stop()
    }
}