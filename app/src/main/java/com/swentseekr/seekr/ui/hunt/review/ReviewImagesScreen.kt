package com.swentseekr.seekr.ui.hunt.review

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.swentseekr.seekr.ui.hunt.preview.PreviewHuntViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp
import  androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.swentseekr.seekr.ui.hunt.preview.UI_CONST

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewImagesScreen(//reviewImageViewModel: ReviewImageViewModel,
    //reviewHuntViewModel: ReviewHuntViewModel,
    photoUrls: List<String>,
                         onGoBack: () -> Unit,
                       modifier: Modifier = Modifier,) {

    //val uiState =  reviewImageViewModel.uiState.collectAsState()
    //val photoUrls = uiState.value.photos

    //val uiState by reviewHuntViewModel.uiState.collectAsState()
    //val photoUrls = uiState.photos
    val scroll = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Images Review") },
                navigationIcon = {
                    IconButton(onClick = onGoBack, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back")
                    }
                }
            )
        },  modifier = Modifier.testTag("TOP_BAR_TEST_TAG")) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(innerPadding)
                .verticalScroll(scroll)

        ) {
            Text("Bug")
            Text(photoUrls.size.toString())
            photoUrls.forEach { url ->
                Text ("2")
                Log.d("ReviewImagesScreen", "URL: $url")
                AsyncImage(
                    model = url,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
        }
    }


}