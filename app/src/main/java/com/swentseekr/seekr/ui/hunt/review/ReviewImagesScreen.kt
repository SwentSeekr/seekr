package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.swentseekr.seekr.R

/**
 * Displays a screen allowing the user to review a list of images with a paging UI and optional
 * full-screen viewing.
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
                    .testTag(ReviewImagesScreenConstantsStrings.ReviewImagesColumnTestTag)) {
              val index = pagerState.currentPage + ReviewImagesScreenConstants.One

              HorizontalPager(
                  state = pagerState,
                  modifier =
                      Modifier.fillMaxWidth()
                          .height(ReviewImagesScreenConstants.PagerHeight)
                          .testTag(ReviewImagesScreenConstantsStrings.ReviewImagePagerTestTag)) {
                      page ->
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .clip(RoundedCornerShape(ReviewImagesScreenConstants.RoundShape))
                                .clickable {
                                  fullscreenIndex = page
                                  showFullScreen = true
                                }
                                .testTag(
                                    "${ReviewImagesScreenConstantsStrings.ReviewImageBoxTestTag}$page")) {
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

              Spacer(modifier = Modifier.height(ReviewImagesScreenConstants.SpacerHeight))

              Text(
                  text = "${index}/${photoUrls.size}",
                  fontSize = AddReviewScreenDefaults.TitleFontSize,
                  fontWeight = FontWeight.SemiBold,
                  style = MaterialTheme.typography.titleMedium,
                  modifier =
                      Modifier.align(Alignment.CenterHorizontally)
                          .testTag(ReviewImagesScreenConstantsStrings.ReviewImageTextBottomTestTag))
            }
      }

  if (showFullScreen) {
    FullScreenImageViewer(
        images = photoUrls, startIndex = fullscreenIndex, onClose = { showFullScreen = false })
  }
}

/** Displays a full-screen dialog that allows the user to swipe through a collection of images. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageViewer(
    images: List<String>,
    startIndex: Int,
    onClose: () -> Unit,
) {
  Dialog(
      onDismissRequest = onClose,
      properties =
          DialogProperties(
              usePlatformDefaultWidth = ReviewImagesScreenConstants.UsePlatformDefaultWidth)) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(Color.Black)
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
                        contentScale = ContentScale.Fit,
                    )
                  }

              // Top overlay with close + index
              val currentIndex = pagerState.currentPage + ReviewImagesScreenConstants.One

              Box(
                  modifier =
                      Modifier.fillMaxWidth()
                          .align(Alignment.TopStart)
                          .background(
                              Brush.verticalGradient(
                                  colors =
                                      listOf(
                                          Color.Black.copy(
                                              alpha =
                                                  ReviewImagesScreenConstants
                                                      .FullScreenOverlayStartAlpha),
                                          Color.Transparent)))
                          .padding(
                              horizontal = ReviewImagesScreenConstants.FullScreenPadding,
                              vertical = ReviewImagesScreenConstants.FullScreenPadding)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()) {
                          IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription =
                                    ReviewImagesScreenConstantsStrings.CloseContentDescription,
                                tint = Color.White)
                          }

                          Text(
                              text = "${currentIndex}/${images.size}",
                              style = MaterialTheme.typography.bodyMedium,
                              color = Color.White)
                        }
                  }
            }
      }
}
