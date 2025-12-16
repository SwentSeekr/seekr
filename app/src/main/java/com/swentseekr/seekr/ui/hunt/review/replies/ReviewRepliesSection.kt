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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.times
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swentseekr.seekr.model.hunt.review.HuntReviewReply
import com.swentseekr.seekr.ui.theme.Transparent

// ---------- Shared shapes ----------

private val replyCardShape = RoundedCornerShape(ReviewRepliesDimensions.ReplyCardCornerRadius)

private val threadLineShape = RoundedCornerShape(ReviewRepliesDimensions.ThreadLineCornerRadius)

private val collapsedComposerShape =
    RoundedCornerShape(ReviewRepliesDimensions.CollapsedComposerCornerRadius)

private val expandedComposerShape =
    RoundedCornerShape(ReviewRepliesDimensions.ExpandedComposerCornerRadius)

/** Aggregates callbacks used by the review replies UI to avoid long parameter lists. */
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

/** Represents the state of a Reddit-style reply composer. */
data class RedditComposerState(
    val text: String,
    val isExpanded: Boolean,
    val isSending: Boolean,
)

/** Configuration for a Reddit-style composer instance. */
data class RedditComposerConfig(
    val placeholder: String,
    val compact: Boolean,
    val onTextChanged: (String) -> Unit,
    val onSend: () -> Unit,
    val onExpandChange: ((Boolean) -> Unit)? = null,
)

/**
 * Entry point composable for the review replies section, wired to the [ReviewRepliesViewModel].
 *
 * This composable subscribes to the ViewModel state and builds the UI through
 * [ReviewRepliesSectionContent].
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

/**
 * Pure UI composable that renders the replies section based on [ReviewRepliesUiState] and
 * [ReviewRepliesCallbacks], without direct knowledge of the ViewModel.
 */
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

/**
 * Header displayed when the root replies are collapsed.
 *
 * Provides a summary of the number of replies and a toggle to expand the thread.
 */
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
      color = Transparent) {
        Row(
            modifier =
                Modifier.padding(
                    vertical = ReviewRepliesDimensions.RootHeaderVerticalPadding,
                    horizontal = ReviewRepliesDimensions.RootHeaderHorizontalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = null,
                  tint =
                      MaterialTheme.colorScheme.primary.copy(
                          alpha = ReviewRepliesAlphas.ROOT_HEADER_ICON),
                  modifier = Modifier.size(ReviewRepliesDimensions.RootHeaderIconSize))
              Spacer(modifier = Modifier.width(ReviewRepliesDimensions.RootHeaderIconSpacing))
              Text(
                  text = replyCountLabel(state.totalReplyCount),
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.primary)
            }
      }
}

/**
 * Expanded content of the replies section, including the root composer, errors and the threaded
 * list of replies.
 */
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
            placeholder = ReviewRepliesStrings.ROOT_COMPOSER_PLACEHOLDER,
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

/**
 * Reddit-style threaded list of replies.
 *
 * Each [ReplyNodeUiState] is rendered as a separate threaded reply item.
 */
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

/** Renders a single reply node, including thread line, card and actions. */
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
        if (node.depth > ReviewRepliesValues.ROOT_DEPTH) {
          ReplyThreadLine()
        }

        Column(
            modifier =
                Modifier.weight(ReviewRepliesValues.FULL_WEIGHT)
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

/** Vertical thread line used to visually connect replies in a thread. */
@Composable
fun ReplyThreadLine() {
  Box(
      modifier =
          Modifier.width(ReviewRepliesDimensions.ThreadLineWidth)
              .height(IntrinsicSize.Min)
              .background(
                  MaterialTheme.colorScheme.outlineVariant.copy(
                      alpha = ReviewRepliesAlphas.OUTLINE_VARIANT),
                  shape = threadLineShape,
              ),
  )
  Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ThreadLineHorizontalSpacing))
}

/**
 * Card that displays a single reply, including header, body, actions and optional inline composer.
 */
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
                  MaterialTheme.colorScheme.surfaceVariant,
                  shape = replyCardShape,
              )
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

/**
 * Header section of a reply card, including avatar, author, timestamp and optional delete action.
 */
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
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface)

    Text(
        text = ReviewRepliesStrings.JUST_NOW,
        style = MaterialTheme.typography.bodySmall,
        color =
            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = ReviewRepliesAlphas.REPLY_TIMESTAMP))

    Spacer(modifier = Modifier.weight(ReviewRepliesValues.FULL_WEIGHT))

    if (node.isMine && !reply.isDeleted) {
      IconButton(
          onClick = { callbacks.onDeleteReply(reply.replyId) },
          modifier =
              Modifier.size(ReviewRepliesDimensions.DeleteButtonSize)
                  .testTag("${ReviewRepliesTestTags.REPLY_DELETE_BUTTON_PREFIX}${reply.replyId}")) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = ReviewRepliesStrings.DELETE_CONTENT_DESCRIPTION,
                tint =
                    MaterialTheme.colorScheme.error.copy(
                        alpha = ReviewRepliesAlphas.DELETE_ICON_ALPHA),
                modifier = Modifier.size(ReviewRepliesDimensions.DeleteIconSize))
          }
    }
  }
}

/**
 * Avatar displayed for a reply. Uses "You" for the current user, otherwise initials from the author
 * id.
 */
@Composable
fun ReplyAvatar(node: ReplyNodeUiState) {
  val reply = node.reply
  val avatarText =
      if (node.isMine) ReviewRepliesStrings.YOU
      else reply.authorId.take(ReviewRepliesValues.SINGLE_REPLY_COUNT).uppercase()

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
            color =
                if (node.isMine) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

/** Body text of a reply, including handling for deleted replies. */
@Composable
fun ReplyBody(reply: HuntReviewReply) {
  Text(
      text = if (reply.isDeleted) ReviewRepliesStrings.DELETE else reply.comment,
      style = MaterialTheme.typography.bodyMedium,
      color =
          if (reply.isDeleted)
              MaterialTheme.colorScheme.onSurfaceVariant.copy(
                  alpha = ReviewRepliesAlphas.DELETED_REPLY)
          else MaterialTheme.colorScheme.onSurface,
      lineHeight = ReviewRepliesDimensions.ReplyTextLineHeight)
}

/** Action row displayed below a reply body, including reply and expand/collapse controls. */
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

        if (node.totalChildrenCount > ReviewRepliesValues.ROOT_DEPTH) {
          RepliesToggleButton(
              node = node,
              onToggle = { callbacks.onToggleReplyThread(reply.replyId) },
          )
        }
      }
}

/** Button used to open the inline composer for replying to a specific message. */
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
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = ReviewRepliesStrings.REPLY_CONTENT_DESCRIPTION,
            modifier = Modifier.size(ReviewRepliesDimensions.ReplyButtonIconSize))
        Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ReplyButtonIconSpacing))
        Text(text = ReviewRepliesStrings.REPLY, style = MaterialTheme.typography.labelLarge)
      }
}

/** Button that toggles the visibility of child replies for a given reply node. */
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
        contentDescription =
            if (node.isExpanded) ReviewRepliesStrings.HIDE_REPLIES
            else ReviewRepliesStrings.SHOW_REPLIES,
        modifier = Modifier.size(ReviewRepliesDimensions.ReplyButtonIconSize))
    Spacer(modifier = Modifier.width(ReviewRepliesDimensions.ReplyButtonIconSpacing))
    Text(
        text = childRepliesLabel(node.isExpanded, node.totalChildrenCount),
        style = MaterialTheme.typography.labelLarge)
  }
}

/** Inline composer displayed under a specific reply to allow direct responses in the thread. */
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
          placeholder = ReviewRepliesStrings.INLINE_REPLY_PLACEHOLDER,
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

/** Generic Reddit-style composer used for both root and inline reply inputs. */
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

/** Collapsed representation of the root composer, prompting the user to start a reply. */
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
              containerColor = MaterialTheme.colorScheme.surfaceVariant,
              contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
      border =
          BorderStroke(
              width = ReviewRepliesDimensions.OutlineBorderWidth,
              color = MaterialTheme.colorScheme.outlineVariant,
          ),
      shape = collapsedComposerShape) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically) {
              Text(
                  text = placeholder,
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                      MaterialTheme.colorScheme.onSurfaceVariant.copy(
                          alpha = ReviewRepliesAlphas.ROOT_HEADER_PLACEHOLDER))
            }
      }
}

/**
 * Expanded content of the composer, including the text field and send button/progress indicator.
 */
@Composable
fun ExpandedComposerContent(
    state: RedditComposerState,
    config: RedditComposerConfig,
    modifier: Modifier,
) {
  val outlineColor =
      MaterialTheme.colorScheme.outlineVariant.copy(
          alpha = ReviewRepliesAlphas.OUTLINE_VARIANT_BORDER)

  Surface(
      modifier =
          modifier
              .fillMaxWidth()
              .padding(
                  horizontal =
                      if (config.compact)
                          ReviewRepliesDimensions.ExpandedComposerCompactHorizontalPadding
                      else ReviewRepliesDimensions.ExpandedComposerHorizontalPadding),
      shape = expandedComposerShape,
      color =
          MaterialTheme.colorScheme.surfaceVariant.copy(
              alpha = ReviewRepliesAlphas.COMPOSER_SURFACE_ALPHA),
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
              ComposerTextField(
                  state = state,
                  config = config,
                  modifier =
                      Modifier.weight(ReviewRepliesValues.FULL_WEIGHT)
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

/** Text field used in the composer to enter a reply. */
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
      enabled = !state.isSending,
      placeholder = {
        Text(
            text = config.placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color =
                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = ReviewRepliesAlphas.COMPOSER_PLACEHOLDER))
      },
      singleLine = true,
      textStyle = MaterialTheme.typography.bodyMedium,
      colors =
          OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Transparent,
              unfocusedBorderColor = Transparent,
              focusedContainerColor = Transparent,
              unfocusedContainerColor = Transparent),
  )
}

/** Send button used in the composer, including disabled state handling. */
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
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = ReviewRepliesStrings.SEND_CONTENT_DESCRIPTION,
                    tint =
                        if (isActive) MaterialTheme.colorScheme.onPrimary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = ReviewRepliesAlphas.INACTIVE_SEND_ICON),
                    modifier = Modifier.size(ReviewRepliesDimensions.SendIconSize))
              }
            }
      }
}

/* ---------- Pure helper functions (no Compose) ---------- */

/** Returns a localized label for the total number of replies at the root level. */
private fun replyCountLabel(totalCount: Int): String {
  return if (totalCount > ReviewRepliesValues.ROOT_DEPTH) {
    val unit =
        if (totalCount == ReviewRepliesValues.ROOT_DEPTH + ReviewRepliesValues.SINGLE_REPLY_COUNT)
            ReviewRepliesStrings.REPLY_UNIT_SINGULAR
        else ReviewRepliesStrings.REPLY_UNIT_PLURAL
    "$totalCount $unit"
  } else {
    ReviewRepliesStrings.BE_THE_FIRST_TO_REPLY
  }
}

/** Returns a localized label for the number of child replies of a node. */
private fun childRepliesLabel(isExpanded: Boolean, totalChildren: Int): String {
  if (isExpanded) return ReviewRepliesStrings.HIDE_REPLIES

  val unit =
      if (totalChildren == ReviewRepliesValues.ROOT_DEPTH + ReviewRepliesValues.SINGLE_REPLY_COUNT)
          ReviewRepliesStrings.REPLY_UNIT_SINGULAR
      else ReviewRepliesStrings.REPLY_UNIT_PLURAL

  return "$totalChildren $unit"
}

/** Returns the display label for the reply author, including handling for the current user. */
private fun authorLabel(node: ReplyNodeUiState): String {
  return if (node.isMine) {
    ReviewRepliesStrings.YOU
  } else {
    "${ReviewRepliesStrings.REPLY_AUTHOR_PREFIX}${
            node.reply.authorId.take(ReviewRepliesValues.AUTHOR_ID_MAX_LENGTH)
        }"
  }
}
