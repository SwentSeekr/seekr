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
import androidx.compose.ui.unit.dp
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
 * @param authorName The name of the hunt author.
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
    rating: Double = 0.0,
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
            title = {
              Text(
                  text = title,
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold)
            },
            navigationIcon = {
              IconButton(
                  modifier = modifier.testTag(AddReviewScreenTestTags.GO_BACK_BUTTON),
                  onClick = onGoBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AddReviewScreenStrings.BackContentDescription)
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
            verticalArrangement = Arrangement.spacedBy(UICons.ColumnVArrangement)) {
              Spacer(modifier = modifier.height(UICons.SpacerHeightSmall))

              // --- Single header card: hunt title + author + rating ---
              Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(UICons.CardCornerRadius),
                  colors =
                      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                    Column(
                        modifier = Modifier.padding(UICons.ColumnPadding).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                          Text(
                              text = huntTitle,
                              style = MaterialTheme.typography.titleLarge,
                              fontWeight = FontWeight.Bold)

                          Text(
                              text = "${AddReviewScreenStrings.By} $authorName",
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant)

                          Spacer(modifier = Modifier.height(4.dp))

                          StarRatingBar(
                              rating = rating.toInt(),
                              maxStars = AddReviewScreenDefaults.MaxStars,
                              onRatingChanged = onRatingChanged)

                          AnimatedVisibility(
                              visible = rating >= 1.0,
                              enter = fadeIn() + scaleIn(),
                              exit = fadeOut() + scaleOut()) {
                                Text(
                                    AddReviewScreenStrings.ratingSummary(
                                        rating.toInt(), AddReviewScreenDefaults.MaxStars),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary)
                              }
                        }
                  }

              // --- Comment field: more "free" and ergonomic ---
              OutlinedTextField(
                  value = reviewText,
                  onValueChange = onReviewTextChanged,
                  modifier =
                      modifier
                          .fillMaxWidth()
                          .heightIn(min = UICons.CommentFieldHeight)
                          .testTag(AddReviewScreenTestTags.COMMENT_TEXT_FIELD),
                  label = { Text(AddReviewScreenStrings.CommentLabel) },
                  placeholder = { Text(AddReviewScreenStrings.CommentPlaceholder) },
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
                                text = "${reviewText.length} chars",
                                style = MaterialTheme.typography.bodySmall,
                                color =
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                          }
                    }
                  },
                  textStyle =
                      LocalTextStyle.current.copy(
                          lineHeight = AddReviewScreenDefaults.CommentLineHeight),
                  singleLine = false,
                  maxLines = AddReviewScreenDefaults.CommentMaxLines,
                  shape = RoundedCornerShape(UICons.CommentFieldCornerRadius),
                  colors =
                      OutlinedTextFieldDefaults.colors(
                          focusedBorderColor = MaterialTheme.colorScheme.primary,
                          unfocusedBorderColor =
                              MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                          focusedContainerColor = MaterialTheme.colorScheme.surface,
                          unfocusedContainerColor = MaterialTheme.colorScheme.surface))

              // --- Photos: "Add photos" when empty, row with + tile when not empty ---
              if (photos.isEmpty()) {
                FilledTonalButton(
                    onClick = onAddPhotos,
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(UICons.ButtonTonalHeight)
                            .testTag(AddReviewScreenTestTags.AddPhotoButtonTag),
                    shape = RoundedCornerShape(UICons.ButtonTonalCornerRadius)) {
                      Icon(
                          imageVector = Icons.Default.AddCircle,
                          contentDescription = AddReviewScreenStrings.AddPhotoContentDescription,
                          modifier = Modifier.size(UICons.IconSize))
                      Spacer(modifier = Modifier.padding(start = UICons.SpacerHeightSmall))
                      Text(
                          AddReviewScreenStrings.AddPicturesButtonLabel,
                          style = MaterialTheme.typography.titleSmall)
                    }
              } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Photos (${photos.size})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface)
                        TextButton(onClick = onAddPhotos) {
                          Text(
                              text = "Add more",
                              style = MaterialTheme.typography.labelMedium,
                              color = MaterialTheme.colorScheme.primary)
                        }
                      }

                  Spacer(modifier = Modifier.height(8.dp))

                  LazyRow(
                      horizontalArrangement = Arrangement.spacedBy(UICons.RowHArrangement),
                      modifier = Modifier.testTag("PhotosLazyRow")) {
                        items(photos.size) { index ->
                          Box {
                            AsyncImage(
                                model = photos[index],
                                contentDescription =
                                    "${AddReviewScreenStrings.SelectedImageContentDescriptionPrefix}$index",
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
                                        .testTag("RemovePhoto$index"),
                                shape = RoundedCornerShape(UICons.SurfaceCorners),
                                color = MaterialTheme.colorScheme.errorContainer) {
                                  Icon(
                                      imageVector = Icons.Default.Close,
                                      contentDescription =
                                          AddReviewScreenStrings.RemovePhotoContentDescription,
                                      tint = MaterialTheme.colorScheme.onErrorContainer,
                                      modifier =
                                          Modifier.padding(UICons.SurfacePadding).size(14.dp))
                                }
                          }
                        }

                        // trailing "+" tile to add more
                        item {
                          Box(
                              modifier =
                                  Modifier.size(UICons.ImageSize)
                                      .clip(RoundedCornerShape(UICons.ImageCorners))
                                      .background(
                                          MaterialTheme.colorScheme.surfaceVariant.copy(
                                              alpha = 0.6f))
                                      .clickable { onAddPhotos() },
                              contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription =
                                        AddReviewScreenStrings.AddPhotoContentDescription,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                              }
                        }
                      }
                }
              }

              Spacer(modifier = Modifier.height(UICons.SpacerHeightSmall))

              // --- Buttons row ---
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
                          Text(AddReviewScreenStrings.CancelButtonLabel)
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
                          Text(AddReviewScreenStrings.DoneButtonLabel)
                        }
                  }

              Spacer(modifier = Modifier.height(UICons.SpacerHeightMedium))
            }
      }
}
