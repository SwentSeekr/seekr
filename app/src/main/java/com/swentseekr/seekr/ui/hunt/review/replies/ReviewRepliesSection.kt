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

// ----------
// Shared shapes
// ----------

private val replyCardShape = RoundedCornerShape(ReviewRepliesDimensions.ReplyCardCornerRadius)

private val threadLineShape = RoundedCornerShape(ReviewRepliesDimensions.ThreadLineCornerRadius)

private val collapsedComposerShape =
    RoundedCornerShape(ReviewRepliesDimensions.CollapsedComposerCornerRadius)

private val expandedComposerShape =
    RoundedCornerShape(ReviewRepliesDimensions.ExpandedComposerCornerRadius)

/**
 * Aggregates callbacks used by the review replies UI to avoid long parameter lists.
 *
 * @property onToggleRootReplies Invoked when the root replies section should expand/collapse.
 * @property onRootReplyTextChanged Invoked when the root reply text changes.
 * @property onSendRootReply Invoked when the root reply is sent.
 * @property onReplyAction Invoked when the user wants to reply to a specific node.
 * @property onToggleReplyThread Invoked when a threaded reply should expand/collapse.
 * @property onReplyTextChanged Invoked when a reply text changes for a specific node.
 * @property onSendReply Invoked when sending a reply to a specific node.
 * @property onDeleteReply Invoked when deleting a specific reply.
 */
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

/**
 * Represents the state of a Reddit-style reply composer.
 *
 * @property text The current text entered in the composer.
 * @property isExpanded Whether the composer is expanded.
 * @property isSending Whether the composer is currently sending a reply.
 */
data class RedditComposerState(
    val text: String,
    val isExpanded: Boolean,
    val isSending: Boolean,
)

/**
 * Configuration for a Reddit-style composer instance.
 *
 * @property placeholder Placeholder text for the input field.
 * @property compact If true, the composer is rendered in compact mode.
 * @property onTextChanged Callback for text changes.
 * @property onSend Callback invoked when sending the reply.
 * @property onExpandChange Optional callback invoked when the composer expands or collapses.
 */
data class RedditComposerConfig(
    val placeholder: String,
    val compact: Boolean,
    val onTextChanged: (String) -> Unit,
    val onSend: () -> Unit,
    val onExpandChange: ((Boolean) -> Unit)? = null,
)

/**
 * Entry point composable for the review replies section, connected to [ReviewRepliesViewModel].
 *
 * Subscribes to the ViewModel state and delegates UI rendering to [ReviewRepliesSectionContent].
 *
 * @param viewModel The ViewModel providing the replies state.
 * @param modifier Optional Compose [Modifier] for styling.
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
 * Pure UI composable for rendering replies based on [ReviewRepliesUiState] and
 * [ReviewRepliesCallbacks]. Does not directly access the ViewModel.
 *
 * @param state The current state of the replies section.
 * @param callbacks Callbacks to handle user interactions.
 * @param modifier Optional Compose [Modifier] for styling.
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
 * Shows a summary of the number of replies and allows toggling to expand the thread.
 *
 * @param state The current UI state of the replies section.
 * @param onToggleRootReplies Callback invoked when the user taps the header to expand/collapse
 *   replies.
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
 * Expanded content of the replies section, including the root composer, error messages, and the
 * threaded list of child replies.
 *
 * @param state Current state of the replies section.
 * @param callbacks Callbacks to handle user interactions.
 * @param rootComposerExpanded Whether the root composer is expanded.
 * @param onRootComposerExpandedChange Callback to update the root composer expanded state.
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
 * Renders a Reddit-style threaded list of replies.
 *
 * @param items List of nodes representing replies.
 * @param state The UI state of the replies section.
 * @param callbacks Callbacks for user interactions.
 * @param modifier Optional Compose [Modifier] for styling.
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

/**
 * Displays a single reply node, including its card, thread line, and actions.
 *
 * @param node Node containing reply data and metadata.
 * @param replyText Text for the inline composer (if any).
 * @param callbacks Callbacks to handle reply actions.
 * @param modifier Optional Compose [Modifier] for styling.
 */
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

/**
 * Vertical thread line used to visually connect replies in a thread.
 *
 * Draws a narrow vertical line to indicate reply hierarchy.
 */
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
 * Card displaying a single reply.
 *
 * Includes header, body, actions, and optionally an inline composer for replying to this node.
 *
 * @param node The reply node state containing the reply and metadata.
 * @param replyText Optional text for the inline composer below this reply.
 * @param composerExpanded Whether the inline composer is currently expanded.
 * @param onComposerExpandedChange Callback invoked when the composer expanded state changes.
 * @param callbacks Callbacks for handling user actions (reply, delete, toggle).
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
 * Header section of a reply card.
 *
 * Displays avatar, author name, timestamp, and a delete button if the reply belongs to the current
 * user.
 *
 * @param node The reply node state containing reply data and metadata.
 * @param callbacks Callbacks to handle delete action.
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
 * Avatar displayed for a reply.
 *
 * Shows "You" if the reply is from the current user; otherwise shows initials of the author.
 *
 * @param node Reply node containing author information.
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

/**
 * Body text of a reply.
 *
 * Shows deleted message if the reply is deleted.
 *
 * @param reply The [HuntReviewReply] to display.
 */
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

/**
 * Action row displayed below a reply.
 *
 * Includes "Reply" button and optional expand/collapse button for child replies.
 *
 * @param node Reply node containing metadata and child count.
 * @param composerExpanded Whether the inline composer is expanded.
 * @param onComposerExpandedChange Callback when composer expand state changes.
 * @param callbacks Callbacks for reply and thread toggle actions.
 */
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

/**
 * Button to open the inline composer for replying to a specific message.
 *
 * @param onClick Callback when the reply button is clicked.
 */
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

/**
 * Button to toggle visibility of child replies for a reply node.
 *
 * @param node Reply node containing child replies info.
 * @param onToggle Callback invoked when toggle button is clicked.
 */
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

/**
 * Inline composer displayed under a specific reply for direct response.
 *
 * @param reply The reply this inline composer responds to.
 * @param replyText Current text in the inline composer.
 * @param composerExpanded Whether the composer is expanded.
 * @param onComposerExpandedChange Callback when the composer expand state changes.
 * @param callbacks Callbacks to handle text changes and send action.
 */
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

/**
 * Generic Reddit-style composer used for both root and inline reply inputs.
 *
 * Chooses between a collapsed placeholder button and the expanded text input depending on the
 * [state] and [config].
 *
 * @param state Current state of the composer (text, expanded, sending).
 * @param config Configuration for placeholder, compact mode, callbacks, and expand behavior.
 * @param modifier Optional [Modifier] for styling and layout.
 */
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

/**
 * Collapsed representation of the root composer.
 *
 * Displays a placeholder button prompting the user to start a reply. When clicked, it invokes
 * [onExpand] to switch to the expanded composer.
 *
 * @param placeholder Text to display in the collapsed button.
 * @param onExpand Callback invoked when the button is clicked.
 * @param modifier Optional [Modifier] for styling and layout.
 */
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
 * Expanded content of the composer, including the text field and send button or progress indicator.
 *
 * @param state Current state of the composer (text, expanded, sending).
 * @param config Configuration for callbacks, placeholder, and compact mode.
 * @param modifier Optional [Modifier] for styling and layout.
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

/**
 * Text field used in the composer to enter a reply.
 *
 * @param state Current state of the composer (text, sending).
 * @param config Configuration containing placeholder text and text change callback.
 * @param modifier Optional [Modifier] for styling and layout.
 */
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

/**
 * Send button used in the composer.
 *
 * Handles the disabled state if the text is blank and calls [config.onSend] when clicked. Also
 * collapses the composer if [config.onExpandChange] is provided.
 *
 * @param state Current state of the composer (text, sending).
 * @param config Configuration containing callbacks for sending and expand/collapse.
 */
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

// ----------
// Helper functions
// ----------

/**
 * Returns a localized label for the total number of replies at the root level.
 *
 * @param totalCount Total number of replies.
 * @return Localized reply count label.
 */
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

/**
 * Returns a localized label for the number of child replies of a node.
 *
 * @param isExpanded Whether the node is currently expanded.
 * @param totalChildren Total number of child replies.
 * @return Localized label for child replies.
 */
private fun childRepliesLabel(isExpanded: Boolean, totalChildren: Int): String {
  if (isExpanded) return ReviewRepliesStrings.HIDE_REPLIES

  val unit =
      if (totalChildren == ReviewRepliesValues.ROOT_DEPTH + ReviewRepliesValues.SINGLE_REPLY_COUNT)
          ReviewRepliesStrings.REPLY_UNIT_SINGULAR
      else ReviewRepliesStrings.REPLY_UNIT_PLURAL

  return "$totalChildren $unit"
}

/**
 * Returns the display label for the reply author.
 *
 * @param node Node containing reply information.
 * @return "You" if authored by current user, otherwise "u/{authorId}".
 */
private fun authorLabel(node: ReplyNodeUiState): String {
  return if (node.isMine) {
    ReviewRepliesStrings.YOU
  } else {
    "${ReviewRepliesStrings.REPLY_AUTHOR_PREFIX}${
            node.reply.authorId.take(ReviewRepliesValues.AUTHOR_ID_MAX_LENGTH)
        }"
  }
}
