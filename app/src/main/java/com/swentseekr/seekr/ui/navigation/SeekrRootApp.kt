package com.swentseekr.seekr.ui.navigation

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.ui.auth.AuthViewModel

@Composable
fun SeekrRootApp(
    context: Context = LocalContext.current,
    authViewModel: AuthViewModel = viewModel()
) {
  // Remember CredentialManager so it's not recreated on every recomposition
  val credentialManager = remember { CredentialManager.create(context) }

  val state by authViewModel.state.collectAsState()

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    if (state.isAuthenticated) {
      // User is logged in → go to main app
      SeekrMainNavHost(modifier = Modifier.padding(innerPadding))
    } else {
      // Not logged in → show auth flow
      AuthNavHost(
          credentialManager = credentialManager,
          viewModel = authViewModel,
          onSignedIn = { /* ViewModel observes FirebaseAuth automatically */},
          modifier = Modifier.padding(innerPadding))
    }
  }
}
