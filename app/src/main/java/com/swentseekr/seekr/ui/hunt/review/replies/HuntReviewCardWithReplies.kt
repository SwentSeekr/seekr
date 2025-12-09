package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.swentseekr.seekr.model.hunt.HuntReview
import com.swentseekr.seekr.ui.profile.Profile

/**
 * High-level composable that wraps an existing review card with
 * an inline, expandable replies section.
 *
 * Intended usage:
 * - Keep your current card layout (avatar, rating stars, comment).
 * - Attach this composable inside the list of reviews on the hunt details screen.
 *
 * @param review The base [HuntReview] to display.
 * @param authorProfile Optional profile of the review author for avatar/name.
 * @param repliesState UI state describing replies for this review.
 * @param onToggleRootReplies Called when the user taps "See replies" / "Hide replies" on the root.
 * @param onRootReplyTextChanged Called when the text of the root inline reply composer changes.
 * @param onSendRootReply Called when the user taps "Send" in the root inline composer.
 * @param onReplyAction Callback triggered when user taps "Reply" on a given reply.
 * @param onToggleReplyThread Callback when user toggles "See replies" on a given reply.
 * @param onReplyTextChanged Callback for per-reply inline composer text changes.
 * @param onSendReply Callback for sending a per-reply inline response.
 * @param onDeleteReply Callback when the user taps delete on a reply they own.
 * @param modifier Layout modifier.
 */
@Composable
fun HuntReviewCardWithReplies(
    review: HuntReview,
    authorProfile: Profile? = null,
    repliesState: ReviewRepliesUiState,
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
    val currentRepliesState by rememberUpdatedState(repliesState)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ----------------- Base review header & body -----------------
            ReviewHeaderRow(
                review = review,
                authorProfile = authorProfile,
            )

            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // TODO: if you already have a dedicated "review card" composable,
            // move this header/body logic there and call it from here.

            Divider(modifier = Modifier.padding(top = 4.dp))

            // ----------------- Replies section -----------------
            ReviewRepliesSection(
                review = review,
                state = currentRepliesState,
                onToggleRootReplies = onToggleRootReplies,
                onRootReplyTextChanged = onRootReplyTextChanged,
                onSendRootReply = onSendRootReply,
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
 * Compact header row showing avatar, author name and rating stars.
 */
@Composable
private fun ReviewHeaderRow(
    review: HuntReview,
    authorProfile: Profile?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Resolve a display name from the profile
        val displayName = authorProfile?.author?.pseudonym ?: "Unknown user"

        // Avatar: simple circle with initial if you don't have an image yet
        val initial = displayName
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            ?: "U"

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                ReadOnlyStarRating(rating = review.rating)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", review.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Simple, non-interactive star rating bar.
 */
@Composable
private fun ReadOnlyStarRating(
    rating: Double,
    maxStars: Int = 5,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val fullStars = rating.toInt().coerceIn(0, maxStars)
        for (i in 1..maxStars) {
            val filled = i <= fullStars
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (filled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Full replies area under a review.
 *
 * - Shows "See replies" / "Hide replies" for the root.
 * - Renders the visible flattened [ReplyNodeUiState] list with indentation.
 * - Shows inline composers at root and under replies where applicable.
 */
@Composable
private fun ReviewRepliesSection(
    review: HuntReview,
    state: ReviewRepliesUiState,
    onToggleRootReplies: () -> Unit,
    onRootReplyTextChanged: (String) -> Unit,
    onSendRootReply: () -> Unit,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: (String) -> Unit,
    onReplyTextChanged: (ReplyTarget.Reply, String) -> Unit,
    onSendReply: (ReplyTarget.Reply) -> Unit,
    onDeleteReply: (String) -> Unit,
) {
    // Local expansion state for the *root* replies section.
    var rootExpanded by remember { mutableStateOf(false) }

    val totalRootReplies = state.replies.count { it.depth == 0 }
    val hasAnyReplies = totalRootReplies > 0

    if (hasAnyReplies || state.isLoading || state.errorMessage != null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (hasAnyReplies) {
                val label =
                    if (rootExpanded) "Hide replies" else "See replies ($totalRootReplies)"
                TextButton(
                    onClick = {
                        rootExpanded = !rootExpanded
                        onToggleRootReplies()
                    },
                ) {
                    Text(text = label, style = MaterialTheme.typography.labelLarge)
                }
            }

            if (state.isLoading) {
                Text(
                    text = "Loading…",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (state.errorMessage != null) {
        Text(
            text = state.errorMessage,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 4.dp),
        )
    }

    // Visible replies only when root is expanded.
    if (rootExpanded && state.replies.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.replies.forEach { node ->
                ReplyItem(
                    node = node,
                    rootReviewId = review.reviewId,
                    replyText = state.childReplyTexts[node.reply.replyId].orEmpty(),
                    onReplyAction = onReplyAction,
                    onToggleReplyThread = onToggleReplyThread,
                    onReplyTextChanged = onReplyTextChanged,
                    onSendReply = onSendReply,
                    onDeleteReply = onDeleteReply,
                )
            }
        }
    }

    // Root inline composer (replying directly to the review itself).
    Spacer(modifier = Modifier.height(8.dp))
    InlineReplyComposer(
        value = state.rootReplyText,
        onValueChange = onRootReplyTextChanged,
        onSend = onSendRootReply,
        enabled = !state.isSendingReply,
        placeholder = "Reply to this review…",
    )
}

/**
 * Single reply row + its local actions (Reply / See replies / Delete).
 */
@Composable
private fun ReplyItem(
    node: ReplyNodeUiState,
    rootReviewId: String,
    replyText: String,
    onReplyAction: (ReplyTarget.Reply) -> Unit,
    onToggleReplyThread: (String) -> Unit,
    onReplyTextChanged: (ReplyTarget.Reply, String) -> Unit,
    onSendReply: (ReplyTarget.Reply) -> Unit,
    onDeleteReply: (String) -> Unit,
) {
    val indent = 24.dp * node.depth.coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = indent),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tiny avatar "bubble"
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = node.reply.authorId
                        .takeLast(3) // placeholder, replace with profile if you fetch it
                        .uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = node.reply.comment.takeIf { !node.reply.isDeleted }
                        ?: "[deleted]",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (node.isMine && !node.reply.isDeleted) {
                IconButton(onClick = { onDeleteReply(node.reply.replyId) }) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete reply",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Actions row under each reply
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = {
                    onReplyAction(
                        ReplyTarget.Reply(
                            reviewId = rootReviewId,
                            parentReplyId = node.reply.replyId,
                        )
                    )
                },
            ) {
                Text(
                    text = "Reply",
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            if (node.totalChildrenCount > 0) {
                val label =
                    if (node.isExpanded) {
                        "Hide replies"
                    } else {
                        "See replies (${node.totalChildrenCount})"
                    }
                TextButton(onClick = { onToggleReplyThread(node.reply.replyId) }) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        // Inline composer for this reply (if open).
        if (node.isComposerOpen && !node.reply.isDeleted) {
            InlineReplyComposer(
                value = replyText,
                onValueChange = {
                    onReplyTextChanged(
                        ReplyTarget.Reply(
                            reviewId = rootReviewId,
                            parentReplyId = node.reply.replyId,
                        ),
                        it,
                    )
                },
                onSend = {
                    onSendReply(
                        ReplyTarget.Reply(
                            reviewId = rootReviewId,
                            parentReplyId = node.reply.replyId,
                        )
                    )
                },
                placeholder = "Reply to this comment…",
            )
        }
    }
}

/**
 * Reusable inline reply text field + "Send" action row.
 */
@Composable
private fun InlineReplyComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean = true,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                )
            },
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = false,
            maxLines = 3,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val canSend = value.isNotBlank() && enabled
            TextButton(
                onClick = {
                    if (canSend) onSend()
                },
                enabled = canSend,
            ) {
                Text(
                    text = "Send",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}
