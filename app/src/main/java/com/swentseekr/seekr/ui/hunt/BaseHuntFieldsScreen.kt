package com.swentseekr.seekr.ui.hunt

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.ui.hunt.BaseHuntFieldsStrings.REMOVE_BUTTON_TAG_PREFIX

sealed class OtherImage {
    data class Remote(val url: String) : OtherImage()
    data class Local(val uri: Uri) : OtherImage()
}

@Composable
fun ValidatedOutlinedField(
    value: String = BaseHuntFieldsStrings.TITLE_DEFAULT,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMsg: String?,
    testTag: String
) {
    ValidatedOutlinedField(
        value = value,
        onValueChange = onValueChange,
        label = label,
        placeholder = placeholder,
        errorMsg = errorMsg,
        testTag = testTag,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(BaseHuntFieldsUi.FieldCornerRadius),
        colors =
            OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary))
}

@Composable
private fun ValidatedOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    errorMsg: String?,
    testTag: String,
    modifier: Modifier,
    shape: Shape,
    colors: TextFieldColors
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        isError = errorMsg != null,
        supportingText = {
            errorMsg?.let { Text(it, modifier = Modifier.testTag(HuntScreenTestTags.ERROR_MESSAGE)) }
        },
        modifier = modifier.testTag(testTag),
        shape = shape,
        colors = colors)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseHuntFieldsScreen(
    title: String = BaseHuntFieldsStrings.TITLE_DEFAULT,
    uiState: HuntUIState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDistanceChange: (String) -> Unit,
    onDifficultySelect: (Difficulty) -> Unit,
    onStatusSelect: (HuntStatus) -> Unit,
    onSelectLocations: () -> Unit,
    onSelectImage: (Uri?) -> Unit,
    onSelectOtherImages: (List<Uri>) -> Unit,
    onRemoveOtherImage: (Uri) -> Unit,
    onRemoveExistingImage: (String) -> Unit,
    onSave: () -> Unit,
    onGoBack: () -> Unit,
    showDeleteAction: Boolean = false,
    onDeleteClick: (() -> Unit)? = null,
) {
    var showStatusDropdown by rememberSaveable { mutableStateOf(false) }
    var showDifficultyDropdown by rememberSaveable { mutableStateOf(false) }
    var showDeleteButton by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Image selector launcher (main image)
    val imagePickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(), onResult = { uri -> onSelectImage(uri) })

    // Launcher for multiple images selection (other images)
    val multipleImagesPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetMultipleContents(),
            onResult = { uris -> onSelectOtherImages(uris) })

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = BaseHuntFieldsStrings.BACK_CONTENT_DESC)
                    }
                },
                actions = {
                    if (showDeleteAction && onDeleteClick != null) {
                        IconButton(onClick = { showDeleteButton = !showDeleteButton }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More actions")
                        }
                    }
                },
            )
        },
        modifier = Modifier.testTag(HuntScreenTestTags.ADD_HUNT_SCREEN)) { paddingValues ->

        Box(
            modifier =
                Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(BaseHuntFieldsUi.ScreenPadding)
                    .padding(paddingValues)) {

            // MAIN SCROLLABLE CONTENT
            Column(
                modifier =
                    Modifier.fillMaxSize()
                        .verticalScroll(scrollState)
                        .testTag(HuntScreenTestTags.COLLUMN_HUNT_FIELDS),
            ) {
                val fieldShape = RoundedCornerShape(BaseHuntFieldsUi.FieldCornerRadius)
                val fieldColors =
                    OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )

                // === Form ===
                ValidatedOutlinedField(
                    value = uiState.title,
                    onValueChange = onTitleChange,
                    label = BaseHuntFieldsStrings.LABEL_TITLE,
                    placeholder = BaseHuntFieldsStrings.PLACEHOLDER_TITLE,
                    errorMsg = uiState.invalidTitleMsg,
                    testTag = HuntScreenTestTags.INPUT_HUNT_TITLE
                )

                ValidatedOutlinedField(
                    value = uiState.description,
                    onValueChange = onDescriptionChange,
                    label = BaseHuntFieldsStrings.LABEL_DESCRIPTION,
                    placeholder = BaseHuntFieldsStrings.PLACEHOLDER_DESCRIPTION,
                    errorMsg = uiState.invalidDescriptionMsg,
                    testTag = HuntScreenTestTags.INPUT_HUNT_DESCRIPTION,
                    modifier = Modifier.fillMaxWidth().height(BaseHuntFieldsUi.DescriptionHeight),
                    shape = fieldShape,
                    colors = fieldColors
                )

                // === STATUS ===
                ExposedDropdownMenuBox(
                    expanded = showStatusDropdown, onExpandedChange = { showStatusDropdown = it }) {
                    OutlinedTextField(
                        value = uiState.status?.name ?: "",
                        onValueChange = {},
                        label = { Text(BaseHuntFieldsStrings.LABEL_STATUS) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector =
                                    if (showStatusDropdown) Icons.Outlined.KeyboardArrowUp
                                    else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = BaseHuntFieldsStrings.EXPAND_STATUS_DESC
                            )
                        },
                        modifier =
                            Modifier.menuAnchor()
                                .fillMaxWidth()
                                .testTag(HuntScreenTestTags.DROPDOWN_STATUS),
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }) {
                        HuntStatus.values().forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status.name) },
                                onClick = {
                                    onStatusSelect(status)
                                    showStatusDropdown = false
                                })
                        }
                    }
                }

                // === DIFFICULTY ===
                ExposedDropdownMenuBox(
                    expanded = showDifficultyDropdown,
                    onExpandedChange = { showDifficultyDropdown = it }) {
                    OutlinedTextField(
                        value = uiState.difficulty?.name ?: "",
                        onValueChange = {},
                        label = { Text(BaseHuntFieldsStrings.LABEL_DIFFICULTY) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector =
                                    if (showDifficultyDropdown) Icons.Outlined.KeyboardArrowUp
                                    else Icons.Outlined.KeyboardArrowDown,
                                contentDescription = BaseHuntFieldsStrings.EXPAND_DIFFICULTY_DESC
                            )
                        },
                        modifier =
                            Modifier.menuAnchor()
                                .fillMaxWidth()
                                .testTag(HuntScreenTestTags.DROPDOWN_DIFFICULTY),
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    ExposedDropdownMenu(
                        expanded = showDifficultyDropdown,
                        onDismissRequest = { showDifficultyDropdown = false }) {
                        Difficulty.values().forEach { diff ->
                            DropdownMenuItem(
                                text = { Text(diff.name) },
                                onClick = {
                                    onDifficultySelect(diff)
                                    showDifficultyDropdown = false
                                })
                        }
                    }
                }

                ValidatedOutlinedField(
                    value = uiState.time,
                    onValueChange = onTimeChange,
                    label = BaseHuntFieldsStrings.LABEL_TIME,
                    placeholder = BaseHuntFieldsStrings.PLACEHOLDER_TIME,
                    errorMsg = uiState.invalidTimeMsg,
                    testTag = HuntScreenTestTags.INPUT_HUNT_TIME
                )

                ValidatedOutlinedField(
                    value = uiState.distance,
                    onValueChange = onDistanceChange,
                    label = BaseHuntFieldsStrings.LABEL_DISTANCE,
                    placeholder = BaseHuntFieldsStrings.PLACEHOLDER_DISTANCE,
                    errorMsg = uiState.invalidDistanceMsg,
                    testTag = HuntScreenTestTags.INPUT_HUNT_DISTANCE
                )

                Button(
                    onClick = onSelectLocations,
                    modifier =
                        Modifier.fillMaxWidth()
                            .testTag(HuntScreenTestTags.BUTTON_SELECT_LOCATION),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                ) {
                    val pointCount = uiState.points.size
                    val label =
                        if (pointCount > 0) {
                            "${BaseHuntFieldsStrings.BUTTON_SELECT_LOCATIONS} ($pointCount ${
                                if (pointCount == 1)
                                    BaseHuntFieldsStrings.UNIT_POINT
                                else BaseHuntFieldsStrings.UNIT_POINTS
                            })"
                        } else {
                            BaseHuntFieldsStrings.BUTTON_SELECT_LOCATIONS
                        }
                    Text(label)
                }

                // IMAGE PICKER + PREVIEW
                Spacer(modifier = Modifier.height(BaseHuntFieldsUi.SpacerHeightSmall))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(BaseHuntFieldsStrings.BUTTON_CHOOSE_IMAGE)
                }

                val imageToDisplay = uiState.mainImageUrl

                if (!imageToDisplay.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(BaseHuntFieldsUi.SpacerHeightMedium))
                    AsyncImage(
                        model = imageToDisplay,
                        contentDescription = BaseHuntFieldsStrings.CONTENT_DESC_SELECTED_IMAGE,
                        modifier =
                            Modifier.fillMaxWidth()
                                .height(BaseHuntFieldsUi.ImageHeight)
                                .clip(RoundedCornerShape(BaseHuntFieldsUi.FieldCornerRadius)),
                        placeholder = painterResource(R.drawable.empty_image),
                        error = painterResource(R.drawable.empty_image)
                    )
                }

                // OTHER IMAGES SECTION – EDIT + ADD MODE
                Spacer(modifier = Modifier.height(BaseHuntFieldsUi.SpacerHeightSmall))

                Button(
                    onClick = { multipleImagesPickerLauncher.launch("image/*") },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(BaseHuntFieldsStrings.BUTTON_CHOOSE_ADDITIONAL_IMAGES)
                }

                val combinedImages: List<OtherImage> =
                    uiState.otherImagesUrls.map { OtherImage.Remote(it) } +
                            uiState.otherImagesUris.map { OtherImage.Local(it) }

                if (combinedImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(BaseHuntFieldsUi.SpacerHeightMedium))

                    Column {
                        combinedImages.forEach { image ->
                            val tagSuffix =
                                when (image) {
                                    is OtherImage.Remote -> image.url
                                    is OtherImage.Local -> image.uri.toString()
                                }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val model =
                                    when (image) {
                                        is OtherImage.Remote -> image.url
                                        is OtherImage.Local -> image.uri
                                    }

                                AsyncImage(
                                    model = model,
                                    contentDescription =
                                        BaseHuntFieldsStrings.CONTENT_DESC_SECONDARY_IMAGE,
                                    modifier =
                                        Modifier.testTag("otherImage_$tagSuffix")
                                            .weight(BaseHuntFieldsUi.ImageWeight)
                                            .height(
                                                BaseHuntFieldsUi.ImageHeight /
                                                        BaseHuntFieldsUi.ImageHeightDivisor
                                            )
                                            .clip(
                                                RoundedCornerShape(BaseHuntFieldsUi.FieldCornerRadius)
                                            ),
                                    placeholder = painterResource(R.drawable.empty_image),
                                    error = painterResource(R.drawable.empty_image)
                                )

                                Spacer(modifier = Modifier.width(BaseHuntFieldsUi.SpacerHeightSmall))

                                TextButton(
                                    modifier =
                                        Modifier.testTag("$REMOVE_BUTTON_TAG_PREFIX$tagSuffix"),
                                    onClick = {
                                        when (image) {
                                            is OtherImage.Remote -> onRemoveExistingImage(image.url)
                                            is OtherImage.Local -> onRemoveOtherImage(image.uri)
                                        }
                                    },
                                    colors =
                                        ButtonDefaults.textButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_delete),
                                        contentDescription =
                                            BaseHuntFieldsStrings.DELETE_ICON_DESC,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier =
                                            Modifier.size(BaseHuntFieldsUi.IconSizeSmall)
                                    )
                                    Spacer(
                                        modifier =
                                            Modifier.width(BaseHuntFieldsUi.SpacerSuperSmall)
                                    )
                                    Text(BaseHuntFieldsStrings.REMOVE)
                                }
                            }

                            Spacer(modifier = Modifier.height(BaseHuntFieldsUi.SpacerHeightSmall))
                        }
                    }

                    Spacer(modifier = Modifier.height(BaseHuntFieldsUi.SpacerHeight))
                }

                Spacer(modifier = Modifier.height(BaseHuntFieldsUi.SpacerHeight))

                Button(
                    onClick = onSave,
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(BaseHuntFieldsUi.SaveButtonHeight)
                            .clip(
                                RoundedCornerShape(BaseHuntFieldsUi.SaveButtonCornerRadius)
                            )
                            .testTag(HuntScreenTestTags.HUNT_SAVE),
                    enabled = uiState.isValid,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                ) {
                    Text(
                        BaseHuntFieldsStrings.BUTTON_SAVE_HUNT,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // FLOATING DELETE BUTTON OVERLAY (only on Edit)
            if (showDeleteAction && onDeleteClick != null && showDeleteButton) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopEnd,
                ) {
                    Button(
                        onClick = onDeleteClick,
                        modifier = Modifier.padding(top = 1.dp, end = 8.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                        shape = RoundedCornerShape(999.dp) // pill shape, modern
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_delete),
                            contentDescription = "Delete hunt",
                            modifier = Modifier.size(BaseHuntFieldsUi.IconSizeSmall),
                            tint = MaterialTheme.colorScheme.onError,
                        )
                        Spacer(modifier = Modifier.width(BaseHuntFieldsUi.SpacerSuperSmall))
                        Text("Delete hunt")
                    }
                }
            }
        }}}