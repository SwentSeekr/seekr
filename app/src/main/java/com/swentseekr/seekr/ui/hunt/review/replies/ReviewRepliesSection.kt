package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swentseekr.seekr.model.hunt.review.HuntReviewReply
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesValues.AuthorIdMaxLength
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesValues.SingleReplyCount

/** Small container so composables don't have 10 parameters each. */
data class ReviewRepliesCallbacks(
    val onToggleRootReplies: () -> Unit,
    val onRootReplyTextChanged: (String) -> Unit,
    val onSendRootReply: () -> Unit,
    val onReplyAction: (ReplyTarget) -> Unit,
    val onToggleReplyThread: (String) -> Unit,
    val onReplyTextChanged: (ReplyTarget, String) -> Unit,
    val onSendReply: (ReplyTarget) -> Unit,
    val onDeleteReply: (String) -> Unit,
)

data class RedditComposerState(
    val text: String,
    val isExpanded: Boolean,
    val isSending: Boolean,
)

data class RedditComposerConfig(
    val placeholder: String,
    val compact: Boolean,
    val onTextChanged: (String) -> Unit,
    val onSend: () -> Unit,
    val onExpandChange: ((Boolean) -> Unit)? = null,
)

/**
 * Entry-point composable that is connected to the ViewModel.
 *
 * Call this from your review card with the appropriate ViewModel instance.
 */
@Composable
fun ReviewRepliesSection(
    viewModel: ReviewRepliesViewModel,
    modifier: Modifier = Modifier,
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(viewModel) { viewModel.start() }

  val callbacks =
      remember(viewModel, state.reviewId) {
        ReviewRepliesCallbacks(
            onToggleRootReplies = { viewModel.onToggleReplies(null) },
            onRootReplyTextChanged = { text ->
              viewModel.onReplyTextChanged(
                  ReplyTarget.RootReview(reviewId = state.reviewId),
                  text,
              )
            },
            onSendRootReply = {
              viewModel.sendReply(
                  ReplyTarget.RootReview(reviewId = state.reviewId),
              )
            },
            onReplyAction = { target -> viewModel.onToggleComposer(target) },
            onToggleReplyThread = { replyId -> viewModel.onToggleReplies(replyId) },
            onReplyTextChanged = { target, text -> viewModel.onReplyTextChanged(target, text) },
            onSendReply = { target -> viewModel.sendReply(target) },
            onDeleteReply = { replyId -> viewModel.deleteReply(replyId) },
        )
      }

  ReviewRepliesSectionContent(
      state = state,
      callbacks = callbacks,
      modifier = modifier,
  )
}

/** Pure UI, driven by state + callbacks (no ViewModel knowledge here). */
@Composable
fun ReviewRepliesSectionContent(
    state: ReviewRepliesUiState,
    callbacks: ReviewRepliesCallbacks,
    modifier: Modifier = Modifier,
) {
  var rootComposerExpanded by remember { mutableStateOf(false) }

  Column(
      modifier = modifier.fillMaxWidth().testTag(ReviewRepliesTestTags.REPLIES_SECTION),
  ) {
    if (!state.isRootExpanded) {
      ReviewRepliesCollapsedHeader(
          state = state,
          onToggleRootReplies = callbacks.onToggleRootReplies,
      )
    }

    if (state.isRootExpanded) {
      ReviewRepliesExpandedContent(
          state = state,
          callbacks = callbacks,
          rootComposerExpanded = rootComposerExpanded,
          onRootComposerExpandedChange = { rootComposerExpanded = it },
      )
    }
  }
}

@Composable
fun ReviewRepliesCollapsedHeader(
    state: ReviewRepliesUiState,
    onToggleRootReplies: () -> Unit,
) {
  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .clickable { onToggleRootReplies() }
              .testTag(ReviewRepliesTestTags.ROOT_SEE_REPLIES),
      color = Color.Transparent) {
        Row(
            modifier =
                Modifier.padding(
                    vertical = ReviewRepliesDimensions.RootHeaderVerticalPadding,
                    horizontal = ReviewRepliesDimensions.RootHeaderHorizontalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.Filled.Send,
                  contentDescription = null,
                  tint =
                      MaterialTheme.colorScheme.primary.copy(
                          alpha = ReviewRepliesAlphas.RootHeaderIcon),
                  modifier = Modifier.size(ReviewRepliesDimensions.RootHeaderIconSize))
              Spacer(modifier = Modifier.width(ReviewRepliesDimensions.RootHeaderIconSpacing))
              Text(
                  text = replyCountLabel(state.totalReplyCount),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Medium,
                  color = MaterialTheme.colorScheme.primary)
            }
      }
}

@Composable
fun ReviewRepliesExpandedContent(
    state: ReviewRepliesUiState,
    callbacks: ReviewRepliesCallbacks,
    rootComposerExpanded: Boolean,
    onRootComposerExpandedChange: (Boolean) -> Unit,
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Spacer(modifier = Modifier.height(ReviewRepliesDimensions.RootComposerTopSpacing))

    val composerState =
        RedditComposerState(
            text = state.rootReplyText,
            isExpanded = rootComposerExpanded,
            isSending = state.isSendingReply,
        )

    val composerConfig =
        RedditComposerConfig(
            placeholder = ReviewRepliesStrings.RootComposerPlaceholder,
            compact = false,
            onTextChanged = callbacks.onRootReplyTextChanged,
            onSend = callbacks.onSendRootReply,
            onExpandChange = onRootComposerExpandedChange,
        )

    RedditStyleComposer(
        state = composerState,
        config = composerConfig,
        modifier = Modifier.testTag(ReviewRepliesTestTags.ROOT_INLINE_COMPOSER),
    )

    state.errorMessage?.let { msg ->
      Spacer(modifier = Modifier.height(ReviewRepliesDimensions.RootErrorTopSpacing))
      Text(
          text = msg,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.padding(horizontal = ReviewRepliesDimensions.ErrorHorizontalPadding))
    }

    if (state.replies.isNotEmpty()) {
      Spacer(modifier = Modifier.height(ReviewRepliesDimensions.ThreadTopSpacing))
      RedditThreadList(
          items = state.replies,
          state = state,
          callbacks = callbacks,
          modifier = Modifier.testTag(ReviewRepliesTestTags.THREAD_LIST),
      )
    }
  }
}

/** Reddit-style thread list with clean vertical lines */
@Composable
fun RedditThreadList(
    items: List<ReplyNodeUiState>,
    state: ReviewRepliesUiState,
    callbacks: ReviewRepliesCallbacks,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(ReviewRepliesDimensions.ThreadLineCornerRadius)) {
        items.forEach { node ->
          val replyText = state.childReplyTexts[node.reply.replyId]
          RedditReplyItem(
              node = node,
              replyText = replyText,
              callbacks = callbacks,
          )
        }
      }
}

/** Individual Reddit-style reply with elegant thread lines */
@Composable
fun RedditReplyItem(
    node: ReplyNodeUiState,
    replyText: String?,
    callbacks: ReviewRepliesCallbacks,
    modifier: Modifier = Modifier,
) {
  val reply = node.reply
  var composerExpanded by remember { mutableStateOf(false) }
  val depthIndent = node.depth * ReviewRepliesDimensions.DepthIndentPerLevel

  Row(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(start = depthIndent)
              .testTag("${ReviewRepliesTestTags.REPLY_ITEM_PREFIX}${reply.replyId}"),
      horizontalArrangement = Arrangement.Start) {
        if (node.depth > ReviewRepliesValues.RootDepth) {
          ReplyThreadLine()
        }

        Column(
            modifier =
                Modifier.weight(ReviewRepliesValues.FullWeight)
                    .padding(bottom = ReviewRepliesDimensions.ReplyVerticalSpacing)) {
              ReplyCard(
                  node = node,
                  replyText = replyText,
                  composerExpanded = composerExpanded,
                  onComposerExpandedChange = { composerExpanded = it },
                  callbacks = callbacks,
              )
            }
      }
}

@Composable
fun ReplyThreadLine() {
  Box(
      modifier =
          Modifier.width(ReviewRepliesDimensions.ThreadLineWidth)
              .height(IntrinsicSize.Min)
              .background(
                  MaterialTheme.colorScheme.outlineVariant.copy(
                      alpha = ReviewRepliesAlphas.OutlineVariant),
                  shape = RoundedCornerShape(ReviewRepliesDimensions.ThreadLineCornerRadius)))
  Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ThreadLineHorizontalSpacing))
}

@Composable
fun ReplyCard(
    node: ReplyNodeUiState,
    replyText: String?,
    composerExpanded: Boolean,
    onComposerExpandedChange: (Boolean) -> Unit,
    callbacks: ReviewRepliesCallbacks,
) {
  val reply = node.reply

  Column(
      modifier =
          Modifier.fillMaxWidth()
              .background(
                  MaterialTheme.colorScheme.surface,
                  shape = RoundedCornerShape(ReviewRepliesDimensions.ReplyCardCornerRadius))
              .padding(ReviewRepliesDimensions.ReplyCardPadding)) {
        ReplyHeader(node = node, callbacks = callbacks)

        Spacer(modifier = Modifier.height(ReviewRepliesDimensions.ReplyHeaderBottomSpacing))

        ReplyBody(reply = reply)

        Spacer(modifier = Modifier.height(ReviewRepliesDimensions.ReplyTextBottomSpacing))

        ReplyActions(
            node = node,
            composerExpanded = composerExpanded,
            onComposerExpandedChange = onComposerExpandedChange,
            callbacks = callbacks,
        )

        if (node.isComposerOpen && !reply.isDeleted) {
          InlineReplyComposer(
              reply = reply,
              replyText = replyText.orEmpty(),
              composerExpanded = composerExpanded,
              onComposerExpandedChange = onComposerExpandedChange,
              callbacks = callbacks,
          )
        }
      }
}

@Composable
fun ReplyHeader(
    node: ReplyNodeUiState,
    callbacks: ReviewRepliesCallbacks,
) {
  val reply = node.reply

  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    ReplyAvatar(node = node)

    Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ReplyAvatarNameSpacing))

    Text(
        text = authorLabel(node),
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface)

    Text(
        text = ReviewRepliesStrings.JustNow,
        style = MaterialTheme.typography.bodySmall,
        color =
            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = ReviewRepliesAlphas.ReplyTimestamp))

    Spacer(modifier = Modifier.weight(ReviewRepliesValues.FullWeight))

    if (node.isMine && !reply.isDeleted) {
      IconButton(
          onClick = { callbacks.onDeleteReply(reply.replyId) },
          modifier =
              Modifier.size(ReviewRepliesDimensions.DeleteButtonSize)
                  .testTag("${ReviewRepliesTestTags.REPLY_DELETE_BUTTON_PREFIX}${reply.replyId}")) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = ReviewRepliesStrings.DeleteContentDescription,
                tint =
                    MaterialTheme.colorScheme.error.copy(
                        alpha = ReviewRepliesAlphas.DeleteIconAlpha),
                modifier = Modifier.size(ReviewRepliesDimensions.DeleteIconSize))
          }
    }
  }
}

@Composable
fun ReplyAvatar(node: ReplyNodeUiState) {
  val reply = node.reply
  val avatarText =
      if (node.isMine) ReviewRepliesStrings.You
      else reply.authorId.take(SingleReplyCount).uppercase()

  Box(
      modifier =
          Modifier.size(ReviewRepliesDimensions.ReplyAvatarSize)
              .clip(CircleShape)
              .background(
                  if (node.isMine) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surfaceVariant),
      contentAlignment = Alignment.Center) {
        Text(
            text = avatarText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (node.isMine) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = ReviewRepliesDimensions.ReplyAvatarFontSize)
      }
}

@Composable
fun ReplyBody(reply: HuntReviewReply) {
  Text(
      text = if (reply.isDeleted) ReviewRepliesStrings.Deleted else reply.comment,
      style = MaterialTheme.typography.bodyMedium,
      color =
          if (reply.isDeleted)
              MaterialTheme.colorScheme.onSurfaceVariant.copy(
                  alpha = ReviewRepliesAlphas.DeletedReply)
          else MaterialTheme.colorScheme.onSurface,
      lineHeight = ReviewRepliesDimensions.ReplyTextLineHeight)
}

@Composable
fun ReplyActions(
    node: ReplyNodeUiState,
    composerExpanded: Boolean,
    onComposerExpandedChange: (Boolean) -> Unit,
    callbacks: ReviewRepliesCallbacks,
) {
  val reply = node.reply

  Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically) {
        ReplyButton(
            onClick = {
              onComposerExpandedChange(!composerExpanded)
              callbacks.onReplyAction(
                  ReplyTarget.Reply(reviewId = reply.reviewId, parentReplyId = reply.replyId))
            })

        if (node.totalChildrenCount > ReviewRepliesValues.RootDepth) {
          RepliesToggleButton(
              node = node,
              onToggle = { callbacks.onToggleReplyThread(reply.replyId) },
          )
        }
      }
}

@Composable
fun ReplyButton(onClick: () -> Unit) {
  TextButton(
      onClick = onClick,
      contentPadding =
          PaddingValues(
              horizontal = ReviewRepliesDimensions.ReplyTextButtonHorizontalPadding,
              vertical = ReviewRepliesDimensions.ReplyTextButtonVerticalPadding),
      colors =
          ButtonDefaults.textButtonColors(
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
        Icon(
            imageVector = Icons.Filled.Send,
            contentDescription = ReviewRepliesStrings.ReplyContentDescription,
            modifier = Modifier.size(ReviewRepliesDimensions.ReplyButtonIconSize))
        Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ReplyButtonIconSpacing))
        Text(
            text = ReviewRepliesStrings.Reply,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium)
      }
}

@Composable
fun RepliesToggleButton(
    node: ReplyNodeUiState,
    onToggle: () -> Unit,
) {
  val reply = node.reply

  TextButton(
      onClick = onToggle,
      contentPadding =
          PaddingValues(
              horizontal = ReviewRepliesDimensions.ReplyTextButtonHorizontalPadding,
              vertical = ReviewRepliesDimensions.ReplyTextButtonVerticalPadding),
      colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
      modifier =
          Modifier.testTag("${ReviewRepliesTestTags.REPLY_SEE_REPLIES_PREFIX}${reply.replyId}"),
  ) {
    Icon(
        imageVector =
            if (node.isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
        contentDescription = null,
        modifier = Modifier.size(ReviewRepliesDimensions.ReplyButtonIconSize))
    Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ReplyButtonIconSpacing))
    Text(
        text = childRepliesLabel(node.isExpanded, node.totalChildrenCount),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium)
  }
}

@Composable
fun InlineReplyComposer(
    reply: HuntReviewReply,
    replyText: String,
    composerExpanded: Boolean,
    onComposerExpandedChange: (Boolean) -> Unit,
    callbacks: ReviewRepliesCallbacks,
) {
  Spacer(modifier = Modifier.height(ReviewRepliesDimensions.ReplyTextBottomSpacing))

  val target =
      ReplyTarget.Reply(
          reviewId = reply.reviewId,
          parentReplyId = reply.replyId,
      )

  val composerState =
      RedditComposerState(
          text = replyText,
          isExpanded = composerExpanded,
          isSending = false,
      )

  val composerConfig =
      RedditComposerConfig(
          placeholder = ReviewRepliesStrings.InlineReplyPlaceholder,
          compact = true,
          onTextChanged = { newText -> callbacks.onReplyTextChanged(target, newText) },
          onSend = { callbacks.onSendReply(target) },
          onExpandChange = onComposerExpandedChange,
      )

  RedditStyleComposer(
      state = composerState,
      config = composerConfig,
      modifier =
          Modifier.testTag("${ReviewRepliesTestTags.REPLY_INLINE_COMPOSER_PREFIX}${reply.replyId}"),
  )
}

/** Reddit-style composer with clean design */
@Composable
fun RedditStyleComposer(
    state: RedditComposerState,
    config: RedditComposerConfig,
    modifier: Modifier = Modifier,
) {
  if (!config.compact && !state.isExpanded && config.onExpandChange != null) {
    CollapsedComposerButton(
        placeholder = config.placeholder,
        onExpand = { config.onExpandChange.invoke(true) },
        modifier = modifier,
    )
  } else {
    ExpandedComposerContent(
        state = state,
        config = config,
        modifier = modifier,
    )
  }
}

@Composable
fun CollapsedComposerButton(
    placeholder: String,
    onExpand: () -> Unit,
    modifier: Modifier,
) {
  OutlinedButton(
      onClick = onExpand,
      modifier =
          modifier
              .fillMaxWidth()
              .padding(horizontal = ReviewRepliesDimensions.CollapsedComposerHorizontalPadding)
              .height(ReviewRepliesDimensions.CollapsedComposerHeight),
      colors =
          ButtonDefaults.outlinedButtonColors(
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      border =
          BorderStroke(
              width = ReviewRepliesDimensions.OutlineBorderWidth,
              color = MaterialTheme.colorScheme.outlineVariant,
          ),
      shape = RoundedCornerShape(ReviewRepliesDimensions.CollapsedComposerCornerRadius)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = placeholder,
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                      MaterialTheme.colorScheme.onSurfaceVariant.copy(
                          alpha = ReviewRepliesAlphas.RootHeaderPlaceholder))
            }
      }
}

@Composable
fun ExpandedComposerContent(
    state: RedditComposerState,
    config: RedditComposerConfig,
    modifier: Modifier,
) {
  val outlineColor =
      MaterialTheme.colorScheme.outlineVariant.copy(
          alpha = ReviewRepliesAlphas.OutlineVariantBorder)

  Surface(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(
                  horizontal =
                      if (config.compact)
                          ReviewRepliesDimensions.ExpandedComposerCompactHorizontalPadding
                      else ReviewRepliesDimensions.ExpandedComposerHorizontalPadding),
      shape = RoundedCornerShape(ReviewRepliesDimensions.ExpandedComposerCornerRadius),
      color =
          MaterialTheme.colorScheme.surfaceVariant.copy(
              alpha = ReviewRepliesAlphas.ComposerSurfaceAlpha),
      border =
          BorderStroke(
              width = ReviewRepliesDimensions.OutlineBorderWidth,
              color = outlineColor,
          )) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = ReviewRepliesDimensions.ExpandedComposerContentHorizontalPadding,
                    vertical = ReviewRepliesDimensions.ExpandedComposerContentVerticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(ReviewRepliesDimensions.InlineComposerHorizontalSpacing)) {

              // ⬅️ weight is applied HERE, inside the RowScope
              ComposerTextField(
                  state = state,
                  config = config,
                  modifier =
                      Modifier.weight(ReviewRepliesValues.FullWeight)
                          .testTag(ReviewRepliesTestTags.ROOT_INLINE_TEXT_FIELD),
              )

              if (state.isSending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(ReviewRepliesDimensions.SendProgressSize),
                    strokeWidth = ReviewRepliesDimensions.SendProgressStrokeWidth,
                    color = MaterialTheme.colorScheme.primary)
              } else {
                ComposerSendButton(state = state, config = config)
              }
            }
      }
}

@Composable
fun ComposerTextField(
    state: RedditComposerState,
    config: RedditComposerConfig,
    modifier: Modifier = Modifier,
) {
  OutlinedTextField(
      value = state.text,
      onValueChange = config.onTextChanged,
      modifier = modifier,
      placeholder = {
        Text(
            text = config.placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = ReviewRepliesAlphas.ComposerPlaceholder))
      },
      singleLine = true,
      textStyle = MaterialTheme.typography.bodyMedium,
      colors =
          OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent,
              focusedContainerColor = Color.Transparent,
              unfocusedContainerColor = Color.Transparent),
  )
}

@Composable
fun ComposerSendButton(
    state: RedditComposerState,
    config: RedditComposerConfig,
) {
  val isActive = state.text.isNotBlank()

  IconButton(
      onClick = {
        if (isActive) {
          config.onSend()
          config.onExpandChange?.invoke(false)
        }
      },
      enabled = isActive,
      modifier =
          Modifier.size(ReviewRepliesDimensions.SendButtonOuterSize)
              .testTag(ReviewRepliesTestTags.ROOT_INLINE_SEND_BUTTON)) {
        Surface(
            shape = CircleShape,
            color =
                if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(ReviewRepliesDimensions.SendButtonInnerSize)) {
              Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = ReviewRepliesStrings.SendContentDescription,
                    tint =
                        if (isActive) MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = ReviewRepliesAlphas.InactiveSendIcon),
                    modifier = Modifier.size(ReviewRepliesDimensions.SendIconSize))
              }
            }
      }
}

/* ---------- Pure helpers (no Compose) ---------- */

private fun replyCountLabel(totalCount: Int): String {
  return if (totalCount > ReviewRepliesValues.RootDepth) {
    val unit =
        if (totalCount == ReviewRepliesValues.RootDepth + SingleReplyCount)
            ReviewRepliesStrings.ReplyUnitSingular
        else ReviewRepliesStrings.ReplyUnitPlural
    "$totalCount $unit"
  } else {
    ReviewRepliesStrings.BeTheFirstToReply
  }
}

private fun childRepliesLabel(isExpanded: Boolean, totalChildren: Int): String {
  if (isExpanded) return ReviewRepliesStrings.HideReplies

  val unit =
      if (totalChildren == ReviewRepliesValues.RootDepth + SingleReplyCount)
          ReviewRepliesStrings.ReplyUnitSingular
      else ReviewRepliesStrings.ReplyUnitPlural

  return "$totalChildren $unit"
}

private fun authorLabel(node: ReplyNodeUiState): String {
  return if (node.isMine) {
    ReviewRepliesStrings.You
  } else {
    "${ReviewRepliesStrings.ReplyAuthorPrefix}${
            node.reply.authorId.take(AuthorIdMaxLength)
        }"
  }
}
