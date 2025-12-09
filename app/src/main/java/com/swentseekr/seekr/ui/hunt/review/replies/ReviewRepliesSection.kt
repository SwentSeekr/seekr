package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

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

  Column(modifier = modifier.fillMaxWidth()) {

    // Elegant header with reply count
    if (!state.isRootExpanded) {
      Surface(
          modifier = Modifier.fillMaxWidth().clickable { onToggleRootReplies() },
          color = Color.Transparent) {
            Row(
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.Default.Send,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                      modifier = Modifier.size(20.dp))
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                      text =
                          when {
                            state.totalReplyCount > 0 ->
                                "${state.totalReplyCount} ${if (state.totalReplyCount == 1) "reply" else "replies"}"
                            else -> "Be the first to reply"
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
        Spacer(modifier = Modifier.height(8.dp))

        // Root composer
        RedditStyleComposer(
            text = state.rootReplyText,
            isSending = state.isSendingReply,
            placeholder = "What are your thoughts?",
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
              modifier = Modifier.padding(horizontal = 12.dp))
        }

        // Replies thread
        if (state.replies.isNotEmpty()) {
          Spacer(modifier = Modifier.height(16.dp))
          RedditThreadList(
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
  Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
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
  val depthIndent = node.depth * 20.dp

  Row(
      modifier = modifier.fillMaxWidth().padding(start = depthIndent),
      horizontalArrangement = Arrangement.Start) {
        // Thread line indicator
        if (node.depth > 0) {
          Box(
              modifier =
                  Modifier.width(2.dp)
                      .height(IntrinsicSize.Min)
                      .background(
                          MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                          shape = RoundedCornerShape(1.dp)))
          Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f).padding(bottom = 12.dp)) {
          // Reply content
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .background(
                          MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
                      .padding(12.dp)) {
                // Header: avatar + author + timestamp + actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically) {
                      // Avatar
                      Box(
                          modifier =
                              Modifier.size(28.dp)
                                  .clip(CircleShape)
                                  .background(
                                      if (node.isMine) MaterialTheme.colorScheme.primary
                                      else MaterialTheme.colorScheme.surfaceVariant),
                          contentAlignment = Alignment.Center) {
                            Text(
                                text = if (node.isMine) "Y" else reply.authorId.take(1).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color =
                                    if (node.isMine) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp)
                          }

                      Spacer(modifier = Modifier.width(10.dp))

                      // Author name
                      Text(
                          text = if (node.isMine) "You" else "u/${reply.authorId.take(10)}",
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.SemiBold,
                          color = MaterialTheme.colorScheme.onSurface)

                      Text(
                          text = " · just now",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))

                      Spacer(modifier = Modifier.weight(1f))

                      // Delete button for own replies
                      if (node.isMine && !reply.isDeleted) {
                        IconButton(onClick = onDeleteReply, modifier = Modifier.size(32.dp)) {
                          Icon(
                              imageVector = Icons.Default.Delete,
                              contentDescription = "Delete",
                              tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
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
                        if (reply.isDeleted)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp)

                Spacer(modifier = Modifier.height(8.dp))

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
                          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                          colors =
                              ButtonDefaults.textButtonColors(
                                  contentColor = MaterialTheme.colorScheme.onSurfaceVariant)) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Reply",
                                modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Reply",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium)
                          }

                      // Show/hide replies button
                      if (node.totalChildrenCount > 0) {
                        TextButton(
                            onClick = onToggleReplyThread,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            colors =
                                ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary)) {
                              Icon(
                                  imageVector =
                                      if (node.isExpanded) Icons.Default.KeyboardArrowUp
                                      else Icons.Default.KeyboardArrowDown,
                                  contentDescription = null,
                                  modifier = Modifier.size(16.dp))
                              Spacer(modifier = Modifier.width(6.dp))
                              Text(
                                  text =
                                      if (node.isExpanded) "Hide"
                                      else
                                          "${node.totalChildrenCount} ${if (node.totalChildrenCount == 1) "reply" else "replies"}",
                                  style = MaterialTheme.typography.labelLarge,
                                  fontWeight = FontWeight.Medium)
                            }
                      }
                    }

                // Inline composer
                if (node.isComposerOpen && !reply.isDeleted) {
                  Spacer(modifier = Modifier.height(8.dp))
                  RedditStyleComposer(
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
        modifier = modifier.fillMaxWidth().padding(horizontal = 4.dp).height(44.dp),
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        border =
            ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush =
                    androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outlineVariant)),
        shape = RoundedCornerShape(24.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.Start,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
              }
        }
  } else {
    // Expanded state - show input field
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = if (compact) 0.dp else 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border =
            ButtonDefaults.outlinedButtonBorder.copy(
                width = 1.dp,
                brush =
                    androidx.compose.ui.graphics.SolidColor(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))) {
          Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                      Text(
                          text = placeholder,
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
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
                      modifier = Modifier.size(32.dp),
                      strokeWidth = 2.dp,
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
                      modifier = Modifier.size(40.dp)) {
                        Surface(
                            shape = CircleShape,
                            color =
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(36.dp)) {
                              Box(
                                  contentAlignment = Alignment.Center,
                                  modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint =
                                            if (isActive) MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.5f),
                                        modifier = Modifier.size(18.dp))
                                  }
                            }
                      }
                }
              }
        }
  }
}
