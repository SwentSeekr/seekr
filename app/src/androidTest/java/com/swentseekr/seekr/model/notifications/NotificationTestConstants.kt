package com.swentseekr.seekr.model.notifications

object NotificationTestConstants {
  const val EMULATOR_HOST = "10.0.2.2"

  const val AUTH_EMULATOR_PORT = 9099
  const val FIRESTORE_EMULATOR_PORT = 8080

  const val DEBUG_COLLECTION = "debug_notifications"
  const val PROFILES_COLLECTION = "profiles"
  const val SETTINGS_COLLECTION = "settings"

  const val FIELD_REVIEW_ID = "reviewId"
  const val FIELD_AUTHOR = "author"
  const val FIELD_PSEUDONYM = "pseudonym"
  const val FIELD_FCM_TOKEN = "fcmToken"
  const val AUTHOR_FIELD_FCM_TOKEN = "author.fcmToken"
  const val FIELD_NOTIFICATIONS = "notifications"

  const val TEST_PSEUDONYM = "TestUser"
  const val TEST_TITLE = "Test Title"
  const val TEST_MESSAGE = "Test Message."

  const val NOTIFICATION_MESSAGE = "Notifications should be enabled"

  const val TEST_FCM_TOKEN_PREFIX = "test_fcm_token_"

  const val WAIT_TIMEOUT_MS = 5000L
  const val POLL_DELAY_MS = 200L
}
