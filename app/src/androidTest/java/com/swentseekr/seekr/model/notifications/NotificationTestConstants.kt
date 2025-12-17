package com.swentseekr.seekr.model.notifications

/** Shared constants for the `Notification` tests. */
object NotificationTestConstants {
  const val EMULATOR_HOST = "10.0.2.2"

  const val AUTH_EMULATOR_PORT = 9099
  const val FIRESTORE_EMULATOR_PORT = 8080

  const val TITLE = "title"
  const val TEST_TITLE = "Test Title"
  const val BODY = "body"
  const val TEST_BODY = "Test Body"

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

  const val NOTIFICATION_MESSAGE = "Notifications should be enabled"

  const val TEST_FCM_TOKEN_PREFIX = "test_fcm_token_"

  const val WAIT_TIMEOUT_MS = 5000L
  const val POLL_DELAY_MS = 200L
  const val NULL_VALUE = 0
  const val THREAD_SLEEP_TIME = 2
  const val NEW_REVIEW = "New Review"
  const val REVIEW_MESSAGE = "Someone reviewed your hunt!"
  const val GENERAL_NOTIFICATION = "General Notification"
  const val GENERAL_MESSAGE = "This is a general message"
  const val EXTRA_HUNT_ID = "huntId"
  const val TEST_HUNT_ID = "test_hunt_123"
  const val TEST_HUNT_ID_456 = "hunt_456"
  const val TEST_HUNT_ID_789 = "hunt_789"
  const val HUNT_REMOTE = "hunt_remote_123"
  const val EMPTY_STRING = ""
  const val HUNT_SPECIAL_CHAR = "hunt_with-special_chars.123"
  const val HUNT_PRESERVE = "hunt_preserve_test"
  const val HUNT_REMOVE = "hunt_remove_test"
  const val HUNT_FCM = "hunt_fcm_data_test"
}
