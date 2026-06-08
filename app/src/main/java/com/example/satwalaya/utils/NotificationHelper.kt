package com.example.satwalaya.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.satwalaya.R

object NotificationHelper {

    private const val CHANNEL_BOOKING = "channel_booking"
    private const val CHANNEL_PROMO = "channel_promo"
    private const val CHANNEL_HEALTH = "channel_health"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_BOOKING, "Pembaruan Booking", NotificationManager.IMPORTANCE_HIGH)
            )
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_PROMO, "Promosi & Penawaran", NotificationManager.IMPORTANCE_DEFAULT)
            )
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL_HEALTH, "Tips Kesehatan Hewan", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    fun sendBookingNotif(context: Context, message: String) {
        val session = SessionManager(context)
        if (!session.getBookingNotif()) return
        send(context, CHANNEL_BOOKING, "Pembaruan Booking", message, 1001)
    }

    fun sendPromoNotif(context: Context, message: String) {
        val session = SessionManager(context)
        if (!session.getPromoNotif()) return
        send(context, CHANNEL_PROMO, "Promosi & Penawaran", message, 1002)
    }

    fun sendHealthNotif(context: Context, message: String) {
        val session = SessionManager(context)
        if (!session.getHealthNotif()) return
        send(context, CHANNEL_HEALTH, "Tips Kesehatan Hewan", message, 1003)
    }

    private fun send(context: Context, channelId: String, title: String, message: String, notifId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(notifId, notif)
    }
}