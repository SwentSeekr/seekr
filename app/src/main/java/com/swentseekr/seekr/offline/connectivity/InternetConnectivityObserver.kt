package com.swentseekr.seekr.offline.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class InternetConnectivityObserver(context: Context) {

  private val appContext = context.applicationContext
  private val connectivityManager =
      appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  private val _connectionState = MutableStateFlow(isConnectedNow())
  val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

  private val callback =
      object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          updateConnectionState()
        }

        override fun onLost(network: Network) {
          updateConnectionState()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
          updateConnectionState()
        }

        override fun onUnavailable() {
          _connectionState.value = false
        }
      }

  private fun isConnectedNow(): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  }

  private fun updateConnectionState() {
    _connectionState.value = isConnectedNow()
  }

  fun start() {
    val request =
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    connectivityManager.registerNetworkCallback(request, callback)
    updateConnectionState()
  }

  fun stop() {
    try {
      connectivityManager.unregisterNetworkCallback(callback)
    } catch (_: Exception) {
      // already unregistered, ignore
    }
  }
}
