package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.times
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesValues.AuthorIdMaxLength
import com.swentseekr.seekr.ui.hunt.review.replies.ReviewRepliesValues.SingleReplyCount

/** Reddit-style replies section with improved visual hierarchy and polish */
@Composable
fun ReviewRepliesSection(
    state: ReviewRepliesUiState,
    onToggleRootReplies: () -> Unit,
    onRootReplyTextChanged: (String) -> Unit,
    onSendRootReply: () -> Unit,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: (String) -> Unit,
    onReplyTextChanged: (ReplyTarget.Reply, String) -> Unit,
    onSendReply: (ReplyTarget.Reply) -> Unit,
    onDeleteReply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  var rootComposerExpanded by remember { mutableStateOf(false) }

  Column(
      modifier = modifier.fillMaxWidth().testTag(ReviewRepliesTestTags.REPLIES_SECTION),
  ) {

    // Elegant header with reply count
    if (!state.isRootExpanded) {
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
                      imageVector = Icons.Default.Send,
                      contentDescription = null,
                      tint =
                          MaterialTheme.colorScheme.primary.copy(
                              alpha = ReviewRepliesAlphas.RootHeaderIcon),
                      modifier = Modifier.size(ReviewRepliesDimensions.RootHeaderIconSize))
                  Spacer(modifier = Modifier.width(ReviewRepliesDimensions.RootHeaderIconSpacing))
                  Text(
                      text =
                          when {
                            state.totalReplyCount > ReviewRepliesValues.RootDepth ->
                                "${state.totalReplyCount} " +
                                    if (state.totalReplyCount ==
                                        ReviewRepliesValues.RootDepth + SingleReplyCount)
                                        ReviewRepliesStrings.ReplyUnitSingular
                                    else ReviewRepliesStrings.ReplyUnitPlural
                            else -> ReviewRepliesStrings.BeTheFirstToReply
                          },
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Medium,
                      color = MaterialTheme.colorScheme.primary)
                }
          }
    }

    // Expanded state with composer and replies
    if (state.isRootExpanded) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.height(ReviewRepliesDimensions.RootComposerTopSpacing))

        // Root composer
        RedditStyleComposer(
            text = state.rootReplyText,
            isSending = state.isSendingReply,
            placeholder = ReviewRepliesStrings.RootComposerPlaceholder,
            onTextChanged = onRootReplyTextChanged,
            onSend = onSendRootReply,
            isExpanded = rootComposerExpanded,
            onExpandChange = { rootComposerExpanded = it },
            modifier = Modifier.testTag(ReviewRepliesTestTags.ROOT_INLINE_COMPOSER),
        )

        // Error message
        state.errorMessage?.let { msg ->
          Spacer(modifier = Modifier.height(ReviewRepliesDimensions.RootErrorTopSpacing))
          Text(
              text = msg,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
              modifier =
                  Modifier.padding(horizontal = ReviewRepliesDimensions.ErrorHorizontalPadding))
        }

        // Replies thread
        if (state.replies.isNotEmpty()) {
          Spacer(modifier = Modifier.height(ReviewRepliesDimensions.ThreadTopSpacing))
          RedditThreadList(
              items = state.replies,
              state = state,
              onReplyAction = onReplyAction,
              onToggleReplyThread = onToggleReplyThread,
              onReplyTextChanged = onReplyTextChanged,
              onSendReply = onSendReply,
              onDeleteReply = onDeleteReply,
              modifier = Modifier.testTag(ReviewRepliesTestTags.THREAD_LIST),
          )
        }
      }
    }
  }
}

/** Reddit-style thread list with clean vertical lines */
@Composable
private fun RedditThreadList(
    items: List<ReplyNodeUiState>,
    state: ReviewRepliesUiState,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: (String) -> Unit,
    onReplyTextChanged: (ReplyTarget.Reply, String) -> Unit,
    onSendReply: (ReplyTarget.Reply) -> Unit,
    onDeleteReply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(
      modifier = modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(ReviewRepliesDimensions.ThreadLineCornerRadius)) {
        items.forEachIndexed { index, node ->
          RedditReplyItem(
              node = node,
              replyText = state.childReplyTexts[node.reply.replyId],
              isLastInThread = index == items.lastIndex,
              onReplyAction = onReplyAction,
              onToggleReplyThread = { onToggleReplyThread(node.reply.replyId) },
              onReplyTextChanged = { newText ->
                onReplyTextChanged(
                    ReplyTarget.Reply(
                        reviewId = node.reply.reviewId, parentReplyId = node.reply.replyId),
                    newText)
              },
              onSendReply = {
                onSendReply(
                    ReplyTarget.Reply(
                        reviewId = node.reply.reviewId, parentReplyId = node.reply.replyId))
              },
              onDeleteReply = { onDeleteReply(node.reply.replyId) })
        }
      }
}

/** Individual Reddit-style reply with elegant thread lines */
@Composable
private fun RedditReplyItem(
    node: ReplyNodeUiState,
    replyText: String?,
    isLastInThread: Boolean,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: () -> Unit,
    onReplyTextChanged: (String) -> Unit,
    onSendReply: () -> Unit,
    onDeleteReply: () -> Unit,
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
        // Thread line indicator
        if (node.depth > ReviewRepliesValues.RootDepth) {
          Box(
              modifier =
                  Modifier.width(ReviewRepliesDimensions.ThreadLineWidth)
                      .height(IntrinsicSize.Min)
                      .background(
                          MaterialTheme.colorScheme.outlineVariant.copy(
                              alpha = ReviewRepliesAlphas.OutlineVariant),
                          shape =
                              RoundedCornerShape(ReviewRepliesDimensions.ThreadLineCornerRadius)))
          Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ThreadLineHorizontalSpacing))
        }

        Column(
            modifier =
                Modifier.weight(ReviewRepliesValues.FullWeight)
                    .padding(bottom = ReviewRepliesDimensions.ReplyVerticalSpacing)) {
              // Reply content
              Column(
                  modifier =
                      Modifier.fillMaxWidth()
                          .background(
                              MaterialTheme.colorScheme.surface,
                              shape =
                                  RoundedCornerShape(ReviewRepliesDimensions.ReplyCardCornerRadius))
                          .padding(ReviewRepliesDimensions.ReplyCardPadding)) {
                    // Header: avatar + author + timestamp + actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                          // Avatar
                          Box(
                              modifier =
                                  Modifier.size(ReviewRepliesDimensions.ReplyAvatarSize)
                                      .clip(CircleShape)
                                      .background(
                                          if (node.isMine) MaterialTheme.colorScheme.primary
                                          else MaterialTheme.colorScheme.surfaceVariant),
                              contentAlignment = Alignment.Center) {
                                Text(
                                    text =
                                        if (node.isMine) ReviewRepliesStrings.You
                                        else reply.authorId.take(SingleReplyCount).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color =
                                        if (node.isMine) Color.White
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = ReviewRepliesDimensions.ReplyAvatarFontSize)
                              }

                          Spacer(
                              modifier =
                                  Modifier.width(ReviewRepliesDimensions.ReplyAvatarNameSpacing))

                          // Author name
                          Text(
                              text =
                                  if (node.isMine) ReviewRepliesStrings.You
                                  else
                                      "${ReviewRepliesStrings.ReplyAuthorPrefix}" +
                                          reply.authorId.take(AuthorIdMaxLength),
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

                          // Delete button for own replies
                          if (node.isMine && !reply.isDeleted) {
                            IconButton(
                                onClick = onDeleteReply,
                                modifier =
                                    Modifier.size(ReviewRepliesDimensions.DeleteButtonSize)
                                        .testTag(
                                            "${ReviewRepliesTestTags.REPLY_DELETE_BUTTON_PREFIX}${reply.replyId}")) {
                                  Icon(
                                      imageVector = Icons.Default.Delete,
                                      contentDescription =
                                          ReviewRepliesStrings.DeleteContentDescription,
                                      tint =
                                          MaterialTheme.colorScheme.error.copy(
                                              alpha = ReviewRepliesAlphas.DeleteIconAlpha),
                                      modifier =
                                          Modifier.size(ReviewRepliesDimensions.DeleteIconSize))
                                }
                          }
                        }

                    Spacer(
                        modifier =
                            Modifier.height(ReviewRepliesDimensions.ReplyHeaderBottomSpacing))

                    // Comment text
                    Text(
                        text = if (reply.isDeleted) ReviewRepliesStrings.Deleted else reply.comment,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (reply.isDeleted)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = ReviewRepliesAlphas.DeletedReply)
                            else MaterialTheme.colorScheme.onSurface,
                        lineHeight = ReviewRepliesDimensions.ReplyTextLineHeight)

                    Spacer(
                        modifier = Modifier.height(ReviewRepliesDimensions.ReplyTextBottomSpacing))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically) {
                          // Reply button
                          TextButton(
                              onClick = {
                                composerExpanded = !composerExpanded
                                onReplyAction(
                                    ReplyTarget.Reply(
                                        reviewId = reply.reviewId, parentReplyId = reply.replyId))
                              },
                              contentPadding =
                                  PaddingValues(
                                      horizontal =
                                          ReviewRepliesDimensions.ReplyTextButtonHorizontalPadding,
                                      vertical =
                                          ReviewRepliesDimensions.ReplyTextButtonVerticalPadding),
                              colors =
                                  ButtonDefaults.textButtonColors(
                                      contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription =
                                        ReviewRepliesStrings.ReplyContentDescription,
                                    modifier =
                                        Modifier.size(ReviewRepliesDimensions.ReplyButtonIconSize))
                                Spacer(
                                    modifier =
                                        Modifier.width(
                                            ReviewRepliesDimensions.ReplyButtonIconSpacing))
                                Text(
                                    text = ReviewRepliesStrings.Reply,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium)
                              }

                          // Show/hide replies button
                          if (node.totalChildrenCount > ReviewRepliesValues.RootDepth) {
                            TextButton(
                                onClick = onToggleReplyThread,
                                contentPadding =
                                    PaddingValues(
                                        horizontal =
                                            ReviewRepliesDimensions
                                                .ReplyTextButtonHorizontalPadding,
                                        vertical =
                                            ReviewRepliesDimensions.ReplyTextButtonVerticalPadding),
                                colors =
                                    ButtonDefaults.textButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary),
                                modifier =
                                    Modifier.testTag(
                                        "${ReviewRepliesTestTags.REPLY_SEE_REPLIES_PREFIX}${reply.replyId}"),
                            ) {
                              Icon(
                                  imageVector =
                                      if (node.isExpanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                                  contentDescription = null,
                                  modifier =
                                      Modifier.size(ReviewRepliesDimensions.ReplyButtonIconSize))
                              Spacer(
                                  modifier =
                                      Modifier.width(
                                          ReviewRepliesDimensions.ReplyButtonIconSpacing))
                              Text(
                                  text =
                                      if (node.isExpanded) ReviewRepliesStrings.HideReplies
                                      else
                                          "${node.totalChildrenCount} " +
                                              if (node.totalChildrenCount ==
                                                  ReviewRepliesValues.RootDepth + SingleReplyCount)
                                                  ReviewRepliesStrings.ReplyUnitSingular
                                              else ReviewRepliesStrings.ReplyUnitPlural,
                                  style = MaterialTheme.typography.labelLarge,
                                  fontWeight = FontWeight.Medium)
                            }
                          }
                        }

                    // Inline composer
                    if (node.isComposerOpen && !reply.isDeleted) {
                      Spacer(
                          modifier =
                              Modifier.height(ReviewRepliesDimensions.ReplyTextBottomSpacing))
                      RedditStyleComposer(
                          text = replyText.orEmpty(),
                          isSending = false,
                          placeholder = ReviewRepliesStrings.InlineReplyPlaceholder,
                          onTextChanged = onReplyTextChanged,
                          onSend = onSendReply,
                          isExpanded = composerExpanded,
                          onExpandChange = { composerExpanded = it },
                          compact = true,
                          modifier =
                              Modifier.testTag(
                                  "${ReviewRepliesTestTags.REPLY_INLINE_COMPOSER_PREFIX}${reply.replyId}"),
                      )
                    }
                  }
            }
      }
}

/** Reddit-style composer with clean design */
@Composable
private fun RedditStyleComposer(
    text: String,
    isSending: Boolean,
    placeholder: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    isExpanded: Boolean = false,
    onExpandChange: ((Boolean) -> Unit)? = null,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
) {
  // Collapsed state - show button
  if (!compact && !isExpanded && onExpandChange != null) {
    OutlinedButton(
        onClick = { onExpandChange(true) },
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
            ButtonDefaults.outlinedButtonBorder.copy(
                width = ReviewRepliesDimensions.OutlineBorderWidth,
                brush =
                    androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outlineVariant)),
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
  } else {
    // Expanded state - show input field
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        if (compact)
                            ReviewRepliesDimensions.ExpandedComposerCompactHorizontalPadding
                        else ReviewRepliesDimensions.ExpandedComposerHorizontalPadding),
        shape = RoundedCornerShape(ReviewRepliesDimensions.ExpandedComposerCornerRadius),
        color =
            MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = ReviewRepliesAlphas.ComposerSurfaceAlpha),
        border =
            ButtonDefaults.outlinedButtonBorder.copy(
                width = ReviewRepliesDimensions.OutlineBorderWidth,
                brush =
                    androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = ReviewRepliesAlphas.OutlineVariantBorder)))) {
          Row(
              modifier =
                  Modifier.padding(
                      horizontal = ReviewRepliesDimensions.ExpandedComposerContentHorizontalPadding,
                      vertical = ReviewRepliesDimensions.ExpandedComposerContentVerticalPadding),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement =
                  Arrangement.spacedBy(ReviewRepliesDimensions.InlineComposerHorizontalSpacing)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier =
                        Modifier.weight(ReviewRepliesValues.FullWeight)
                            .testTag(ReviewRepliesTestTags.ROOT_INLINE_TEXT_FIELD),
                    placeholder = {
                      Text(
                          text = placeholder,
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
                            unfocusedContainerColor = Color.Transparent))

                if (isSending) {
                  CircularProgressIndicator(
                      modifier = Modifier.size(ReviewRepliesDimensions.SendProgressSize),
                      strokeWidth = ReviewRepliesDimensions.SendProgressStrokeWidth,
                      color = MaterialTheme.colorScheme.primary)
                } else {
                  val isActive = text.isNotBlank()
                  IconButton(
                      onClick = {
                        if (isActive) {
                          onSend()
                          onExpandChange?.invoke(false)
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
                              Box(
                                  contentAlignment = Alignment.Center,
                                  modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription =
                                            ReviewRepliesStrings.SendContentDescription,
                                        tint =
                                            if (isActive) MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = ReviewRepliesAlphas.InactiveSendIcon),
                                        modifier =
                                            Modifier.size(ReviewRepliesDimensions.SendIconSize))
                                  }
                            }
                      }
                }
              }
        }
  }
}
