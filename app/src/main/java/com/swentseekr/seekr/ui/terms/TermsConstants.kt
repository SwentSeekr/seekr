package com.swentseekr.seekr.ui.terms

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object TermsScreenConstants {
  val SCREEN_PADDING = 20.dp
  val SECTION_SPACING = 16.dp
  val CARD_CORNER_RADIUS = 20.dp
  val CARD_PADDING = 20.dp
  val CARD_ELEVATION = 2.dp
  val TEXT_SPACING = 12.dp
  val LINE_HEIGHT = 24.sp
  val TOP_SPACER = 8.dp
  val BOTTOM_SPACER = 24.dp
}

object TermsScreenStrings {
  const val TITLE = "Terms & Conditions"
  const val BACK_DESCRIPTION = "Go back"
  const val LAST_UPDATED = "Last updated: December 2024"

  const val SECTION_1_TITLE = "1. Acceptance of Terms"
  const val SECTION_1_CONTENT =
      "By accessing and using Seekr, you accept and agree to be bound by the terms and provisions of this agreement. If you do not agree to these terms, please do not use our application."

  const val SECTION_2_TITLE = "2. Use of Service"
  const val SECTION_2_CONTENT =
      "Seekr provides a platform for users to create, share, and participate in location-based treasure hunts. You agree to use the service only for lawful purposes and in accordance with these Terms and Conditions."

  const val SECTION_3_TITLE = "3. User Accounts"
  const val SECTION_3_CONTENT =
      "You are responsible for maintaining the confidentiality of your account credentials and for all activities that occur under your account. You agree to notify us immediately of any unauthorized use of your account."

  const val SECTION_4_TITLE = "4. Content and Conduct"
  const val SECTION_4_CONTENT =
      "Users are responsible for the content they create and share on Seekr. You agree not to post content that is illegal, offensive, or violates the rights of others. We reserve the right to remove any content that violates these terms."

  const val SECTION_5_TITLE = "5. Privacy and Data"
  const val SECTION_5_CONTENT =
      "Your privacy is important to us. We collect and use your personal information in accordance with our Privacy Policy. By using Seekr, you consent to our collection and use of your data as described in our Privacy Policy."

  const val SECTION_6_TITLE = "6. Limitation of Liability"
  const val SECTION_6_CONTENT =
      "Seekr is provided on an 'as is' and 'as available' basis. We make no warranties or representations about the accuracy or completeness of the content. We shall not be liable for any damages arising from your use of the application."
}

object TermsScreenTestTags {
  const val SCREEN = "TERMS_SCREEN"
  const val BACK_BUTTON = "TERMS_BACK_BUTTON"
  const val CONTENT_COLUMN = "TERMS_CONTENT_COLUMN"
}
