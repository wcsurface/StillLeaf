package app.vercel.stillleaf.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercel.stillleaf.R
import app.vercel.stillleaf.data.model.CareTaskType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.ui.component.StillLeafTopBar
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import app.vercel.stillleaf.ui.theme.PillShape
import app.vercel.stillleaf.ui.theme.SurfaceWhite
import coil.compose.AsyncImage
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data class CalendarTaskItem(
    val id: String,
    val plant: Plant,
    val taskType: CareTaskType,
    val taskName: String,
    val location: String,
    val isCompleted: Boolean
)

@Composable
fun CalendarScreen(
    plants: List<Plant>,
    onBackRequested: () -> Unit,
    onNavTabSelected: (NavTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedDayIndex by remember { mutableIntStateOf(2) } // Default to Wednesday/current day
    val today = remember { LocalDate.now() }

    // Generate 7 days for the weekly strip
    val weekDays = remember(today) {
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    // Task completion map
    val taskCompletionState = remember {
        mutableStateMapOf(
            "t1" to true,
            "t2" to false,
            "t3" to false,
            "t4" to true,
            "t5" to false
        )
    }

    // Sample Calendar Tasks matching Section 11 specifications
    val sampleTasks = remember(plants) {
        val p1 = plants.getOrNull(0)
        val p2 = plants.getOrNull(1)
        val p3 = plants.getOrNull(2)
        val p4 = plants.getOrNull(3)

        listOf(
            CalendarTaskItem(
                id = "t1",
                plant = p1 ?: plants.firstOrNull() ?: Plant(id = "dummy", nickname = "Sanctuary Plant", botanicalName = "", commonName = "", primaryPhotoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80", dateAdded = LocalDate.now(), careRequirements = app.vercel.stillleaf.data.model.CareRequirement("Light", "20C", "50%", 7, 30, 14, 60, "Care"), location = "Indoor"),
                taskType = CareTaskType.WATER,
                taskName = "Water ${p1?.nickname ?: "Sanctuary Plant"}",
                location = p1?.location ?: "Indoor • Living Room",
                isCompleted = taskCompletionState["t1"] ?: true
            ),
            CalendarTaskItem(
                id = "t2",
                plant = p2 ?: plants.firstOrNull() ?: Plant(id = "dummy", nickname = "Sanctuary Plant", botanicalName = "", commonName = "", primaryPhotoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80", dateAdded = LocalDate.now(), careRequirements = app.vercel.stillleaf.data.model.CareRequirement("Light", "20C", "50%", 7, 30, 14, 60, "Care"), location = "Indoor"),
                taskType = CareTaskType.FEED,
                taskName = "Fertilize ${p3?.nickname ?: "Patio Herb"}",
                location = p3?.location ?: "Outdoor • Patio",
                isCompleted = taskCompletionState["t2"] ?: false
            ),
            CalendarTaskItem(
                id = "t3",
                plant = p3 ?: plants.firstOrNull() ?: Plant(id = "dummy", nickname = "Sanctuary Plant", botanicalName = "", commonName = "", primaryPhotoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80", dateAdded = LocalDate.now(), careRequirements = app.vercel.stillleaf.data.model.CareRequirement("Light", "20C", "50%", 7, 30, 14, 60, "Care"), location = "Indoor"),
                taskType = CareTaskType.TRIM,
                taskName = "Trim ${p4?.nickname ?: "Garden Lavender"}",
                location = p4?.location ?: "Outdoor • Garden",
                isCompleted = taskCompletionState["t3"] ?: false
            ),
            CalendarTaskItem(
                id = "t4",
                plant = p4 ?: plants.firstOrNull() ?: Plant(id = "dummy", nickname = "Sanctuary Plant", botanicalName = "", commonName = "", primaryPhotoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80", dateAdded = LocalDate.now(), careRequirements = app.vercel.stillleaf.data.model.CareRequirement("Light", "20C", "50%", 7, 30, 14, 60, "Care"), location = "Indoor"),
                taskType = CareTaskType.WATER,
                taskName = "Water ${p2?.nickname ?: "Office Succulent"}",
                location = p2?.location ?: "Indoor • Office",
                isCompleted = taskCompletionState["t4"] ?: true
            ),
            CalendarTaskItem(
                id = "t5",
                plant = p1 ?: plants.firstOrNull() ?: Plant(id = "dummy", nickname = "Sanctuary Plant", botanicalName = "", commonName = "", primaryPhotoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80", dateAdded = LocalDate.now(), careRequirements = app.vercel.stillleaf.data.model.CareRequirement("Light", "20C", "50%", 7, 30, 14, 60, "Care"), location = "Indoor"),
                taskType = CareTaskType.WATER,
                taskName = "Check Soil Moisture",
                location = "Indoor • Bedroom",
                isCompleted = taskCompletionState["t5"] ?: false
            )
        )
    }

    val completedCount = sampleTasks.count { taskCompletionState[it.id] == true }
    val totalCount = sampleTasks.size

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Background Image (bg_calendar)
        Image(
            painter = painterResource(R.drawable.bg_calendar),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Screen Content
        Column(modifier = Modifier.fillMaxSize()) {
            // Unified Top Logo & Navbar
            StillLeafTopBar(
                currentTab = NavTab.CALENDAR,
                onNavTabSelected = onNavTabSelected
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Header Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Care & Calendar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CreamyOffWhite
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Weekly Calendar Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEachIndexed { index, date ->
                    val isSelected = index == selectedDayIndex
                    val weekdayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val dayOfMonth = date.dayOfMonth.toString()

                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else SurfaceWhite.copy(alpha = 0.2f),
                        animationSpec = tween(durationMillis = 200),
                        label = "DayBg"
                    )

                    Surface(
                        onClick = { selectedDayIndex = index },
                        shape = RoundedCornerShape(16.dp),
                        color = bgColor,
                        border = if (isSelected) null else BorderStroke(1.dp, CreamyOffWhite.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .width(44.dp)
                            .height(68.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = weekdayName,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else CreamyOffWhite.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayOfMonth,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else CreamyOffWhite,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Today's Tasks Section Header & Progress Counter Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CreamyOffWhite
                )

                Surface(
                    color = SurfaceWhite.copy(alpha = 0.25f),
                    shape = PillShape,
                    border = BorderStroke(1.dp, CreamyOffWhite.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "$completedCount/$totalCount",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CreamyOffWhite,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Individual Care Task Cards List
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sampleTasks, key = { it.id }) { task ->
                    val isCompleted = taskCompletionState[task.id] ?: false

                    CalendarTaskCard(
                        task = task,
                        isCompleted = isCompleted,
                        onToggleCompletion = {
                            taskCompletionState[task.id] = !isCompleted
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    // View Full Calendar Wide Button
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(20.dp))
                            Text(
                                text = "View Full Calendar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CalendarTaskCard(
    task: CalendarTaskItem,
    isCompleted: Boolean,
    onToggleCompletion: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onToggleCompletion() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Plant Thumbnail
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = task.plant.primaryPhotoUrl,
                        contentDescription = task.plant.nickname,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Task Name & Location
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.taskName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Circular Completion Checkbox Control
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        shape = CircleShape
                    )
                    .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
