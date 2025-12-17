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

/** Enumeration of authentication screens used in [AuthNavHost]. */
enum class AuthScreen {
  /** Sign-in screen for user authentication. */
  SIGN_IN
}

/**
 * Sets up the navigation host for authentication-related screens.
 *
 * Observes the [AuthViewModel] state and triggers [onSignedIn] callback when a user successfully
 * signs in.
 *
 * @param credentialManager the [CredentialManager] used for handling platform credentials.
 * @param viewModel the [AuthViewModel] instance, defaults to Compose's `viewModel()`.
 * @param onSignedIn callback invoked when a user successfully signs in.
 * @param navController the [NavHostController] to manage navigation; defaults to a new
 *   [rememberNavController].
 * @param modifier optional [Modifier] for styling the NavHost container.
 */
@Composable
fun AuthNavHost(
    credentialManager: CredentialManager,
    viewModel: AuthViewModel = viewModel(),
    onSignedIn: () -> Unit,
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

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
