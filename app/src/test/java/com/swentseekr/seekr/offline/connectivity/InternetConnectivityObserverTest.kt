package com.swentseekr.seekr.offline.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import io.mockk.CapturingSlot
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the InternetConnectivityObserver.
 *
 * This test suite verifies the observer's ability to monitor internet connectivity changes using
 * mocked Android networking components.
 */
class InternetConnectivityObserverTest {

  // mock objects

  private lateinit var mockContext: Context
  private lateinit var mockConnectivityManager: ConnectivityManager
  private lateinit var mockNetwork: Network
  private lateinit var networkCapabilities: NetworkCapabilities
  private lateinit var callbackSlot: CapturingSlot<ConnectivityManager.NetworkCallback>

  @Before
  fun setup() {
    mockContext = mockk(relaxed = true)
    mockConnectivityManager = mockk(relaxed = true)
    mockNetwork = mockk(relaxed = true)
    networkCapabilities = mockk(relaxed = true)

    every { mockContext.applicationContext } returns mockContext
    every { mockContext.getSystemService(Context.CONNECTIVITY_SERVICE) } returns
        mockConnectivityManager

    callbackSlot = slot()
    every {
      mockConnectivityManager.registerNetworkCallback(any<NetworkRequest>(), capture(callbackSlot))
    } returns Unit

    mockkConstructor(NetworkRequest.Builder::class)
    val builderMock = mockk<NetworkRequest.Builder>(relaxed = true)
    val networkRequestMock = mockk<NetworkRequest>(relaxed = true)

    every { anyConstructed<NetworkRequest.Builder>().addCapability(any()) } returns builderMock

    every { builderMock.build() } returns networkRequestMock
  }

  @After
  fun tearDown() {
    clearAllMocks()
    unmockkAll()
  }

  @Test
  fun initialOnlineWhenNetworkValid() {
    every { mockConnectivityManager.activeNetwork } returns mockNetwork
    every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns
        networkCapabilities
    every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns
        true
    every {
      networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } returns true

    val observer = InternetConnectivityObserver(mockContext)

    assertEquals(true, observer.connectionState.value)
  }

  @Test
  fun initialOfflineWhenNoNetwork() {
    every { mockConnectivityManager.activeNetwork } returns null

    val observer = InternetConnectivityObserver(mockContext)

    assertEquals(false, observer.connectionState.value)
  }

  @Test
  fun startRegistersCallbackAndUpdatesState() {
    every { mockConnectivityManager.activeNetwork } returns null

    val observer = InternetConnectivityObserver(mockContext)

    every { mockConnectivityManager.activeNetwork } returns mockNetwork
    every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns
        networkCapabilities
    every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns
        true
    every {
      networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } returns true

    observer.start()

    verify(exactly = 1) {
      mockConnectivityManager.registerNetworkCallback(
          any<NetworkRequest>(), any<ConnectivityManager.NetworkCallback>())
    }

    assertEquals(true, observer.connectionState.value)
  }

  @Test
  fun onAvailableSetsOnline() {
    every { mockConnectivityManager.activeNetwork } returns null

    val observer = InternetConnectivityObserver(mockContext)
    observer.start()

    every { mockConnectivityManager.activeNetwork } returns mockNetwork
    every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns
        networkCapabilities
    every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns
        true
    every {
      networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } returns true

    callbackSlot.captured.onAvailable(mockNetwork)

    assertEquals(true, observer.connectionState.value)
  }

  @Test
  fun onLostSetsOffline() {
    every { mockConnectivityManager.activeNetwork } returns mockNetwork
    every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns
        networkCapabilities
    every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns
        true
    every {
      networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } returns true

    val observer = InternetConnectivityObserver(mockContext)
    observer.start()
    assertEquals(true, observer.connectionState.value)

    every { mockConnectivityManager.activeNetwork } returns null

    callbackSlot.captured.onLost(mockNetwork)

    assertEquals(false, observer.connectionState.value)
  }

  @Test
  fun onCapabilitiesChangedUpdatesState() {
    every { mockConnectivityManager.activeNetwork } returns null

    val observer = InternetConnectivityObserver(mockContext)
    observer.start()

    every { mockConnectivityManager.activeNetwork } returns mockNetwork
    every { mockConnectivityManager.getNetworkCapabilities(mockNetwork) } returns
        networkCapabilities
    every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns
        true
    every {
      networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    } returns true

    callbackSlot.captured.onCapabilitiesChanged(mockNetwork, networkCapabilities)

    assertEquals(true, observer.connectionState.value)
  }

  @Test
  fun onUnavailableSetsOffline() {
    every { mockConnectivityManager.activeNetwork } returns null

    val observer = InternetConnectivityObserver(mockContext)
    observer.start()

    callbackSlot.captured.onUnavailable()

    assertEquals(false, observer.connectionState.value)
  }

  @Test
  fun stopUnregistersCallback() {
    every { mockConnectivityManager.activeNetwork } returns null

    val observer = InternetConnectivityObserver(mockContext)
    observer.start()

    observer.stop()

    verify(exactly = 1) { mockConnectivityManager.unregisterNetworkCallback(callbackSlot.captured) }
  }

  @Test
  fun stopSwallowException() {
    every { mockConnectivityManager.activeNetwork } returns null

    val observer = InternetConnectivityObserver(mockContext)
    observer.start()

    every {
      mockConnectivityManager.unregisterNetworkCallback(any<ConnectivityManager.NetworkCallback>())
    } throws IllegalStateException("already unregistered")

    observer.stop()
  }
}
