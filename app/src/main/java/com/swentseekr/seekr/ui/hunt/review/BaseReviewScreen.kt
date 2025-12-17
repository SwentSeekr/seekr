package com.swentseekr.seekr.ui.hunt.review

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.swentseekr.seekr.R

/**
 * This composable represents the main review UI for a hunt. It provides:
 * - Display of hunt title and author.
 * - Star rating input with animation.
 * - Text field for review comments with validation support.
 * - Optional photo attachments with add/remove functionality.
 * - Action buttons for Cancel and Done.
 *
 * @param title The title displayed in the top app bar.
 * @param huntTitle The title of the hunt being reviewed.
 * @param authorName The name of the hunt author.val UICons = AddReviewScreenDefaults
 * @param rating Current rating value (0.0–MaxStars).
 * @param reviewText Current text of the review comment.
 * @param photos List of photo URLs to display in the review.
 * @param isReviewTextError True if the comment field has a validation error.
 * @param isDoneEnabled Enables the "Done" button when true.
 * @param reviewTextErrorMessage error message to display below the comment field.
 * @param onRatingChanged Callback invoked when the rating is changed.
 * @param onReviewTextChanged Callback invoked when the review text is changed.
 * @param onAddPhotos Callback invoked when the user taps the "Add Photos" button.
 * @param onRemovePhoto Callback invoked when the user removes a photo; provides the index.
 * @param onGoBack Callback invoked when the top app bar back button is pressed.
 * @param onCancel Callback invoked when the "Cancel" button is pressed.
 * @param onDone Callback invoked when the "Done" button is pressed.
 * @param modifier Optional [Modifier] for customization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseReviewScreen(
    title: String,
    huntTitle: String,
    authorName: String,
    rating: Double = AddReviewScreenDefaults.RATING,
    reviewText: String,
    photos: List<String> = emptyList(),
    isReviewTextError: Boolean = false,
    isDoneEnabled: Boolean = false,
    reviewTextErrorMessage: String? = "",
    onRatingChanged: (Int) -> Unit,
    onReviewTextChanged: (String) -> Unit,
    onAddPhotos: () -> Unit,
    onRemovePhoto: (Int) -> Unit,
    onGoBack: () -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
  Scaffold(
      topBar = {
        TopAppBar(
            title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
            navigationIcon = {
              IconButton(
                  modifier = modifier.testTag(AddReviewScreenTestTags.GO_BACK_BUTTON),
                  onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AddReviewScreenStrings.BACK_CONTENT_DESCRIPTION)
                  }
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface))
      },
      modifier = modifier.fillMaxWidth()) { innerPadding ->
        Column(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = UICons.ChangeAlpha))
                    .padding(innerPadding)
                    .padding(horizontal = UICons.ColumnPadding)
                    .verticalScroll(rememberScrollState())
                    .testTag(AddReviewScreenTestTags.INFO_COLUMN),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(UICons.SpacerHeightSmall)) {
              Spacer(modifier = modifier.height(UICons.SpacerHeightSmall))

              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(UICons.CardCornerRadius),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation =
                      CardDefaults.cardElevation(
                          defaultElevation = AddReviewScreenDefaults.CardElevation)) {
                    Column(
                        modifier = Modifier.padding(UICons.ColumnPadding).fillMaxWidth(),
                        verticalArrangement =
                            Arrangement.spacedBy(AddReviewScreenDefaults.HeaderInnerSpacing)) {
                          Text(
                              text = huntTitle,
                              style = MaterialTheme.typography.titleLarge,
                              fontWeight = FontWeight.Bold)

                          Text(
                              text = "${AddReviewScreenStrings.BY} $authorName",
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant)

                          Spacer(
                              modifier =
                                  Modifier.height(
                                      AddReviewScreenDefaults.HeaderSubtitleSpacerHeight))

                          StarRatingBar(
                              rating = rating.toInt(),
                              maxStars = AddReviewScreenDefaults.MAX_STARS,
                              onRatingChanged = onRatingChanged)

                          AnimatedVisibility(
                              visible =
                                  rating >= AddReviewScreenDefaults.FIRST_STAR_INDEX.toDouble(),
                              enter = fadeIn() + scaleIn(),
                              exit = fadeOut() + scaleOut()) {
                                Text(
                                    AddReviewScreenStrings.ratingSummary(
                                        rating.toInt(), AddReviewScreenDefaults.MAX_STARS),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary)
                              }
                        }
                  }

              OutlinedTextField(
                  value = reviewText,
                  onValueChange = onReviewTextChanged,
                  modifier =
                      modifier
                          .fillMaxWidth()
                          .heightIn(min = UICons.CommentFieldHeight)
                          .testTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD),
                  label = { Text(AddReviewScreenStrings.COMMENT_LABEL) },
                  placeholder = { Text(AddReviewScreenStrings.COMMENT_PLACEHOLDER) },
                  isError = isReviewTextError,
                  supportingText = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                      AnimatedVisibility(
                          visible = isReviewTextError && reviewTextErrorMessage != null,
                          enter = fadeIn(),
                          exit = fadeOut()) {
                            Text(
                                reviewTextErrorMessage.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                          }

                      Row(
                          modifier = Modifier.fillMaxWidth(),
                          horizontalArrangement = Arrangement.End) {
                            Text(
                                text = AddReviewScreenStrings.commentLengthLabel(reviewText.length),
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = AddReviewScreenDefaults.COMMENT_CHAR_COUNT_ALPHA))
                          }
                    }
                  },
                  textStyle =
                      LocalTextStyle.current.copy(
                          lineHeight = AddReviewScreenDefaults.CommentLineHeight),
                  singleLine = false,
                  maxLines = AddReviewScreenDefaults.COMMENT_MAX_LINES,
                  shape = RoundedCornerShape(UICons.CommentFieldCornerRadius),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          focusedBorderColor = MaterialTheme.colorScheme.primary,
                          unfocusedBorderColor =
                              MaterialTheme.colorScheme.outline.copy(alpha = UICons.ChangeAlpha),
                          focusedContainerColor = MaterialTheme.colorScheme.surface,
                          unfocusedContainerColor = MaterialTheme.colorScheme.surface))

              if (photos.isEmpty()) {
                FilledTonalButton(
                    onClick = onAddPhotos,
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(UICons.ButtonTonalHeight)
                            .testTag(AddReviewScreenTestTags.ADD_PHOTO_BUTTON_TEST_TAG),
                    shape = RoundedCornerShape(UICons.ButtonTonalCornerRadius)) {
                      Icon(
                          imageVector = Icons.Default.AddCircle,
                          contentDescription = AddReviewScreenStrings.ADD_PHOTO_CONTENT_DESCRIPTION,
                          modifier = Modifier.size(UICons.IconSize))
                      Spacer(modifier = Modifier.width(UICons.SpacerHeightSmall))
                      Text(
                          AddReviewScreenStrings.ADD_PICTURES_BUTTON_LABEL,
                          style = MaterialTheme.typography.titleSmall)
                    }
              } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = AddReviewScreenStrings.photosHeader(photos.size),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        TextButton(onClick = onAddPhotos) {
                          Text(
                              text = AddReviewScreenStrings.ADD_MORE_PHOTOS_BUTTON_LABEL,
                              style = MaterialTheme.typography.labelMedium,
                              color = MaterialTheme.colorScheme.primary)
                        }
                      }

                  Spacer(modifier = Modifier.height(AddReviewScreenDefaults.PhotosSpacerHeight))

                  LazyRow(
                      horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement),
                      modifier = Modifier.testTag(AddReviewScreenTestTags.PHOTOS_LAZY_ROW_TAG)) {
                        items(photos.size) { index ->
                          Box {
                            AsyncImage(
                                model = photos[index],
                                contentDescription =
                                    "${AddReviewScreenStrings.SELECTED_IMAGE_CONTENT_DESCRIPTION_PREFIX}$index",
                                modifier =
                                    Modifier.size(UICons.ImageSize)
                                        .clip(RoundedCornerShape(UICons.ImageCorners))
                                        .shadow(
                                            UICons.ImageShadow,
                                            RoundedCornerShape(UICons.ImageCorners)),
                                placeholder = painterResource(R.drawable.empty_image),
                                error = painterResource(R.drawable.empty_image))

                            Surface(
                                modifier =
                                    Modifier.align(Alignment.TopEnd)
                                        .padding(UICons.SurfacePadding)
                                        .size(UICons.SurfaceSize)
                                        .clickable { onRemovePhoto(index) }
                                        .testTag(AddReviewScreenTestTags.removePhotoTag(index)),
                                shape = RoundedCornerShape(UICons.SurfaceCorners),
                                color = MaterialTheme.colorScheme.errorContainer) {
                                  Icon(
                                      imageVector = Icons.Default.Close,
                                      contentDescription =
                                          AddReviewScreenStrings.REMOVE_PHOTO_CONTENT_DESCRIPTION,
                                      tint = MaterialTheme.colorScheme.onErrorContainer,
                                      modifier =
                                          Modifier.padding(UICons.SurfacePadding)
                                              .size(UICons.SurfaceIconSize))
                                }
                          }
                        }

                        item {
                          Box(
                              modifier =
                                  Modifier.size(UICons.ImageSize)
                                      .clip(RoundedCornerShape(UICons.ImageCorners))
                                      .background(
                                          MaterialTheme.colorScheme.surfaceVariant.copy(
                                              alpha = UICons.TrailingTileAlpha))
                                      .clickable { onAddPhotos() },
                              contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription =
                                        AddReviewScreenStrings.ADD_PHOTO_CONTENT_DESCRIPTION,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                              }
                        }
                      }
                }
              }

              Spacer(modifier = Modifier.height(UICons.SpacerHeightSmall))

              // Buttons row
              Row(
                  modifier = modifier.fillMaxWidth().testTag(AddReviewScreenTestTags.BUTTONS_ROW),
                  horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement)) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier =
                            modifier
                                .weight(UICons.ButtonWeight)
                                .height(UICons.ButtonTonalHeight)
                                .testTag(AddReviewScreenTestTags.CANCEL_BUTTON),
                        shape = RoundedCornerShape(UICons.ButtonTonalCornerRadius)) {
                          Text(AddReviewScreenStrings.CANCEL_BUTTON_LABEL)
                        }
                    Button(
                        onClick = onDone,
                        enabled = isDoneEnabled,
                        modifier =
                            modifier
                                .weight(UICons.ButtonWeight)
                                .height(UICons.ButtonTonalHeight)
                                .testTag(AddReviewScreenTestTags.DONE_BUTTON),
                        shape = RoundedCornerShape(UICons.ButtonTonalCornerRadius),
                        elevation =
                            ButtonDefaults.buttonElevation(
                                defaultElevation = UICons.ButtonElevation)) {
                          Text(AddReviewScreenStrings.DONE_BUTTON_LABEL)
                        }
                  }

              Spacer(modifier = Modifier.height(UICons.SpacerHeightMedium))
            }
      }
}
