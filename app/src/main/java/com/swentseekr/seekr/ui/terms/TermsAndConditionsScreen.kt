package com.swentseekr.seekr.ui.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndConditionsScreen(onGoBack: () -> Unit = {}) {
  val scrollState = rememberScrollState()

  Scaffold(
      topBar = {
        TopAppBar(
            title = {
              Text(TermsScreenStrings.TITLE, style = MaterialTheme.typography.headlineSmall)
            },
            navigationIcon = {
              IconButton(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(TermsScreenTestTags.BACK_BUTTON)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = TermsScreenStrings.BACK_DESCRIPTION)
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface))
      },
      modifier = Modifier.testTag(TermsScreenTestTags.SCREEN)) { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))))
                    .padding(paddingValues)
                    .padding(horizontal = TermsScreenConstants.SCREEN_PADDING)
                    .verticalScroll(scrollState)
                    .testTag(TermsScreenTestTags.CONTENT_COLUMN),
            verticalArrangement = Arrangement.spacedBy(TermsScreenConstants.SECTION_SPACING)) {
              Spacer(modifier = Modifier.height(TermsScreenConstants.TOP_SPACER))

              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(TermsScreenConstants.CARD_CORNER_RADIUS),
                  colors =
                      CardDefaults.cardColors(
                          containerColor =
                              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                  elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                    Column(modifier = Modifier.padding(TermsScreenConstants.CARD_PADDING)) {
                      Text(
                          text = TermsScreenStrings.LAST_UPDATED,
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }

              TermsSection(
                  title = TermsScreenStrings.SECTION_1_TITLE,
                  content = TermsScreenStrings.SECTION_1_CONTENT)

              TermsSection(
                  title = TermsScreenStrings.SECTION_2_TITLE,
                  content = TermsScreenStrings.SECTION_2_CONTENT)

              TermsSection(
                  title = TermsScreenStrings.SECTION_3_TITLE,
                  content = TermsScreenStrings.SECTION_3_CONTENT)

              TermsSection(
                  title = TermsScreenStrings.SECTION_4_TITLE,
                  content = TermsScreenStrings.SECTION_4_CONTENT)

              TermsSection(
                  title = TermsScreenStrings.SECTION_5_TITLE,
                  content = TermsScreenStrings.SECTION_5_CONTENT)

              TermsSection(
                  title = TermsScreenStrings.SECTION_6_TITLE,
                  content = TermsScreenStrings.SECTION_6_CONTENT)

              Spacer(
                  modifier =
                      Modifier.height(TermsScreenConstants.BOTTOM_SPACER)
                          .testTag(TermsScreenTestTags.LAST_SPACER))
            }
      }
}

@Composable
fun TermsSection(title: String, content: String) {
  Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(TermsScreenConstants.CARD_CORNER_RADIUS),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation =
          CardDefaults.cardElevation(defaultElevation = TermsScreenConstants.CARD_ELEVATION)) {
        Column(
            modifier = Modifier.padding(TermsScreenConstants.CARD_PADDING),
            verticalArrangement = Arrangement.spacedBy(TermsScreenConstants.TEXT_SPACING)) {
              Text(
                  text = title,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary)
              Text(
                  text = content,
                  color = MaterialTheme.colorScheme.onSurface,
                  style = MaterialTheme.typography.bodyMedium,
                  lineHeight = TermsScreenConstants.LINE_HEIGHT)
            }
      }
}

@Preview
@Composable
private fun TermsScreenPreview() {
  TermsAndConditionsScreen()
}
