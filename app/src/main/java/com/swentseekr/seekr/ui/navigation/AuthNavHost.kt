package com.swentseekr.seekr.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.swentseekr.seekr.ui.auth.AuthViewModel
import com.swentseekr.seekr.ui.auth.SignInScreen

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
  val uiState by viewModel.uiState.collectAsState()

  // If already authenticated, go to next screen
  LaunchedEffect(uiState.user) {
    if (uiState.user != null) {
      onSignedIn()
    }
  }

  NavHost(
      navController = navController,
      startDestination = AuthScreen.SIGN_IN.name,
      modifier = modifier) {
        composable(AuthScreen.SIGN_IN.name) {
          SignInScreen(
              viewModel = viewModel, credentialManager = credentialManager, onSignedIn = onSignedIn)
        }
      }
}
