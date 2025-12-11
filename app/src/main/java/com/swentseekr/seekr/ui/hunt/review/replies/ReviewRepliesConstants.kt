package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * User-visible strings for the inline replies UI.
 *
 * NOTE: later you can move these to your centralized localization system.
 */
object ReviewRepliesStrings {
  const val HideReplies = "Hide replies"
  const val ShowReplies = "Show replies"
  const val Reply = "Reply"
  const val Delete = "Delete"
  const val ErrorEmptyReply = "Reply cannot be empty."
  const val You = "You"

  // New strings used by the Reddit-style UI
  const val RootComposerPlaceholder = "What are your thoughts?"
  const val InlineReplyPlaceholder = "Write a reply..."
  const val Deleted = "[deleted]"
  const val BeTheFirstToReply = "Be the first to reply"
  const val ReplyUnitSingular = "reply"
  const val ReplyUnitPlural = "replies"
  const val ErrorSignInToReply = "You must be signed in to reply."
  const val ErrorSignInToDelete = "You must be signed in to delete a reply."
  const val ErrorReplyNotFound = "Reply not found."
  const val ErrorDeleteNotOwner = "You can only delete your own replies."
  const val ErrorRefreshReplies = "Failed to refresh replies"
  const val ErrorSendReply = "Failed to send reply."
  const val ErrorDeleteReply = "Failed to delete reply."
  const val JustNow = " · just now"
  const val ReplyAuthorPrefix = "u/"

  const val ReplyContentDescription = "Reply"
  const val DeleteContentDescription = "Delete"
  const val SendContentDescription = "Send"

  // Factory / internal error strings
  const val ErrorUnknownViewModelClassPrefix = "Unknown ViewModel class: "
}

/** Layout / typography constants for the replies UI. */
object ReviewRepliesDimensions {
  // Root header
  val RootHeaderVerticalPadding = 12.dp
  val RootHeaderHorizontalPadding = 4.dp
  val RootHeaderIconSize = 20.dp
  val RootHeaderIconSpacing = 10.dp

  // Root section spacing
  val RootComposerTopSpacing = 8.dp
  val RootErrorTopSpacing = 8.dp
  val ThreadTopSpacing = 16.dp

  // Error message
  val ErrorHorizontalPadding = 12.dp

  // Thread list / hierarchy
  val DepthIndentPerLevel = 20.dp
  val ThreadLineWidth = 2.dp
  val ThreadLineCornerRadius = 1.dp
  val ThreadLineHorizontalSpacing = 12.dp
  val ReplyVerticalSpacing = 12.dp

  // Reply card
  val ReplyCardCornerRadius = 8.dp
  val ReplyCardPadding = 12.dp
  val ReplyAvatarSize = 28.dp
  val ReplyAvatarFontSize = 12.sp
  val ReplyAvatarNameSpacing = 10.dp
  val ReplyHeaderBottomSpacing = 8.dp
  val ReplyTextLineHeight = 22.sp
  val ReplyTextBottomSpacing = 8.dp

  // Reply actions
  val ReplyButtonIconSize = 16.dp
  val ReplyButtonIconSpacing = 6.dp
  val ReplyTextButtonHorizontalPadding = 12.dp
  val ReplyTextButtonVerticalPadding = 4.dp

  // Delete button
  val DeleteButtonSize = 32.dp
  val DeleteIconSize = 18.dp

  // Collapsed composer
  val CollapsedComposerHorizontalPadding = 4.dp
  val CollapsedComposerHeight = 44.dp
  val CollapsedComposerCornerRadius = 24.dp

  // Expanded composer
  val ExpandedComposerHorizontalPadding = 4.dp
  val ExpandedComposerCornerRadius = 12.dp
  val ExpandedComposerContentHorizontalPadding = 12.dp
  val ExpandedComposerContentVerticalPadding = 8.dp
  val InlineComposerHorizontalSpacing = 8.dp
  val ExpandedComposerCompactHorizontalPadding = 0.dp

  // Send button / progress
  val SendProgressSize = 32.dp
  val SendProgressStrokeWidth = 2.dp
  val SendButtonOuterSize = 40.dp
  val SendButtonInnerSize = 36.dp
  val SendIconSize = 18.dp

  // Borders
  val OutlineBorderWidth = 1.dp
}

/** Alpha constants for various UI elements. */
object ReviewRepliesAlphas {
  const val RootHeaderIcon = 0.8f
  const val RootHeaderPlaceholder = 0.6f
  const val ReplyTimestamp = 0.6f
  const val DeletedReply = 0.5f
  const val ComposerPlaceholder = 0.5f
  const val OutlineVariant = 0.4f
  const val OutlineVariantBorder = 0.5f
  const val InactiveSendIcon = 0.5f
  const val ComposerSurfaceAlpha = 0.3f
  const val DeleteIconAlpha = 0.7f
}

/** Non-UI numeric constants. */
object ReviewRepliesValues {
  const val RootDepth = 0
  const val FullWeight = 1f
  const val SingleReplyCount = RootDepth + 1
  const val AuthorIdMaxLength = 10
}

/** Test tags for the replies UI, to be used in Compose UI tests. */
object ReviewRepliesTestTags {

  const val REPLIES_SECTION = "ReviewReplies_Section"
  const val ROOT_SEE_REPLIES = "ReviewReplies_Root_SeeReplies"

  const val ROOT_INLINE_COMPOSER = "ReviewReplies_Root_InlineComposer"
  const val ROOT_INLINE_TEXT_FIELD = "ReviewReplies_Root_TextField"
  const val ROOT_INLINE_SEND_BUTTON = "ReviewReplies_Root_Send"

  const val THREAD_LIST = "ReviewReplies_ThreadList"

  const val REPLY_ITEM_PREFIX = "ReviewReplies_ReplyItem_"
  const val REPLY_SEE_REPLIES_PREFIX = "ReviewReplies_Reply_See_"
  const val REPLY_INLINE_COMPOSER_PREFIX = "ReviewReplies_ReplyComposer_"
  const val REPLY_DELETE_BUTTON_PREFIX = "ReviewReplies_ReplyDelete_"
}

/** Constants for ReviewRepliesUiModel. */
object ReviewRepliesUiModelConstants {
  const val totalReplyBaseCount = 0
  const val rootReplyBaseText = ""
}
