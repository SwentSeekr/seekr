package com.swentseekr.seekr.model.notifications

/**
 * Constant values used for Firebase Cloud Messaging (FCM) and local notification handling
 * throughout the app.
 */
object NotificationConstants {
  /** Default request code used for pending intents when no specific code is provided. */
  const val NULL_PENDING_INTENT_REQUEST_CODE = 0

  // -------------------------------
  // MyFirebaseMessagingService constants
  // -------------------------------
  const val COLLECTION_PROFILES = "profiles"
  const val FIELD_AUTHOR_FCM_TOKEN = "author.fcmToken"
  const val DEFAULT_NOTIFICATION_TITLE = "New Notification"
  const val DEFAULT_NOTIFICATION_BODY = ""
  const val TAG_FCM = "FCM"
  const val LOG_TOKEN_SAVED = "Token saved"
  const val LOG_TOKEN_FAILED = "Failed to save token"

  // -------------------------------
  // NotificationHelper constants
  // -------------------------------
  const val CHANNEL_ID = "default_channel"
  const val GENERAL_CHANNEL_NAME = "General Notifications"
  const val HUNT_ID = "huntId"
}
