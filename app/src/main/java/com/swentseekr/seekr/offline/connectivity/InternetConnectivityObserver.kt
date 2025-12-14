package com.swentseekr.seekr.offline.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Observes the internet connectivity state of the device.
 *
 * @param context The application context used to access system connectivity services.
 */
class InternetConnectivityObserver(context: Context) {

  /** Application context to avoid leaking an Activity or Service context. */
  private val appContext = context.applicationContext

  /** The system connectivity manager used to monitor network status. */
  private val connectivityManager =
      appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

  /** Internal mutable state representing the current internet connectivity. */
  private val _connectionState = MutableStateFlow(isConnectedNow())

  /** Public read-only flow of the current internet connectivity status. */
  val connectionState: StateFlow<Boolean> = _connectionState.asStateFlow()

  /** Network callback used to monitor changes in the device's internet connectivity. */
  private val callback =
      object : ConnectivityManager.NetworkCallback() {

        /**
         * Called when a network with internet capability becomes available. Updates the
         * [_connectionState] to reflect current connectivity.
         *
         * @param network The [Network] that became available.
         */
        override fun onAvailable(network: Network) {
          updateConnectionState()
        }

        /**
         * Called when a network is lost. Updates the [_connectionState] to reflect loss of
         * connectivity.
         *
         * @param network The [Network] that was lost.
         */
        override fun onLost(network: Network) {
          updateConnectionState()
        }

        /**
         * Called when the capabilities of a network change. Ensures [_connectionState] remains
         * accurate.
         *
         * @param network The [Network] whose capabilities changed.
         * @param networkCapabilities The new [NetworkCapabilities] of the network.
         */
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
          updateConnectionState()
        }

        /**
         * Called when the requested network is unavailable. Explicitly sets [_connectionState] to
         * `false`.
         */
        override fun onUnavailable() {
          _connectionState.value = false
        }
      }

  /**
   * Checks the current connectivity state immediately.
   *
   * @return true if the device currently has a validated internet connection, false otherwise.
   */
  private fun isConnectedNow(): Boolean {
    val network = connectivityManager.activeNetwork ?: return false
    val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
  }

  /** Updates the internal state with the current connectivity status. */
  private fun updateConnectionState() {
    _connectionState.value = isConnectedNow()
  }

  /**
   * Starts observing network connectivity changes.
   *
   * Registers the network callback to receive updates about internet connectivity.
   */
  fun start() {
    val request =
        NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
    connectivityManager.registerNetworkCallback(request, callback)
    updateConnectionState()
  }

  /**
   * Stops observing network connectivity changes.
   *
   * Unregisters the network callback. If it has already been unregistered, any exception is
   * ignored.
   */
  fun stop() {
    try {
      connectivityManager.unregisterNetworkCallback(callback)
    } catch (_: Exception) {
      // already unregistered, ignore
    }
  }
}
