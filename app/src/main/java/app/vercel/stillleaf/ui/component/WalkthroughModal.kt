package app.vercel.stillleaf.ui.component

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.vercel.stillleaf.ui.theme.DeepForestAccent
import app.vercel.stillleaf.ui.theme.PillShape
import app.vercel.stillleaf.ui.theme.PrimarySage
import app.vercel.stillleaf.ui.theme.SurfaceWhite

object WalkthroughManager {
    private const val PREFS_NAME = "still_leaf_walkthrough_prefs"
    private const val KEY_HAS_SEEN_WALKTHROUGH = "key_has_seen_walkthrough"

    fun hasSeenWalkthrough(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_HAS_SEEN_WALKTHROUGH, false)
    }

    fun markWalkthroughSeen(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HAS_SEEN_WALKTHROUGH, true).apply()
    }
}

data class WalkthroughSlide(
    val icon: ImageVector,
    val header: String,
    val body: String
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WalkthroughModal(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val slides = listOf(
        WalkthroughSlide(
            icon = Icons.Default.CameraAlt,
            header = "Precision Botanical Identification",
            body = "Combine a close-up leaf detail shot with a full potted plant overview. Our Botanical Vision Engine identifies the exact species and generates custom care intervals for watering, feeding, rotating, and pruning."
        ),
        WalkthroughSlide(
            icon = Icons.Default.CalendarMonth,
            header = "Peaceful Care Routine & Scheduling",
            body = "Daily care tasks appear in Care & Calendar on the exact day they're due. Checking off a task clears it from your daily schedule until its recommended care cycle is due again."
        ),
        WalkthroughSlide(
            icon = Icons.Default.Spa,
            header = "Quarterly Visual Growth Journal",
            body = "Track your sanctuary over time. Compare Day 1 baseline photos side-by-side with 90-day milestone snapshots to celebrate your plant's growth progress."
        )
    )

    fun finishWalkthrough() {
        WalkthroughManager.markWalkthroughSeen(context)
        onDismiss()
    }

    Dialog(onDismissRequest = { finishWalkthrough() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row (Skip Button on Left, Close Icon on Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { finishWalkthrough() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    IconButton(onClick = { finishWalkthrough() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Walkthrough")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Slide Content
                AnimatedContent(
                    targetState = currentSlideIndex,
                    label = "WalkthroughSlideTransition"
                ) { targetIndex ->
                    val slide = slides[targetIndex]
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = PrimarySage.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = PrimarySage,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = slide.header,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepForestAccent,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = slide.body,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { index ->
                        val isSelected = index == currentSlideIndex
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 10.dp else 8.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) PrimarySage else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Button (Next vs Explore Sanctuary)
                val isLastSlide = currentSlideIndex == slides.size - 1

                Button(
                    onClick = {
                        if (isLastSlide) {
                            finishWalkthrough()
                        } else {
                            currentSlideIndex++
                        }
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySage),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastSlide) "Explore My Sanctuary 🌿" else "Next",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!isLastSlide) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
