package com.swentseekr.seekr.ui.hunt.review

/**
 * User-visible strings for the inline replies UI.
 *
 * NOTE: later you can move these to your centralized localization system.
 */
object ReviewRepliesStrings {
    const val SeeReplies = "See replies"
    const val HideReplies = "Hide replies"
    const val Reply = "Reply"
    const val Delete = "Delete"
    const val ReplyPlaceholder = "Add a reply..."
    const val Sending = "Sending..."
    const val NoRepliesYet = "No replies yet"
    const val ErrorEmptyReply = "Reply cannot be empty"
    const val ErrorGeneric = "Something went wrong, please try again."
    const val You = "You"
}

/**
 * Test tags for the replies UI, to be used in Compose UI tests.
 */
object ReviewRepliesTestTags {
    const val REVIEW_CARD_WITH_REPLIES = "ReviewCard_WithReplies"
    const val REPLIES_SECTION = "ReviewReplies_Section"
    const val ROOT_SEE_REPLIES = "ReviewReplies_Root_SeeReplies"
    const val ROOT_INLINE_COMPOSER = "ReviewReplies_Root_InlineComposer"
    const val ROOT_INLINE_TEXT_FIELD = "ReviewReplies_Root_TextField"
    const val ROOT_INLINE_SEND_BUTTON = "ReviewReplies_Root_Send"

    const val REPLY_ITEM_PREFIX = "ReviewReplies_ReplyItem_"          // + replyId
    const val REPLY_SEE_REPLIES_PREFIX = "ReviewReplies_Reply_See_"   // + replyId
    const val REPLY_INLINE_COMPOSER_PREFIX = "ReviewReplies_ReplyComposer_" // + replyId
    const val REPLY_INLINE_TEXT_FIELD_PREFIX = "ReviewReplies_ReplyText_"   // + replyId
    const val REPLY_INLINE_SEND_BUTTON_PREFIX = "ReviewReplies_ReplySend_"  // + replyId
    const val REPLY_DELETE_BUTTON_PREFIX = "ReviewReplies_ReplyDelete_"     // + replyId
}
