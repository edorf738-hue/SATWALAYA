package com.example.satwalaya.ui.notifications

import com.google.firebase.firestore.PropertyName

data class NotificationItem(
    val id: String = "",

    @get:PropertyName("bookingId") @set:PropertyName("bookingId")
    var bookingId: String = "",

    @get:PropertyName("title") @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("message") @set:PropertyName("message")
    var message: String = "",

    @get:PropertyName("type") @set:PropertyName("type")
    var type: String = "",

    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var isRead: Boolean = false,

    @get:PropertyName("userId") @set:PropertyName("userId")
    var userId: String = "",

    @get:PropertyName("createdAt") @set:PropertyName("createdAt")
    var createdAt: com.google.firebase.Timestamp? = null
)