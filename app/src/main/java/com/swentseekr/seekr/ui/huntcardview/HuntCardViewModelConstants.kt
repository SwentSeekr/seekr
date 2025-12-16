package com.swentseekr.seekr.ui.huntcardview

/**
 * Centralized constants used by [HuntCardViewModel] and related classes.
 */
object HuntCardViewModelConstants {

  //-------------------
  // Log tags
  //-------------------
  const val HUNT_CARD_TAG = "HuntCardViewModel"
  const val REVIEW_HUNT_TAG = "ReviewHuntViewModel"

  //-------------------
  // Profile loading errors
  //-------------------
  const val ERROR_LOADING_PROFILE = "Error loading user profile for User ID:"
  const val ERROR_LOADING_PROFILE_SET_MSG = "Unable to load author profile."
  const val UNKNOWN_USER = "unknown"
  const val ERROR_LOADING_CURRENT_USER = "Error loading current user ID"
  const val ERROR_LOADING_CURRENT_USER_SET_MSG = "Unable to load your account information."
  const val NO_USER = "None (B2)"


  //-------------------
  // Hunt and review loading errors
  //-------------------
  const val ERROR_LOADING_OTHER_REVIEWS = "Error loading reviews for Hunt ID:"
  const val ERROR_LOADING_OTHER_REVIEWS_SET_MSG = "Failed to load reviews."
  const val ERROR_LOADING_HUNT = "Error loading Hunt by ID:"
  const val ERROR_LOADING_HUNT_SET_MSG = "Failed to load hunt details."

  //-------------------
  // Hunt deletion and editing errors
  //-------------------
  const val ERROR_DELETE_HUNT = "Error in deleting Hunt by ID:"
  const val ERROR_DELETE_HUNT_SET_MSG = "Failed to delete hunt."
  const val ERROR_DELETING_REVIEW = "Error deleting Review for hunt"
  const val ERROR_DELETE_REVIEW_SET_MSG = "You can only delete your own review."
  const val ERROR_EDIT_HUNT = "Error in editing Hunt by ID:"
  const val ERROR_EDIT_HUNT_SET_MSG = "Failed to update hunt."

  //-------------------
  // Hunt completion errors
  //-------------------
  const val ERROR_ON_DONE_LOADING = "Hunt data is not loaded."
  const val ERROR_ON_DONE_CLICK = "Error marking hunt done"
  const val ERROR_ON_DONE_CLICK_SET_MSG = "Failed to mark hunt as done:"

  //-------------------
  // Photo related errors
  //-------------------
  const val ERROR_DELETING_PHOTO = "Error deleting photo:"
  const val ERROR_DELETING_PHOTO_SET_MSG = "Failed to delete photo"

  //-------------------
  // Author errors
  //-------------------
  const val ERROR_AUTHOR = "Error loading author"
  const val ERROR_AUTHOR_SET_MSG = "Error loading author profile"

  //-------------------
  // Like cache errors
  //-------------------
  const val ERROR_CACHE_LIKE = "Error loading liked hunts cache"
  const val ERROR_ON_LIKE = "Failed to update liked hunt:"

}
