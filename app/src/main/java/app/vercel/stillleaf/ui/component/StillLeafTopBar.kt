package app.vercel.stillleaf.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercel.stillleaf.ui.screen.NavTab
import app.vercel.stillleaf.ui.theme.AttentionTerracotta
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import app.vercel.stillleaf.ui.theme.LakkiReddyFontFamily
import app.vercel.stillleaf.ui.theme.SurfaceWhite

@Composable
fun StillLeafTopBar(
    currentTab: NavTab,
    onNavTabSelected: (NavTab) -> Unit,
    pendingTasksCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 52.dp, bottom = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Centered Brand Logo & Slogan (Light Cream for high contrast across all pages)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = "Still Leaf Logo",
                tint = CreamyOffWhite,
                modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "STILL LEAF",
                fontFamily = LakkiReddyFontFamily,
                fontSize = 30.sp,
                color = CreamyOffWhite,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Healthy plants. Happier days.",
            style = MaterialTheme.typography.bodyMedium,
            color = CreamyOffWhite.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. Floating Top Navigation Pill Bar with Lakki Reddy Font Labels & Calendar Task Badge
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceWhite.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    val activeBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        label = "TabBg"
                    )

                    Surface(
                        onClick = { onNavTabSelected(tab) },
                        shape = RoundedCornerShape(20.dp),
                        color = activeBgColor,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                // Calendar Remaining Task Counter Badge Popup
                                if (tab == NavTab.CALENDAR && pendingTasksCount > 0) {
                                    Surface(
                                        color = AttentionTerracotta,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .offset(x = 8.dp, y = (-4).dp)
                                            .size(16.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (pendingTasksCount > 9) "9+" else pendingTasksCount.toString(),
                                                color = SurfaceWhite,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = tab.title,
                                fontFamily = LakkiReddyFontFamily,
                                fontSize = 12.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
