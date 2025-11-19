package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HuntImageCarousel(
    hunt: com.swentseekr.seekr.model.hunt.Hunt,
    modifier: Modifier = Modifier,
) {
  val images: List<Any> =
      remember(hunt) {
        buildList {
          val main = hunt.mainImageUrl.takeIf { it.isNotBlank() }
          if (main != null) {
            add(main)
          } else {
            add(R.drawable.empty_image)
          }
          hunt.otherImagesUrls.filter { it.isNotBlank() }.forEach { add(it) }
        }
      }

  val pagerState = rememberPagerState(pageCount = { images.size })
  var showFullScreen by remember { mutableStateOf(false) }

  HuntImageFullScreenDialog(
      showFullScreen = showFullScreen,
      currentImage = images[pagerState.currentPage],
      onDismiss = { showFullScreen = false },
  )

  Box(
      modifier = modifier.testTag(HuntCardScreenTestTags.IMAGE_CAROUSEL_CONTAINER),
  ) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      HuntImagePager(
          images = images,
          pagerState = pagerState,
          onCurrentPageClicked = { showFullScreen = true },
      )

      HuntImageIndicators(
          imagesCount = images.size,
          currentPage = pagerState.currentPage,
      )
    }
  }
}

@Composable
private fun HuntImageFullScreenDialog(
    showFullScreen: Boolean,
    currentImage: Any,
    onDismiss: () -> Unit,
) {
  if (!showFullScreen) return

  Dialog(onDismissRequest = onDismiss) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
      AsyncImage(
          model = currentImage,
          contentDescription = HuntCardScreenStrings.FullScreenImageDescription,
          modifier =
              Modifier.fillMaxWidth()
                  .fillMaxHeight(HuntCardScreenDefaults.FullScreenImageHeightFraction)
                  .testTag(HuntCardScreenTestTags.IMAGE_FULLSCREEN),
          placeholder = painterResource(R.drawable.empty_image),
          error = painterResource(R.drawable.empty_image),
          contentScale = ContentScale.Fit,
      )
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HuntImagePager(
    images: List<Any>,
    pagerState: PagerState,
    onCurrentPageClicked: () -> Unit,
) {
  HorizontalPager(
      state = pagerState,
      modifier =
          Modifier.fillMaxWidth()
              .height(HuntCardScreenDefaults.ImageCarouselHeight)
              .testTag(HuntCardScreenTestTags.IMAGE_PAGER),
      contentPadding =
          PaddingValues(horizontal = HuntCardScreenDefaults.ImageCarouselPagerContentPadding),
      pageSpacing = HuntCardScreenDefaults.ImageCarouselPageSpacing,
  ) { page ->
    val transforms = rememberPageTransforms(page = page, pagerState = pagerState)

    HuntImagePage(
        image = images[page],
        page = page,
        isCurrentPage = page == pagerState.currentPage,
        transforms = transforms,
        onClickCurrent = onCurrentPageClicked,
    )
  }
}

private data class PageTransforms(
    val scale: Float,
    val rotationY: Float,
    val overlayAlpha: Float,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberPageTransforms(
    page: Int,
    pagerState: PagerState,
): PageTransforms {
  val pageOffset =
      (pagerState.currentPage - page + pagerState.currentPageOffsetFraction).absoluteValue

  val clampedOffset =
      pageOffset.coerceIn(
          HuntCardScreenDefaults.ImageCarouselInterpolationMinFraction,
          HuntCardScreenDefaults.ImageCarouselInterpolationMaxFraction,
      )

  val interpolationFraction =
      HuntCardScreenDefaults.ImageCarouselInterpolationMaxFraction - clampedOffset

  val scale =
      lerp(
          start = HuntCardScreenDefaults.ImageCarouselMinScale,
          stop = HuntCardScreenDefaults.ImageCarouselMaxScale,
          fraction = interpolationFraction,
      )

  val sideRotation =
      if (page < pagerState.currentPage) {
        HuntCardScreenDefaults.ImageCarouselSideRotationDegrees
      } else {
        -HuntCardScreenDefaults.ImageCarouselSideRotationDegrees
      }

  val rotationY = lerp(start = sideRotation, stop = 0f, fraction = interpolationFraction)

  val overlayAlpha =
      lerp(
          start = HuntCardScreenDefaults.ImageCarouselOverlayMaxAlpha,
          stop = HuntCardScreenDefaults.ImageCarouselOverlayMinAlpha,
          fraction = interpolationFraction,
      )

  return PageTransforms(
      scale = scale,
      rotationY = rotationY,
      overlayAlpha = overlayAlpha,
  )
}

@Composable
private fun HuntImagePage(
    image: Any,
    page: Int,
    isCurrentPage: Boolean,
    transforms: PageTransforms,
    onClickCurrent: () -> Unit,
) {
  Box(
      modifier =
          Modifier.graphicsLayer {
                scaleX = transforms.scale
                scaleY = transforms.scale
                rotationY = transforms.rotationY
                cameraDistance = HuntCardScreenDefaults.ImageCarouselCameraDistanceFactor * density
              }
              .shadow(
                  elevation = HuntCardScreenDefaults.ImageCarouselShadowElevation,
                  shape = RoundedCornerShape(HuntCardScreenDefaults.ImageCarouselCornerRadius),
              )
              .clip(RoundedCornerShape(HuntCardScreenDefaults.ImageCarouselCornerRadius))
              .background(Color.White)
              .border(
                  width = HuntCardScreenDefaults.ImageCarouselWhiteFrame,
                  color = Color.White,
                  shape = RoundedCornerShape(HuntCardScreenDefaults.ImageCarouselCornerRadius),
              )
              .testTag(HuntCardScreenTestTags.IMAGE_PAGE_PREFIX + page)
              .clickable(enabled = isCurrentPage) { if (isCurrentPage) onClickCurrent() },
  ) {
    AsyncImage(
        model = image,
        contentDescription = HuntCardScreenStrings.HuntPicturePageDescriptionPrefix + page,
        modifier = Modifier.fillMaxSize(),
        placeholder = painterResource(R.drawable.empty_image),
        error = painterResource(R.drawable.empty_image),
        contentScale = ContentScale.Crop,
    )

    Box(
        modifier =
            Modifier.matchParentSize()
                .background(Color.Black.copy(alpha = transforms.overlayAlpha)),
    )
  }
}

@Composable
private fun HuntImageIndicators(
    imagesCount: Int,
    currentPage: Int,
) {
  if (imagesCount <= 1) return

  Spacer(modifier = Modifier.height(HuntCardScreenDefaults.ImageIndicatorTopPadding))

  Row(
      modifier = Modifier.testTag(HuntCardScreenTestTags.IMAGE_INDICATOR_ROW),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    repeat(imagesCount) { index ->
      val isSelected = index == currentPage

      Box(
          modifier =
              Modifier.size(
                      if (isSelected) HuntCardScreenDefaults.ImageIndicatorDotSelectedSize
                      else HuntCardScreenDefaults.ImageIndicatorDotSize,
                  )
                  .background(
                      color =
                          if (isSelected) HuntCardScreenDefaults.ImageIndicatorSelectedColor
                          else HuntCardScreenDefaults.ImageIndicatorUnselectedColor,
                      shape = CircleShape,
                  )
                  .testTag(HuntCardScreenTestTags.IMAGE_INDICATOR_DOT_PREFIX + index),
      )

      if (index != imagesCount - 1) {
        Spacer(modifier = Modifier.width(HuntCardScreenDefaults.ImageIndicatorDotSpacing))
      }
    }
  }
}
