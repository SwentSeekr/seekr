package com.swentseekr.seekr.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.hunt.Difficulty
import com.swentseekr.seekr.model.hunt.Hunt
import com.swentseekr.seekr.model.hunt.HuntStatus
import com.swentseekr.seekr.model.map.Location

/**
 * Modern Hunt Card with image-dominant design
 */
@Composable
fun HuntCard(hunt: Hunt, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(12.dp)
            .fillMaxWidth(0.92f),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // IMAGE SECTION WITH GRADIENT OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Hunt Image
                AsyncImage(
                    model = hunt.mainImageUrl.takeIf { it.isNotBlank() },
                    contentDescription = HuntCardScreenStrings.HuntPictureDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.empty_image),
                    error = painterResource(R.drawable.empty_image)
                )

                // Dark gradient overlay for text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.3f),
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = 600f
                            )
                        )
                )

                // DIFFICULTY BADGE (top left)
                ModernDifficultyBadge(
                    difficulty = hunt.difficulty,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                )

                // LIKE BUTTON (top right)
                IconButton(
                    onClick = { /* Handle like */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .testTag(HuntCardScreenStrings.LikeButton)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = HuntCardScreenStrings.LikeButton,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // TITLE AND AUTHOR (bottom left on image)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = hunt.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${HuntCardScreenStrings.By} ${hunt.authorId}",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // STATS ROW (below image)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance stat
                ModernStatChip(
                    icon = Icons.Filled.LocationOn,
                    value = "${hunt.distance}",
                    unit = HuntCardScreenStrings.DistanceUnit,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Time stat
                ModernStatChip(
                    //Todo : change icon
                    icon = Icons.Filled.Favorite,
                    value = "${hunt.time}",
                    unit = HuntCardScreenStrings.TimeUnit,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Modern difficulty badge with rounded pill shape
 */
@Composable
fun ModernDifficultyBadge(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor) = when (difficulty) {
        Difficulty.EASY -> Color(0xFF4CAF50) to Color.White
        Difficulty.INTERMEDIATE -> Color(0xFFFFA726) to Color.White
        Difficulty.DIFFICULT -> Color(0xFFEF5350) to Color.White
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = 4.dp
    ) {
        Text(
            text = difficulty.toString(),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Modern stat chip with icon
 */
@Composable
fun ModernStatChip(
    icon: ImageVector,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$value $unit",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )
        }
    }
}

/** Preview */
@Preview
@Composable
fun ModernHuntCardPreview() {
    val hunt = Hunt(
        uid = "hunt123",
        start = Location(40.7128, -74.0060, "New York"),
        end = Location(40.730610, -73.935242, "Brooklyn"),
        middlePoints = emptyList(),
        status = HuntStatus.FUN,
        title = "City Exploration",
        description = "Discover hidden gems in the city",
        time = 2.5,
        distance = 5.0,
        difficulty = Difficulty.DIFFICULT,
        authorId = "JohnDoe",
        mainImageUrl = "",
        otherImagesUrls = emptyList(),
        reviewRate = 4.5
    )
    HuntCard(hunt, Modifier.padding(8.dp))
}