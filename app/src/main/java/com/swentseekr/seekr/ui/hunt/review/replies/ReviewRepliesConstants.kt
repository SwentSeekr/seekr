package com.swentseekr.seekr.ui.hunt.review.replies

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Constants used in the inline replies UI.
 *
 * This file contains:
 * - User-visible strings
 * - Layout / typography dimensions
 * - Alpha values for UI elements
 * - Numeric constants
 * - Test tags for Compose UI tests
 * - Internal model constants
 */
object ReviewRepliesStrings {

  // -----------------
  // General actions
  // -----------------
  const val HIDE_REPLIES = "Hide replies"
  const val SHOW_REPLIES = "Show replies"
  const val REPLY = "Reply"
  const val DELETE = "Delete"
  const val ERROR_EMPTY_REPLY = "Reply cannot be empty."
  const val YOU = "You"

  // -----------------
  // Reddit-style UI / composer placeholders
  // -----------------
  const val ROOT_COMPOSER_PLACEHOLDER = "What are your thoughts?"
  const val INLINE_REPLY_PLACEHOLDER = "Write a reply..."
  const val BE_THE_FIRST_TO_REPLY = "Be the first to reply"
  const val REPLY_UNIT_SINGULAR = "reply"
  const val REPLY_UNIT_PLURAL = "replies"

  // -----------------
  // Sign-in / error messages
  // -----------------
  const val ERROR_SIGN_IN_TO_REPLY = "You must be signed in to reply."
  const val ERROR_SIGN_IN_TO_DELETE = "You must be signed in to delete a reply."
  const val ERROR_REPLY_NOT_FOUND = "Reply not found."
  const val ERROR_DELETE_NOT_OWNER = "You can only delete your own replies."
  const val ERROR_SEND_REPLY = "Failed to send reply."
  const val ERROR_DELETE_REPLY = "Failed to delete reply."

  // -----------------
  // Timestamps / author prefixes
  // -----------------
  const val JUST_NOW = " · just now"
  const val REPLY_AUTHOR_PREFIX = "u/"

  // -----------------
  // Content descriptions for accessibility
  // -----------------
  const val REPLY_CONTENT_DESCRIPTION = "Reply"
  const val DELETE_CONTENT_DESCRIPTION = "Delete"
  const val SEND_CONTENT_DESCRIPTION = "Send"

  // -----------------
  // Factory / internal error messages
  // -----------------
  const val ERROR_UNKNOWN_VIEW_MODEL_CLASS_PREFIX = "Unknown ViewModel class: "
}

/** Layout / typography constants for the replies UI. */
object ReviewRepliesDimensions {
  // -----------------
  // Root header
  // -----------------
  val RootHeaderVerticalPadding = 12.dp
  val RootHeaderHorizontalPadding = 4.dp
  val RootHeaderIconSize = 20.dp
  val RootHeaderIconSpacing = 10.dp

  // -----------------
  // Root section spacing
  // -----------------
  val RootComposerTopSpacing = 8.dp
  val RootErrorTopSpacing = 8.dp
  val ThreadTopSpacing = 16.dp

  // -----------------
  // Error message
  // -----------------
  val ErrorHorizontalPadding = 12.dp

  // -----------------
  // Thread list / hierarchy
  // -----------------
  val DepthIndentPerLevel = 20.dp
  val ThreadLineWidth = 2.dp
  val ThreadLineCornerRadius = 1.dp
  val ThreadLineHorizontalSpacing = 12.dp
  val ReplyVerticalSpacing = 12.dp

  // -----------------
  // Reply card
  // -----------------
  val ReplyCardCornerRadius = 8.dp
  val ReplyCardPadding = 12.dp
  val ReplyAvatarSize = 28.dp
  val ReplyAvatarFontSize = 12.sp
  val ReplyAvatarNameSpacing = 10.dp
  val ReplyHeaderBottomSpacing = 8.dp
  val ReplyTextLineHeight = 22.sp
  val ReplyTextBottomSpacing = 8.dp

  // -----------------
  // Reply actions
  // -----------------
  val ReplyButtonIconSize = 16.dp
  val ReplyButtonIconSpacing = 6.dp
  val ReplyTextButtonHorizontalPadding = 12.dp
  val ReplyTextButtonVerticalPadding = 4.dp

  // -----------------
  // Delete button
  // -----------------
  val DeleteButtonSize = 32.dp
  val DeleteIconSize = 18.dp

  // -----------------
  // Collapsed composer
  // -----------------
  val CollapsedComposerHorizontalPadding = 4.dp
  val CollapsedComposerHeight = 44.dp
  val CollapsedComposerCornerRadius = 24.dp

  // -----------------
  // Expanded composer
  // -----------------
  val ExpandedComposerHorizontalPadding = 4.dp
  val ExpandedComposerCornerRadius = 12.dp
  val ExpandedComposerContentHorizontalPadding = 12.dp
  val ExpandedComposerContentVerticalPadding = 8.dp
  val InlineComposerHorizontalSpacing = 8.dp
  val ExpandedComposerCompactHorizontalPadding = 0.dp

  // -----------------
  // Send button / progress
  // -----------------
  val SendProgressSize = 32.dp
  val SendProgressStrokeWidth = 2.dp
  val SendButtonOuterSize = 40.dp
  val SendButtonInnerSize = 36.dp
  val SendIconSize = 18.dp

  // -----------------
  // Borders
  // -----------------
  val OutlineBorderWidth = 1.dp
}

/** Alpha constants for various UI elements. */
object ReviewRepliesAlphas {
  const val ROOT_HEADER_ICON = 0.8f
  const val ROOT_HEADER_PLACEHOLDER = 0.6f
  const val REPLY_TIMESTAMP = 0.6f
  const val DELETED_REPLY = 0.5f
  const val COMPOSER_PLACEHOLDER = 0.5f
  const val OUTLINE_VARIANT = 0.4f
  const val OUTLINE_VARIANT_BORDER = 0.5f
  const val INACTIVE_SEND_ICON = 0.5f
  const val COMPOSER_SURFACE_ALPHA = 0.3f
  const val DELETE_ICON_ALPHA = 0.7f
}

/** Non-UI numeric constants. */
object ReviewRepliesValues {
  const val ROOT_DEPTH = 0
  const val FULL_WEIGHT = 1f
  const val SINGLE_REPLY_COUNT = ROOT_DEPTH + 1
  const val AUTHOR_ID_MAX_LENGTH = 10
}

/** Test tags for the replies UI, to be used in Compose UI tests. */
object ReviewRepliesTestTags {
  // -----------------
  // Sections
  // -----------------
  const val REPLIES_SECTION = "ReviewReplies_Section"
  const val ROOT_SEE_REPLIES = "ReviewReplies_Root_SeeReplies"

  // -----------------
  // Inline composer
  // -----------------
  const val ROOT_INLINE_COMPOSER = "ReviewReplies_Root_InlineComposer"
  const val ROOT_INLINE_TEXT_FIELD = "ReviewReplies_Root_TextField"
  const val ROOT_INLINE_SEND_BUTTON = "ReviewReplies_Root_Send"

  // -----------------
  // Thread / reply items
  // -----------------
  const val THREAD_LIST = "ReviewReplies_ThreadList"
  const val REPLY_ITEM_PREFIX = "ReviewReplies_ReplyItem_"
  const val REPLY_SEE_REPLIES_PREFIX = "ReviewReplies_Reply_See_"
  const val REPLY_INLINE_COMPOSER_PREFIX = "ReviewReplies_ReplyComposer_"
  const val REPLY_DELETE_BUTTON_PREFIX = "ReviewReplies_ReplyDelete_"
}

/** Constants for ReviewRepliesUiModel. */
object ReviewRepliesUiModelConstants {
  const val TOTAL_REPLY_BASE_COUNT = 0
  const val ROOT_BASE_REPLY_TEXT = ""
}
