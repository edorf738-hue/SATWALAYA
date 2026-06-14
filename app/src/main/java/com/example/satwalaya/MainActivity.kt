package com.example.satwalaya

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.example.satwalaya.databinding.ActivityMainBinding
import com.example.satwalaya.utils.SessionManager
import com.example.satwalaya.utils.NotificationHelper
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private var offlineSnackbar: Snackbar? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread {
                offlineSnackbar?.dismiss()
                offlineSnackbar = null
                Snackbar.make(binding.root, "Kembali online ✓", Snackbar.LENGTH_SHORT)
                    .setAnchorView(binding.bottomNav)
                    .show()
            }
        }

        override fun onLost(network: Network) {
            runOnUiThread {
                if (offlineSnackbar?.isShown != true) {
                    offlineSnackbar = Snackbar.make(
                        binding.root,
                        "Kamu sedang offline. Menampilkan data terakhir.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAnchorView(binding.bottomNav).also { it.show() }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        applyTheme()
        NotificationHelper.createChannels(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setOnItemSelectedListener { item ->
            navController.navigate(item.itemId, null, navOptions {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            })
            true
        }

        binding.bottomNav.setOnItemReselectedListener { item ->
            navController.popBackStack(item.itemId, false)
        }
    }

    override fun onResume() {
        super.onResume()
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, networkCallback)
        if (!isConnected()) {
            offlineSnackbar = Snackbar.make(
                binding.root,
                "Kamu sedang offline. Menampilkan data terakhir.",
                Snackbar.LENGTH_INDEFINITE
            ).setAnchorView(binding.bottomNav).also { it.show() }
        }
    }

    override fun onPause() {
        super.onPause()
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        try { cm.unregisterNetworkCallback(networkCallback) } catch (_: IllegalArgumentException) {}
        offlineSnackbar?.dismiss()
        offlineSnackbar = null
    }

    private fun isConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork ?: return false) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun applyTheme() {
        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}