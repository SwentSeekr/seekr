package com.swentseekr.seekr.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.swentseekr.seekr.R

object SignInScreenTestTags {
  const val APP_LOGO = "appLogo"
  const val LOGIN_BUTTON = "loginButton"
}

/**
 * Displays the Google sign-in screen.
 *
 * @param viewModel ViewModel managing authentication state.
 * @param credentialManager Google credentials manager.
 * @param onSignedIn Callback called after successful sign-in.
 */
@Composable
fun SignInScreen(
    viewModel: AuthViewModel = viewModel(),
    credentialManager: CredentialManager = CredentialManager.create(LocalContext.current),
    onSignedIn: () -> Unit = {}
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  // Handle error messages
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  // Handle successful login
  LaunchedEffect(uiState.user) {
    uiState.user?.let {
      Toast.makeText(context, "Login successful: ${it.email}", Toast.LENGTH_SHORT).show()
      onSignedIn()
    }
  }

  Scaffold { padding ->
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF60BA37)).padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
          Spacer(modifier = Modifier.height(120.dp))

          // Center logo
          Image(
              painter = painterResource(id = R.drawable.logo_seekr),
              contentDescription = "App Logo",
              modifier =
                  Modifier.size(180.dp)
                      .clip(RoundedCornerShape(64.dp))
                      .testTag(SignInScreenTestTags.APP_LOGO))

          Spacer(modifier = Modifier.height(300.dp))

          // Bottom section
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom,
              modifier = Modifier.fillMaxWidth().padding(bottom = 64.dp)) {
                if (uiState.isLoading) {
                  CircularProgressIndicator(color = Color.White, modifier = Modifier.size(48.dp))
                } else {
                  GoogleSignInButton(
                      onSignInClick = { viewModel.signIn(context, credentialManager) })
                }
              }
        }
  }
}

/**
 * Composable for a Google Sign-In button.
 *
 * Displays a button styled with the Google logo and text. Triggers the provided `onSignInClick`
 * callback when pressed.
 */
@Composable
fun GoogleSignInButton(onSignInClick: () -> Unit) {
  Button(
      onClick = onSignInClick,
      colors = ButtonDefaults.buttonColors(containerColor = Color.White),
      shape = RoundedCornerShape(12.dp),
      border = BorderStroke(1.dp, Color.LightGray),
      modifier =
          Modifier.padding(horizontal = 32.dp)
              .height(48.dp)
              .fillMaxWidth(0.7f)
              .testTag(SignInScreenTestTags.LOGIN_BUTTON)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = "Google Logo",
                  modifier = Modifier.size(24.dp).padding(end = 8.dp))

              Text(
                  text = "Sign in with Google",
                  color = Color.Gray,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium)
            }
      }
}
