package com.swentseekr.seekr.model.hunt.review

/** Constants used by [HuntReviewReplyRepositoryFirestore] for Firestore integration. */
object HuntReviewReplyFirestoreConstants {

  /** Firestore collection path where review replies are stored. */
  const val COLLECTION_PATH = "huntReviewReplies"

  /** Field names for [HuntReviewReply] documents in Firestore. */
  const val FIELD_REVIEW_ID = "reviewId"
  const val FIELD_PARENT_REPLY_ID = "parentReplyId"
  const val FIELD_AUTHOR_ID = "authorId"
  const val FIELD_COMMENT = "comment"
  const val FIELD_CREATED_AT = "createdAt"
  const val FIELD_UPDATED_AT = "updatedAt"
  const val FIELD_IS_DELETED = "isDeleted"

  /** Default values for missing/malformed Firestore fields. */
  const val DEFAULT_COMMENT_VALUE = ""
  const val DEFAULT_CREATED_AT_VALUE = 0L
  const val DEFAULT_IS_DELETED_VALUE = false

  /** Logging configuration. */
  const val LOG_TAG = "HuntReviewReplyRepositoryFirestore"
  const val LISTEN_ERROR_MESSAGE = "listenToReplies error"
  const val DOCUMENT_CONVERSION_ERROR_MESSAGE = "Error converting document to HuntReviewReply"

  /** Error message used when a reply does not exist. */
  fun replyNotFoundError(replyId: String): String = "Reply with id $replyId does not exist"

  /** Error message used when trying to persist a reply with a blank ID. */
  const val REPLY_ID_BLANK_ERROR = "Reply ID cannot be blank."
}
