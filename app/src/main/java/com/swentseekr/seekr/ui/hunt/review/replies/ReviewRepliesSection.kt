package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

/**
 * Renders the entire replies block below a review card:
 *
 *  - A "See replies" / "Hide replies" line when there are replies.
 *  - Inline composer to reply directly to the review.
 *  - Visible replies list with nested indentation.
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
    val isRootExpanded = state.isRootExpanded
    val totalReplyCount = state.totalReplyCount

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        ReviewRepliesHeader(
            totalReplyCount = totalReplyCount,
            isExpanded = isRootExpanded,
            onToggleRootReplies = onToggleRootReplies,
        )

        Spacer(modifier = Modifier.height(4.dp))

        InlineReplyComposer(
            text = state.rootReplyText,
            isSending = state.isSendingReply,
            placeholder = "Reply to this review...",
            onTextChanged = onRootReplyTextChanged,
            onSend = onSendRootReply,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        state.errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isRootExpanded && state.replies.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
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

/**
 * Header row shown just under the review, combining:
 *  - "See X replies" / "Hide replies" text if there are replies.
 *  - A subtle "No replies yet" label when there are none.
 */
@Composable
fun ReviewRepliesHeader(
    totalReplyCount: Int,
    isExpanded: Boolean,
    onToggleRootReplies: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (totalReplyCount > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .weight(1f)
                    .statusBarsPadding()
                    .clickable { onToggleRootReplies() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector =
                        if (isExpanded) Icons.Filled.KeyboardArrowUp
                        else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isExpanded) {
                        "Hide replies"
                    } else {
                        "See replies ($totalReplyCount)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = "No replies yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Shows all currently visible replies as a vertical list.
 * The indentation, nested "See replies" and inline composers are handled at the item level.
 */
@Composable
fun RepliesList(
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
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items.forEach { node ->
            val replyId = node.reply.replyId
            val replyText = state.childReplyTexts[replyId]

            ReplyItem(
                node = node,
                replyText = replyText,
                onReplyAction = { target -> onReplyAction(target) },
                onToggleReplyThread = { onToggleReplyThread(replyId) },
                onReplyTextChanged = { newText ->
                    onReplyTextChanged(
                        ReplyTarget.Reply(
                            reviewId = node.reply.reviewId,
                            parentReplyId = replyId
                        ),
                        newText
                    )
                },
                onSendReply = {
                    onSendReply(
                        ReplyTarget.Reply(
                            reviewId = node.reply.reviewId,
                            parentReplyId = replyId
                        )
                    )
                },
                onDeleteReply = { onDeleteReply(replyId) }
            )
        }
    }
}

/**
 * Single reply item row, Reddit-style.
 */
@Composable
fun ReplyItem(
    node: ReplyNodeUiState,
    replyText: String?,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: () -> Unit,
    onReplyTextChanged: (String) -> Unit,
    onSendReply: () -> Unit,
    onDeleteReply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val depthIndent = (node.depth.coerceAtLeast(0)) * 12.dp
    val reply = node.reply

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = depthIndent)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .heightIn(min = 32.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (node.isMine) "You" else reply.authorId,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "just now", // placeholder
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    if (node.isMine && !reply.isDeleted) {
                        IconButton(onClick = onDeleteReply) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete reply",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (reply.isDeleted) "[deleted]" else reply.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reply.isDeleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevatedButton(
                        onClick = {
                            onReplyAction(
                                ReplyTarget.Reply(
                                    reviewId = reply.reviewId,
                                    parentReplyId = reply.replyId
                                )
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Reply",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reply",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (node.totalChildrenCount > 0) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { onToggleReplyThread() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector =
                                    if (node.isExpanded) Icons.Filled.KeyboardArrowUp
                                    else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (node.isExpanded) {
                                    "Hide replies"
                                } else {
                                    "See replies (${node.totalChildrenCount})"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (node.isComposerOpen && !reply.isDeleted) {
                    Spacer(modifier = Modifier.height(6.dp))
                    InlineReplyComposer(
                        text = replyText.orEmpty(),
                        isSending = false,
                        placeholder = "Reply to this reply...",
                        onTextChanged = onReplyTextChanged,
                        onSend = onSendReply
                    )
                }
            }
        }
    }
}

/**
 * Compact inline reply composer used under the root review and each reply.
 */
@Composable
fun InlineReplyComposer(
    text: String,
    isSending: Boolean,
    placeholder: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 40.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(999.dp),
            textStyle = MaterialTheme.typography.bodySmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        if (isSending) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        } else {
            Surface(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend()
                    }
                },
                shape = RoundedCornerShape(999.dp),
                color = MaterialTheme.colorScheme.primary,
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send reply",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Send",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
