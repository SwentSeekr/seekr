package com.swentseekr.seekr.ui.hunt

/**
 * Provides centralized constants and UI configuration values used across Hunt-creation and
 * Hunt-editing screens within the Seekr application.
 *
 * This file contains three main objects:
 *
 * ### `BaseHuntFieldsStrings`
 * Holds all user-facing strings, content descriptions, placeholders, labels, button texts, and
 * units used throughout Hunt-related UI components. Centralizing these values ensures:
 * - Consistent wording across screens.
 * - Easier localization in the future.
 * - Cleaner composable files by removing inline string values.
 *
 * ### `BaseHuntFieldsUi`
 * Defines UI layout constants, such as paddings, corner radii, image sizes, button dimensions, and
 * alpha values used for styling. These standards ensure:
 * - Consistent spacing and visual hierarchy.
 * - Ease of adjusting UI design from a single source.
 *
 * ### `HuntScreenTestTags`
 * Provides test tag constants used by UI tests (e.g., Compose UI testing). Tags are placed on
 * interactive elements so automated tests can reliably target and validate them. Centralizing tags
 * prevents typos and makes UI tests more maintainable.
 *
 * Together, these objects support clean code practices by separating concerns:
 * - UI visuals in `BaseHuntFieldsUi`
 * - Text content in `BaseHuntFieldsStrings`
 * - Test identifiers in `HuntScreenTestTags`
 *
 * Any new Hunt-related screen or composable should reference these constants rather than defining
 * new ones inline.
 */
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object BaseHuntFieldsStrings {

  // Top bar
  const val BACK_CONTENT_DESC = "Back"

  // Fields
  const val LABEL_TITLE = "Title"
  const val PLACEHOLDER_TITLE = "Enter hunt name"
  const val LABEL_DESCRIPTION = "Description"
  const val PLACEHOLDER_DESCRIPTION = "Describe your hunt"
  const val LABEL_STATUS = "Status"
  const val EXPAND_STATUS_DESC = "Expand Status"
  const val LABEL_DIFFICULTY = "Difficulty"
  const val EXPAND_DIFFICULTY_DESC = "Expand Difficulty"
  const val LABEL_TIME = "Estimated Time (hours)"
  const val PLACEHOLDER_TIME = "(leave empty for suggested time)"
  const val LABEL_DISTANCE = "Distance (km)"
  const val PLACEHOLDER_DISTANCE = "(leave empty for suggested distance)"

  // Screens
  const val TITLE_DEFAULT = "Add your Hunt"

  // Buttons
  const val BUTTON_SELECT_LOCATIONS = "Select Locations"
  const val BUTTON_SAVE_HUNT = "Save Hunt"
  const val BUTTON_GALLERY = "Gallery"
  const val BUTTON_CAMERA = "Camera"
  const val BUTTON_REMOVE_IMAGE = "Remove image"
  const val BUTTON_REMOVE = "Remove"
  const val BUTTON_DELETE = "Delete"

  // Image labels
  const val LABEL_MAIN_IMAGE = "Main image"
  const val LABEL_ADDITIONAL_IMAGES = "Additional images"
  const val DELETE_ICON_DESC = "Delete Image"

  const val LABEL_IMAGES = "Images"
  const val UNIT_IMAGE = "image"
  const val UNIT_IMAGES = "images"
  const val CONTENT_DESC_MAIN_IMAGE = "Main image preview"
  const val CONTENT_DESC_ADDITIONAL_IMAGE = "Additional image preview"

  // Units
  const val UNIT_POINT = "point"
  const val UNIT_POINTS = "points"

  // Tags
  const val REMOVE_BUTTON_TAG_PREFIX = "removeButton_"

  const val IMAGE_LAUNCH = "image/*"
  const val DELETE_MAIN_IMAGE = "delete_main_image"
  const val NAME_1 = "P1"
  const val NAME_2 = "P2"
  const val OTHER_IMAGES = "otherImage_"
  const val MORE_DESCRIPTION = "More actions"
}

object BaseHuntFieldsUi {

  // Layout
  val ColumnHPadding: Dp = 20.dp
  val ColumnVArrangement: Dp = 16.dp
  val RowHArrangement: Dp = 12.dp

  // Fields
  val FieldCornerRadius: Dp = 16.dp
  val ChangeAlpha: Float = 0.5f
  val Alpha03: Float = 0.3f
  val WeightTextField: Float = 1f
  val DescriptionHeight: Dp = 140.dp

  // Images
  val ImageHeight: Dp = 200.dp
  val ImageThumbSize: Dp = 80.dp
  val ImageCornerRadius: Dp = 16.dp
  val ImageThumbCornerRadius: Dp = 8.dp

  // Buttons
  val ButtonHeight: Dp = 64.dp
  val ButtonCornerRadius: Dp = 16.dp

  val ImageButtonHeight: Dp = 48.dp
  val ImageButtonCornerRadius: Dp = 12.dp

  val ButtonSaveHeight: Dp = 56.dp
  val ButtonSaveCornerRadius: Dp = 16.dp
  val ButtonShadow: Dp = 8.dp

  // Cards
  val CardCornerRadius: Dp = 20.dp
  val CardPadding: Dp = 20.dp
  val CardVArrangement: Dp = 16.dp
  val CardRowPadding: Dp = 12.dp

  // Spacers
  val SpacerHeight: Dp = 16.dp
  val SpacerHeightSmall: Dp = 8.dp
  val SpacerHeightTiny: Dp = 4.dp

  // Icons
  val IconSize: Dp = 20.dp

  // Divider
  val DividerThickness: Dp = 1.dp
}

object BaseHuntConstantsDefault {
  const val DEFAULT_LATITUDE_1: Double = 0.0
  const val DEFAULT_LONGITUDE_1: Double = 0.0
  const val DEFAULT_LATITUDE_2: Double = 1.0
  const val DEFAULT_LONGITUDE_2: Double = 1.0
  const val POLYLINE = 2
  const val ONE = 1
  const val EMPTY_DISTANCE = 0.0
  const val FUN_SPEED = 4.0
  const val DISCOVER_SPEED = 4.5
  const val SPORT_SPEED = 6.5
  const val DEFAULT_SPEED = 5.0
  const val KILOMETER = 1000.0
}

object HuntScreenTestTags {
  const val INPUT_HUNT_TITLE = "inputHuntTitle"
  const val INPUT_HUNT_DESCRIPTION = "inputHuntDescription"
  const val INPUT_HUNT_TIME = "inputHuntTime"
  const val INPUT_HUNT_DISTANCE = "inputHuntDistance"
  const val DROPDOWN_STATUS = "dropdownStatus"
  const val DROPDOWN_DIFFICULTY = "dropdownDifficulty"
  const val BUTTON_SELECT_LOCATION = "buttonSelectLocation"
  const val HUNT_SAVE = "huntSave"
  const val ERROR_MESSAGE = "errorMessage"
  const val ADD_HUNT_SCREEN = "AddHuntScreen"
  const val COLLUMN_HUNT_FIELDS = "ColumnHuntFields"
}
