package com.swentseekr.seekr.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(uiState.user) {
    uiState.user?.let {
      Toast.makeText(context, SignInScreenStrings.successMessage(it.email), Toast.LENGTH_SHORT)
          .show()
      onSignedIn()
    }
  }

  Scaffold { padding ->
    Column(
        modifier =
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary).padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
          Spacer(modifier = Modifier.height(SignInScreenDimensions.TopSpacing))

          Image(
              painter = painterResource(id = R.drawable.logo_seekr),
              contentDescription = SignInScreenStrings.AppLogoDescription,
              modifier =
                  Modifier.size(SignInScreenDimensions.LogoSize)
                      .clip(RoundedCornerShape(SignInScreenDimensions.LogoCornerRadius))
                      .testTag(SignInScreenTestTags.APP_LOGO))

          Spacer(modifier = Modifier.height(SignInScreenDimensions.SpacerAfterLogo))

          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Bottom,
              modifier =
                  Modifier.fillMaxWidth().padding(bottom = SignInScreenDimensions.BottomPadding)) {
                if (uiState.isLoading) {
                  CircularProgressIndicator(
                      color = MaterialTheme.colorScheme.onPrimary,
                      modifier = Modifier.size(SignInScreenDimensions.LoaderSize))
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
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.background),
      shape = RoundedCornerShape(SignInScreenDimensions.ButtonCornerRadius),
      border =
          BorderStroke(
              SignInScreenDimensions.ButtonBorderWidth, MaterialTheme.colorScheme.tertiary),
      modifier =
          Modifier.padding(horizontal = SignInScreenDimensions.ButtonHorizontalPadding)
              .height(SignInScreenDimensions.ButtonHeight)
              .fillMaxWidth(SignInScreenDimensions.ButtonWidthFraction)
              .testTag(SignInScreenTestTags.LOGIN_BUTTON)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()) {
              Image(
                  painter = painterResource(id = R.drawable.google_logo),
                  contentDescription = SignInScreenStrings.GoogleLogoDescription,
                  modifier =
                      Modifier.size(SignInScreenDimensions.ButtonIconSize)
                          .padding(end = SignInScreenDimensions.ButtonIconPaddingEnd))

              Text(
                  text = SignInScreenStrings.SignInButtonLabel,
                  color = MaterialTheme.colorScheme.onBackground,
                  fontSize = SignInScreenTypography.ButtonFontSize,
                  fontWeight = FontWeight.Medium)
            }
      }
}
