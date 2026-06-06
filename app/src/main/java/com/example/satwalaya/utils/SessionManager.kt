package com.example.satwalaya.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val pref: SharedPreferences = context.getSharedPreferences("SatwalayaSession", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    companion object {
        private const val IS_LOGIN = "isLogin"
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_DARK_MODE = "isDarkMode"

        // Pet Info
        private const val KEY_PET_NAME = "petName"
        private const val KEY_PET_AGE = "petAge"
        private const val KEY_PET_ALLERGIES = "petAllergies"

        // Payment Info
        private const val KEY_SAVED_CARD = "savedCard"

        // Loyalty Points
        private const val KEY_POINTS = "userPoints"

        // Notification settings
        private const val KEY_NOTIF_BOOKING = "notifBooking"
        private const val KEY_NOTIF_PROMO = "notifPromo"
        private const val KEY_NOTIF_HEALTH = "notifHealth"

        // Privacy settings
        private const val KEY_2FA = "twoFactorEnabled"
        private const val KEY_BIOMETRIC = "biometricEnabled"
    }

    fun saveLoginSession(username: String, email: String, phone: String = "") {
        editor.putBoolean(IS_LOGIN, true)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_PHONE, phone)
        // Initialize points for new users if not set
        if (getUserPoints() == 0) {
            setUserPoints(100) // Welcome points
        }
        editor.apply()
    }

    fun savePetInfo(name: String, age: String, allergies: String) {
        editor.putString(KEY_PET_NAME, name)
        editor.putString(KEY_PET_AGE, age)
        editor.putString(KEY_PET_ALLERGIES, allergies)
        editor.apply()
    }

    fun getPetName(): String = pref.getString(KEY_PET_NAME, "") ?: ""
    fun getPetAge(): String = pref.getString(KEY_PET_AGE, "") ?: ""
    fun getPetAllergies(): String = pref.getString(KEY_PET_ALLERGIES, "") ?: ""

    fun saveCard(cardNumber: String) {
        editor.putString(KEY_SAVED_CARD, cardNumber)
        editor.apply()
    }
    fun getSavedCard(): String = pref.getString(KEY_SAVED_CARD, "") ?: ""

    fun setUserPoints(points: Int) {
        editor.putInt(KEY_POINTS, points)
        editor.apply()
    }
    fun getUserPoints(): Int = pref.getInt(KEY_POINTS, 0)
    fun addPoints(points: Int) {
        setUserPoints(getUserPoints() + points)
    }

    fun setDarkMode(isEnabled: Boolean) {
        editor.putBoolean(KEY_DARK_MODE, isEnabled)
        editor.apply()
    }
    fun isDarkMode(): Boolean = pref.getBoolean(KEY_DARK_MODE, false)

    // Notification toggles
    fun setBookingNotif(enabled: Boolean) = editor.putBoolean(KEY_NOTIF_BOOKING, enabled).apply()
    fun getBookingNotif(): Boolean = pref.getBoolean(KEY_NOTIF_BOOKING, true)

    fun setPromoNotif(enabled: Boolean) = editor.putBoolean(KEY_NOTIF_PROMO, enabled).apply()
    fun getPromoNotif(): Boolean = pref.getBoolean(KEY_NOTIF_PROMO, false)

    fun setHealthNotif(enabled: Boolean) = editor.putBoolean(KEY_NOTIF_HEALTH, enabled).apply()
    fun getHealthNotif(): Boolean = pref.getBoolean(KEY_NOTIF_HEALTH, true)

    // Privacy toggles
    fun set2FA(enabled: Boolean) = editor.putBoolean(KEY_2FA, enabled).apply()
    fun is2FAEnabled(): Boolean = pref.getBoolean(KEY_2FA, false)

    fun setBiometric(enabled: Boolean) = editor.putBoolean(KEY_BIOMETRIC, enabled).apply()
    fun isBiometricEnabled(): Boolean = pref.getBoolean(KEY_BIOMETRIC, false)

    fun isLoggedIn(): Boolean = pref.getBoolean(IS_LOGIN, false)
    fun getUsername(): String = pref.getString(KEY_USERNAME, "") ?: ""
    fun getEmail(): String = pref.getString(KEY_EMAIL, "") ?: ""
    fun getPhone(): String = pref.getString(KEY_PHONE, "") ?: ""

    fun clearSession() {
        editor.clear()
        editor.apply()
    }
}