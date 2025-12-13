package com.swentseekr.seekr.model.authentication

import com.swentseekr.seekr.model.hunt.review.HuntReviewReplyRepositoryFirestore

/** Constants used by [AuthRepositoryFirebase]. */
object AuthRepositoryFirebaseConstantsString {
    const val ERROR_INFO_NOT_FOUND= "Login failed : Could not retrieve user information"
    const val ERROR_CREDENTIAL = "Login failed: Credential is not of type Google ID"
    const val ERROR_FAIL_LOGIN= "Login failed:"
    const val UNEXPECTED_ERROR = "Unexpected error."
}