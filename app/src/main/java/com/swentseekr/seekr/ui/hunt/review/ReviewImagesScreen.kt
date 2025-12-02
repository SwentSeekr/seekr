package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.swentseekr.seekr.R

/**
 * Displays a screen allowing the user to review a list of images with a paging UI
 * * and optional full-screen viewing.
 *
 * @param photoUrls the list of image URLs to display in this screen pager.
 * @param onGoBack callback when the user cancels or navigates back.
 * @param modifier a modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewImagesScreen(
    photoUrls: List<String>,
    onGoBack: () -> Unit,
    modifier: Modifier = Modifier,
) {

  val scroll = rememberScrollState()
  val pagerState = rememberPagerState(pageCount = { photoUrls.size })
  var showFullScreen by remember { mutableStateOf(false) }
  var fullscreenIndex by remember { mutableStateOf(ReviewImagesScreenConstants.StartIndex) }

  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(ReviewImagesScreenConstantsStrings.Title) },
            navigationIcon = {
              IconButton(
                  onClick = onGoBack,
                  modifier = Modifier.testTag(ReviewImagesScreenConstantsStrings.BackButtonTag)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription =
                            ReviewImagesScreenConstantsStrings.BackButtonDescription)
                  }
            },
            modifier = Modifier.testTag(ReviewImagesScreenConstantsStrings.TopBarTestTag))
      },
      modifier = Modifier.testTag(ReviewImagesScreenConstantsStrings.ReviewImagesScreenTestTag)) {
          innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(ReviewImagesScreenConstants.PaddingColumn)
                    .padding(innerPadding)
                    .verticalScroll(scroll)
                    .testTag(ReviewImagesScreenConstantsStrings.ReviewImagesColumnTestTag),
        ) {
          Text(
              "${ReviewImagesScreenConstantsStrings.StartTopText} ${photoUrls.size} ${ReviewImagesScreenConstantsStrings.EndTopText}",
              fontSize = AddReviewScreenDefaults.TitleFontSize,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleLarge,
              modifier =
                  Modifier.padding(bottom = ReviewImagesScreenConstants.TextBottomPadding)
                      .testTag(ReviewImagesScreenConstantsStrings.ReviewImageInfoTexteTestTag))

          val index = pagerState.currentPage + ReviewImagesScreenConstants.One
          HorizontalPager(
              state = pagerState,
              modifier =
                  Modifier.fillMaxWidth()
                      .height(ReviewImagesScreenConstants.PagerHeight)
                      .testTag(ReviewImagesScreenConstantsStrings.ReviewImagePagerTestTag)) { page
                ->
                Box(
                    modifier =
                        Modifier.fillMaxSize()
                            .clip(RoundedCornerShape(ReviewImagesScreenConstants.RoundShape))
                            .testTag(
                                "${ReviewImagesScreenConstantsStrings.ReviewImageBoxTestTag}$page")
                            .clickable {
                              fullscreenIndex = page
                              showFullScreen = true
                            }) {
                      AsyncImage(
                          model = photoUrls[page],
                          contentDescription = null,
                          modifier =
                              Modifier.fillMaxWidth()
                                  .height(ReviewImagesScreenConstants.ImageSize)
                                  .padding(vertical = ReviewImagesScreenConstants.PaddingImage)
                                  .clip(
                                      RoundedCornerShape(
                                          ReviewImagesScreenConstants.ImageCornerRadius))
                                  .testTag(
                                      "${ReviewImagesScreenConstantsStrings.ReviewImageAsycTestTag}$page"),
                          contentScale = ContentScale.Fit,
                          placeholder = painterResource(R.drawable.logo_seekr),
                          error = painterResource(R.drawable.logo_seekr),
                      )
                    }
              }

          Text(
              "${ReviewImagesScreenConstantsStrings.ImageTitle}$index/${photoUrls.size}",
              fontSize = AddReviewScreenDefaults.TitleFontSize,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleLarge,
              modifier =
                  Modifier.padding(
                          top = ReviewImagesScreenConstants.TextTopPadding,
                          bottom = ReviewImagesScreenConstants.TextBottomPadding)
                      .testTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag))
        }
      }
  if (showFullScreen) {
    FullScreenImageViewer(
        images = photoUrls, startIndex = fullscreenIndex, onClose = { showFullScreen = false })
  }
}

/**
 * Displays a full-screen dialog that allows the user to swipe through a collection of images.
 *
 * @param images the list of image URLs to display in the full-screen pager.
 * @param startIndex the index of the image that should be shown first when the full-screen opens.
 * @param onClose callback invoked when the user dismisses the dialog (by tipping on the screen)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    images: List<String>,
    startIndex: Int,
    onClose: () -> Unit,
) {
  Dialog(
      onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .clickable { onClose() }
                    .testTag(
                        ReviewImagesScreenConstantsStrings.ReviewImageFullScreenDialogTestTag)) {
              val pagerState =
                  rememberPagerState(initialPage = startIndex, pageCount = { images.size })

              HorizontalPager(
                  state = pagerState,
                  modifier =
                      Modifier.fillMaxSize()
                          .testTag(
                              ReviewImagesScreenConstantsStrings
                                  .ReviewImageFullScreenPagerTestTag)) { page ->
                    AsyncImage(
                        model = images[page],
                        contentDescription =
                            "${ReviewImagesScreenConstantsStrings.ReviewImageFullScreenImageDescription} ${page + ReviewImagesScreenConstants.One}",
                        modifier = Modifier.fillMaxSize(),
                    )
                  }
            }
      }
}
