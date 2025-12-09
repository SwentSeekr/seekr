package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

/**
 * Renders the entire replies block below a review card:
 * - A "See replies" / "Hide replies" line when there are replies.
 * - Inline composer to reply directly to the review.
 * - Visible replies list with nested indentation.
 */
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

  Column(modifier = modifier.fillMaxWidth().padding(top = 12.dp)) {
    // Header with reply count (only show when collapsed)
    if (!state.isRootExpanded) {
      ReviewRepliesHeader(
          totalReplyCount = state.totalReplyCount,
          isExpanded = state.isRootExpanded,
          onToggleRootReplies = onToggleRootReplies,
      )
    }

    // Show composer and replies when expanded
    if (state.isRootExpanded) {
      Spacer(modifier = Modifier.height(8.dp))

      // Root reply composer
      ModernReplyComposer(
          text = state.rootReplyText,
          isSending = state.isSendingReply,
          placeholder = "Add a reply...",
          onTextChanged = onRootReplyTextChanged,
          onSend = onSendRootReply,
          isExpanded = rootComposerExpanded,
          onExpandChange = { rootComposerExpanded = it })

      // Error message
      state.errorMessage?.let { msg ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = msg,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 8.dp))
      }

      // Replies list
      if (state.replies.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        RepliesList(
            items = state.replies,
            state = state,
            onReplyAction = onReplyAction,
            onToggleReplyThread = onToggleReplyThread,
            onReplyTextChanged = onReplyTextChanged,
            onSendReply = onSendReply,
            onDeleteReply = onDeleteReply,
        )
      }
    }
  }
}
/**
 * Header row shown just under the review, combining:
 * - "See X replies" / "Hide replies" text if there are replies.
 * - A subtle "No replies yet" label when there are none.
 */
@Composable
private fun ReviewRepliesHeader(
    totalReplyCount: Int,
    isExpanded: Boolean,
    onToggleRootReplies: () -> Unit,
    modifier: Modifier = Modifier,
) {
  if (totalReplyCount > 0) {
    Surface(
        modifier = modifier.clickable { onToggleRootReplies() }.fillMaxWidth(),
        color = Color.Transparent) {
          Row(
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$totalReplyCount ${if (totalReplyCount == 1) "reply" else "replies"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium)
              }
        }
  }
}

/**
 * Shows all currently visible replies as a vertical list. The indentation, nested "See replies" and
 * inline composers are handled at the item level.
 */
@Composable
private fun RepliesList(
    items: List<ReplyNodeUiState>,
    state: ReviewRepliesUiState,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: (String) -> Unit,
    onReplyTextChanged: (ReplyTarget.Reply, String) -> Unit,
    onSendReply: (ReplyTarget.Reply) -> Unit,
    onDeleteReply: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
    items.forEach { node ->
      ReplyItem(
          node = node,
          replyText = state.childReplyTexts[node.reply.replyId],
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
/** Single reply item row, Reddit-style. */
@Composable
private fun ReplyItem(
    node: ReplyNodeUiState,
    replyText: String?,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: () -> Unit,
    onReplyTextChanged: (String) -> Unit,
    onSendReply: () -> Unit,
    onDeleteReply: () -> Unit,
    modifier: Modifier = Modifier,
) {
  val depthIndent = (node.depth.coerceAtLeast(0)) * 16.dp
  val reply = node.reply
  var composerExpanded by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxWidth().padding(start = depthIndent)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
      // Thread line
      if (node.depth > 0) {
        Box(
            modifier =
                Modifier.width(2.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)))
        Spacer(modifier = Modifier.width(12.dp))
      }

      // Reply content card
      Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.onPrimary,
          tonalElevation = 0.dp) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
              // Header: author + timestamp + delete
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circle
                    Box(
                        modifier =
                            Modifier.size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (node.isMine) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center) {
                          Text(
                              text = if (node.isMine) "Y" else reply.authorId.take(1).uppercase(),
                              style = MaterialTheme.typography.labelSmall,
                              fontWeight = FontWeight.Bold,
                              color = Color.White,
                              fontSize = 11.sp)
                        }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (node.isMine) "You" else reply.authorId.take(8),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)

                    Text(
                        text = " • just now",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))

                    Spacer(modifier = Modifier.weight(1f))

                    if (node.isMine && !reply.isDeleted) {
                      IconButton(onClick = onDeleteReply, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp))
                      }
                    }
                  }

              Spacer(modifier = Modifier.height(8.dp))

              // Comment text
              Text(
                  text = if (reply.isDeleted) "[deleted]" else reply.comment,
                  style = MaterialTheme.typography.bodyMedium,
                  color =
                      if (reply.isDeleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      },
                  lineHeight = 20.sp)

              Spacer(modifier = Modifier.height(8.dp))

              // Action buttons
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Reply button
                    TextButton(
                        onClick = {
                          composerExpanded = !composerExpanded
                          onReplyAction(
                              ReplyTarget.Reply(
                                  reviewId = reply.reviewId, parentReplyId = reply.replyId))
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)) {
                          Icon(
                              imageVector = Icons.Filled.Send,
                              contentDescription = "Reply",
                              modifier = Modifier.size(16.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(text = "Reply", style = MaterialTheme.typography.labelMedium)
                        }

                    // Toggle thread button
                    if (node.totalChildrenCount > 0) {
                      TextButton(
                          onClick = onToggleReplyThread,
                          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                          modifier = Modifier.height(32.dp)) {
                            Icon(
                                imageVector =
                                    if (node.isExpanded) Icons.Filled.KeyboardArrowUp
                                    else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text =
                                    if (node.isExpanded) "Hide"
                                    else
                                        "${node.totalChildrenCount} ${if (node.totalChildrenCount == 1) "reply" else "replies"}",
                                style = MaterialTheme.typography.labelMedium)
                          }
                    }
                  }

              // Inline composer
              if (node.isComposerOpen && !reply.isDeleted) {
                Spacer(modifier = Modifier.height(8.dp))
                ModernReplyComposer(
                    text = replyText.orEmpty(),
                    isSending = false,
                    placeholder = "Write a reply...",
                    onTextChanged = onReplyTextChanged,
                    onSend = onSendReply,
                    isExpanded = composerExpanded,
                    onExpandChange = { composerExpanded = it },
                    compact = true)
              }
            }
          }
    }
  }
}

/** Modern, compact reply composer with send button. */
@Composable
private fun ModernReplyComposer(
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
  if (!isExpanded && onExpandChange != null) {
    // Collapsed state - just show Reply button
    TextButton(
        onClick = { onExpandChange(true) },
        modifier = modifier.fillMaxWidth().padding(horizontal = if (compact) 0.dp else 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors =
            ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Start,
              modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Reply",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reply",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
        }
  } else {
    // Expanded state - show text field and send button
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = if (compact) 0.dp else 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
              value = text,
              onValueChange = onTextChanged,
              modifier = Modifier.weight(1f).heightIn(min = 44.dp),
              placeholder = {
                Text(text = placeholder, style = MaterialTheme.typography.bodyMedium)
              },
              singleLine = true,
              shape = RoundedCornerShape(24.dp),
              textStyle = MaterialTheme.typography.bodyMedium,
              colors =
                  OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = MaterialTheme.colorScheme.primary,
                      unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                      focusedContainerColor = MaterialTheme.colorScheme.surface,
                      unfocusedContainerColor = MaterialTheme.colorScheme.surface))

          if (isSending) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 2.dp)
          } else {
            IconButton(
                onClick = {
                  if (text.isNotBlank()) {
                    onSend()
                    onExpandChange?.invoke(false)
                  }
                },
                enabled = text.isNotBlank(),
                modifier = Modifier.size(40.dp)) {
                  Surface(
                      shape = CircleShape,
                      color =
                          if (text.isNotBlank()) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.surfaceVariant,
                      modifier = Modifier.size(36.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()) {
                              Icon(
                                  imageVector = Icons.Filled.Send,
                                  contentDescription = "Send",
                                  tint =
                                      if (text.isNotBlank()) MaterialTheme.colorScheme.onPrimary
                                      else MaterialTheme.colorScheme.onSurfaceVariant,
                                  modifier = Modifier.size(18.dp))
                            }
                      }
                }
          }
        }
  }
}
