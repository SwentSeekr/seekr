package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.ui.components.HuntCardScreenDefaults.IMAGE_CAROUSEL_ROTATION_CENTER_DEGREES
import com.swentseekr.seekr.ui.components.HuntCardScreenDefaults.IMAGE_INDICATOR_LAST_INDEX_OFFSET
import com.swentseekr.seekr.ui.theme.Black
import kotlin.math.absoluteValue

/**
 * High-level image carousel for a hunt.
 *
 * Responsibilities:
 * - Build the list of images from `mainImageUrl` + `otherImagesUrls` with a safe fallback.
 * - Manage pager state and full-screen dialog visibility.
 * - Delegate layout responsibilities to:
 *     - [HuntImagePager] for the paged images.
 *     - [HuntImageIndicators] for the dot indicator row.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HuntImageCarousel(
    hunt: com.swentseekr.seekr.model.hunt.Hunt,
    modifier: Modifier = Modifier,
) {
  // Build the list of images once per hunt instance.
  // The main image is first; if missing, we fall back to an empty placeholder drawable.
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

  // Pager state drives the current page and offset for transforms.
  val pagerState = rememberPagerState(pageCount = { images.size })

  // Local state controlling whether the full-screen dialog is visible.
  var showFullScreen by remember { mutableStateOf(false) }

  // Full-screen image dialog, rendered only when requested.
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
      // Main horizontal image pager with 3D transforms and overlay.
      HuntImagePager(
          images = images,
          pagerState = pagerState,
          onCurrentPageClicked = { showFullScreen = true },
      )

      // Dot indicators below the pager, rendered only when multiple images are present.
      HuntImageIndicators(
          imagesCount = images.size,
          currentPage = pagerState.currentPage,
      )
    }
  }
}

/**
 * Full-screen dialog to display the currently selected image.
 *
 * This is intentionally separated from [HuntImageCarousel] to:
 * - Keep the main composable easier to read.
 * - Make the dialog behavior easier to test and maintain.
 */
@Composable
private fun HuntImageFullScreenDialog(
    showFullScreen: Boolean,
    currentImage: Any,
    onDismiss: () -> Unit,
) {
  // Early-return to avoid composing a Dialog when not needed.
  if (!showFullScreen) return

  Dialog(onDismissRequest = onDismiss) {
    Box(
        modifier = Modifier.fillMaxSize().background(Black),
        contentAlignment = Alignment.Center,
    ) {
      AsyncImage(
          model = currentImage,
          contentDescription = HuntCardScreenStrings.FULL_SCREEN_DESCRIPTION,
          modifier =
              Modifier.fillMaxWidth()
                  .fillMaxHeight(HuntCardScreenDefaults.FULL_SCREEN_IMAGE_HEIGHT_FRACTION)
                  .testTag(HuntCardScreenTestTags.IMAGE_FULLSCREEN),
          placeholder = painterResource(R.drawable.empty_image),
          error = painterResource(R.drawable.empty_image),
          contentScale = ContentScale.Crop,
      )
    }
  }
}

/**
 * Horizontal pager hosting each image page with the carousel effect.
 *
 * This composable:
 * - Applies a fixed height from [HuntCardScreenDefaults.ImageCarouselHeight].
 * - Delegates per-page visuals (scale, rotation, overlay) to [HuntImagePage].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HuntImagePager(
    images: List<Any>,
    pagerState: PagerState,
    onCurrentPageClicked: () -> Unit,
) {
  HorizontalPager(
      state = pagerState,
      modifier = Modifier.fillMaxSize().testTag(HuntCardScreenTestTags.IMAGE_PAGER),
  ) { page ->
    // Compute animations / transforms based on current page and scroll offset.
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

/**
 * Precomputed 3D transform values for a single pager page.
 *
 * @property scale The scale factor applied to the page.
 * @property rotationY The Y-axis rotation angle used for the "perspective" effect.
 * @property overlayAlpha Alpha used by the dark overlay (stronger on non-centered pages).
 */
private data class PageTransforms(
    val scale: Float,
    val rotationY: Float,
    val overlayAlpha: Float,
)

/**
 * Computes the visual transforms (scale, rotation, overlay) for a given page.
 *
 * This function:
 * - Derives a normalized offset from the current page.
 * - Interpolates between min and max values defined in [HuntCardScreenDefaults].
 * - Is separated from [HuntImagePager] for clarity and easier reuse/testing.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun rememberPageTransforms(
    page: Int,
    pagerState: PagerState,
): PageTransforms {
  // Distance of this page from the currently selected page (0 = center).
  val pageOffset =
      (pagerState.currentPage - page + pagerState.currentPageOffsetFraction).absoluteValue

  // Clamp offset to the expected interpolation range [0, 1].
  val clampedOffset =
      pageOffset.coerceIn(
          HuntCardScreenDefaults.IMAGE_CAROUSEL_INTERPOLATION_MIN_FRACTION,
          HuntCardScreenDefaults.IMAGE_CAROUSEL_INTERPOLATION_MAX_FRACTION,
      )

  // Fraction increases as the page gets closer to the center (1f = fully centered).
  val interpolationFraction =
      HuntCardScreenDefaults.IMAGE_CAROUSEL_INTERPOLATION_MAX_FRACTION - clampedOffset

  // Scale: center image is larger, side images are slightly smaller.
  val scale =
      lerp(
          start = HuntCardScreenDefaults.IMAGE_CAROUSEL_MIN_SCALE,
          stop = HuntCardScreenDefaults.IMAGE_CAROUSEL_MAX_SCALE,
          fraction = interpolationFraction,
      )

  // Rotation: pages rotate around the Y axis to create a "carousel" perspective effect.
  val sideRotation =
      if (page < pagerState.currentPage) {
        HuntCardScreenDefaults.IMAGE_CAROUSEL_SIDE_ROTATION_DEGREE
      } else {
        -HuntCardScreenDefaults.IMAGE_CAROUSEL_SIDE_ROTATION_DEGREE
      }

  val rotationY =
      lerp(
          start = sideRotation,
          stop = IMAGE_CAROUSEL_ROTATION_CENTER_DEGREES,
          fraction = interpolationFraction,
      )

  // Overlay: side images are darker, center image has no overlay.
  val overlayAlpha =
      lerp(
          start = HuntCardScreenDefaults.IMAGE_CAROUSEL_OVERLAYER_MAX_ALPHA,
          stop = HuntCardScreenDefaults.IMAGE_CAROUSEL_OVERLAYER_MIN_ALPHA,
          fraction = interpolationFraction,
      )

  return PageTransforms(
      scale = scale,
      rotationY = rotationY,
      overlayAlpha = overlayAlpha,
  )
}

/**
 * Single page in the image carousel.
 *
 * Responsibilities:
 * - Render the image with scale / rotation / overlay transforms.
 * - Expose a clickable surface **only** when this is the current page.
 * - Provide test tags for UI tests (`IMAGE_PAGE_PREFIX + page`).
 */
@Composable
private fun HuntImagePage(
    image: Any,
    page: Int,
    isCurrentPage: Boolean,
    transforms: PageTransforms,
    onClickCurrent: () -> Unit,
) {
  // Compute camera distance in px from dp using the current density.
  val density = LocalDensity.current
  val cameraDistancePx =
      with(density) { HuntCardScreenDefaults.IMAGE_CAROUSEL_DISTANCE_FACTOR.dp.toPx() }

  Box(
      modifier =
          Modifier.graphicsLayer {
                scaleX = transforms.scale
                scaleY = transforms.scale
                rotationY = transforms.rotationY
                cameraDistance = cameraDistancePx
              }
              .fillMaxSize()
              .testTag(HuntCardScreenTestTags.IMAGE_PAGE_PREFIX + page)
              .clickable(enabled = isCurrentPage) { onClickCurrent() }) {

        // IMAGE
        AsyncImage(
            model = image,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            placeholder = painterResource(R.drawable.empty_image),
            error = painterResource(R.drawable.empty_image),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = HuntCardScreenDefaults.BACKGROUND_ALPHA)),
                            startY = HuntCardScreenDefaults.START))) {}
      }
}

/**
 * Row of dot indicators representing each page in the carousel.
 *
 * Behavior:
 * - Renders one dot per image.
 * - Uses a larger, darker dot for the currently selected page.
 * - Hides the entire row when there is only one image.
 */
@Composable
private fun HuntImageIndicators(
    imagesCount: Int,
    currentPage: Int,
) {
  // No indicator when there is only a single image.
  if (imagesCount <= IMAGE_INDICATOR_LAST_INDEX_OFFSET) return

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

      // Add spacing between dots except after the last one.
      if (index != imagesCount - IMAGE_INDICATOR_LAST_INDEX_OFFSET) {
        Spacer(modifier = Modifier.width(HuntCardScreenDefaults.ImageIndicatorDotSpacing))
      }
    }
  }
}
