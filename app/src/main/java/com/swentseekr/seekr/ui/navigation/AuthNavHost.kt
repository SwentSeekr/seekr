package com.swentseekr.seekr.ui.auth

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class AuthScreen {
  SIGN_IN
}

@Composable
fun AuthNavHost(
    credentialManager: CredentialManager,
    viewModel: AuthViewModel = viewModel(),
    onSignedIn: () -> Unit,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
  val state by viewModel.state.collectAsState()

  // Forward to onSignedIn if already authenticated
  LaunchedEffect(state.isAuthenticated) { if (state.isAuthenticated) onSignedIn() }

  NavHost(
      navController = navController,
      startDestination = AuthScreen.SIGN_IN.name,
      modifier = modifier) {
        composable(AuthScreen.SIGN_IN.name) {
          SignInScreen(
              authViewModel = viewModel,
              credentialManager = credentialManager,
              onSignedIn = onSignedIn)
        }
      }
}
