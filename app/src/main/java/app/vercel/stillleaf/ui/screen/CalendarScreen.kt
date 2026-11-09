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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.vercel.stillleaf.R
import app.vercel.stillleaf.data.model.CareTaskType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.ui.component.StillLeafTopBar
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import app.vercel.stillleaf.ui.theme.DeepForestAccent
import app.vercel.stillleaf.ui.theme.PillShape
import app.vercel.stillleaf.ui.theme.PrimarySage
import app.vercel.stillleaf.ui.theme.SurfaceWhite
import app.vercel.stillleaf.util.rememberImageModel
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
    val dueDate: LocalDate,
    val isCompleted: Boolean
)

@Composable
fun CalendarScreen(
    plants: List<Plant>,
    onBackRequested: () -> Unit,
    onNavTabSelected: (NavTab) -> Unit = {},
    onLegalRequested: (app.vercel.stillleaf.ui.component.LegalTab) -> Unit = {},
    onTaskToggled: (plantId: String, taskType: CareTaskType, isCompleted: Boolean) -> Unit = { _, _, _ -> },
    pendingTasksCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now() }
    val todayDayIndex = remember(today) { (today.dayOfWeek.value - 1).coerceIn(0, 6) }
    var selectedDayIndex by remember { mutableIntStateOf(todayDayIndex) }
    var showFullCalendarModal by remember { mutableStateOf(false) }

    // Generate 7 days for the weekly strip
    val weekDays = remember(today) {
        val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
        (0..6).map { startOfWeek.plusDays(it.toLong()) }
    }

    val selectedDate = weekDays.getOrElse(selectedDayIndex) { today }

    // Helper to get interval for a task type
    fun getIntervalForTask(plant: Plant, taskType: CareTaskType): Int {
        return when (taskType) {
            CareTaskType.WATER -> plant.careRequirements.waterIntervalDays
            CareTaskType.FEED -> plant.careRequirements.feedIntervalDays
            CareTaskType.ROTATE -> plant.careRequirements.rotateIntervalDays
            CareTaskType.TRIM -> plant.careRequirements.trimIntervalDays
            else -> 14
        }
    }

    // Helper to check if task was completed in the current active cycle
    fun isTaskCompletedInActiveCycle(plant: Plant, taskType: CareTaskType, targetDate: LocalDate = today): Boolean {
        val interval = getIntervalForTask(plant, taskType)
        val latestLogDate = plant.recentLogs
            .filter { it.taskType == taskType }
            .maxByOrNull { it.completedAt }
            ?.completedAt
            ?.toLocalDate() ?: return false

        val daysSinceLog = java.time.temporal.ChronoUnit.DAYS.between(latestLogDate, targetDate)
        return daysSinceLog in 0 until interval
    }

    // Helper to check if task is due on or before target date
    fun isTaskDueOnDate(plant: Plant, taskType: CareTaskType, targetDate: LocalDate = today): Boolean {
        val interval = getIntervalForTask(plant, taskType)
        val latestLogDate = plant.recentLogs
            .filter { it.taskType == taskType }
            .maxByOrNull { it.completedAt }
            ?.completedAt
            ?.toLocalDate() ?: return true

        val nextDueDate = latestLogDate.plusDays(interval.toLong())
        return nextDueDate <= targetDate
    }

    // Dynamic tasks due TODAY (Clears from list once completed for current cycle)
    val todayTasks = remember(plants, selectedDate) {
        plants.flatMap { plant ->
            val tasksList = mutableListOf<CalendarTaskItem>()

            CareTaskType.entries.forEach { taskType ->
                if (taskType == CareTaskType.WATER || taskType == CareTaskType.FEED || taskType == CareTaskType.ROTATE || taskType == CareTaskType.TRIM) {
                    val isDue = isTaskDueOnDate(plant, taskType, selectedDate)
                    val isCompleted = isTaskCompletedInActiveCycle(plant, taskType, selectedDate)

                    // Display task in Today's Tasks ONLY if due AND not yet completed for current active cycle
                    if (isDue && !isCompleted) {
                        val taskTitle = when (taskType) {
                            CareTaskType.WATER -> "Water ${plant.nickname}"
                            CareTaskType.FEED -> "Nourish ${plant.nickname}"
                            CareTaskType.ROTATE -> "Rotate ${plant.nickname} 90°"
                            CareTaskType.TRIM -> "Prune ${plant.nickname}"
                            else -> "Care for ${plant.nickname}"
                        }
                        tasksList.add(
                            CalendarTaskItem(
                                id = "task_${taskType.name}_${plant.id}_${selectedDate}",
                                plant = plant,
                                taskType = taskType,
                                taskName = taskTitle,
                                location = plant.location,
                                dueDate = selectedDate,
                                isCompleted = isCompleted
                            )
                        )
                    }
                }
            }
            tasksList
        }
    }

    // Function to calculate tasks actually due on ANY specific date for the Monthly Calendar
    fun getTasksDueOnSpecificDate(targetDate: LocalDate): List<CalendarTaskItem> {
        return plants.flatMap { plant ->
            val dueForPlant = mutableListOf<CalendarTaskItem>()

            CareTaskType.entries.forEach { taskType ->
                if (taskType == CareTaskType.WATER || taskType == CareTaskType.FEED || taskType == CareTaskType.ROTATE || taskType == CareTaskType.TRIM) {
                    val isDue = isTaskDueOnDate(plant, taskType, targetDate)
                    val isCompleted = isTaskCompletedInActiveCycle(plant, taskType, targetDate)

                    if (isDue || isCompleted) {
                        dueForPlant.add(
                            CalendarTaskItem(
                                id = "task_${taskType.name}_${plant.id}_${targetDate}",
                                plant = plant,
                                taskType = taskType,
                                taskName = "${taskType.name.lowercase().replaceFirstChar { it.uppercase() }} ${plant.nickname}",
                                location = plant.location,
                                dueDate = targetDate,
                                isCompleted = isCompleted
                            )
                        )
                    }
                }
            }
            dueForPlant
        }
    }

    val completedCount = todayTasks.count { it.isCompleted }
    val totalCount = todayTasks.size

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
                onNavTabSelected = onNavTabSelected,
                pendingTasksCount = pendingTasksCount
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-Header Title & Calendar Icon Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.width(32.dp))

                Text(
                    text = "Care & Calendar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CreamyOffWhite
                )

                IconButton(onClick = { showFullCalendarModal = true }) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Full Calendar",
                        tint = CreamyOffWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
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

            // Dynamic Care Task List OR Serene Empty Calendar State
            if (todayTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier.padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Spa,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Your Care Schedule is Empty",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "No care tasks scheduled. Add plants to your sanctuary to generate automated watering, nourishment, and pruning routines.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { onNavTabSelected(NavTab.ADD) },
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Plants to Sanctuary", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(todayTasks, key = { it.id }) { task ->
                        CalendarTaskCard(
                            task = task,
                            isCompleted = task.isCompleted,
                            onToggleCompletion = {
                                onTaskToggled(task.plant.id, task.taskType, !task.isCompleted)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))

                        // View Full Calendar Button
                        Button(
                            onClick = { showFullCalendarModal = true },
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Footer Links & Copyright
                        app.vercel.stillleaf.ui.component.StillLeafFooter(
                            onLegalRequested = onLegalRequested
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }

    // Full Monthly Calendar Modal Dialog
    if (showFullCalendarModal) {
        FullCalendarModal(
            today = today,
            getTasksForDate = { targetDate -> getTasksDueOnSpecificDate(targetDate) },
            onDismiss = { showFullCalendarModal = false }
        )
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
                        model = rememberImageModel(task.plant.primaryPhotoUrl),
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

@Composable
fun FullCalendarModal(
    today: LocalDate,
    getTasksForDate: (LocalDate) -> List<CalendarTaskItem>,
    onDismiss: () -> Unit
) {
    var currentYearMonth by remember { mutableStateOf(java.time.YearMonth.from(today)) }
    var selectedDay by remember { mutableIntStateOf(today.dayOfMonth) }

    val monthTitle = remember(currentYearMonth) {
        "${currentYearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentYearMonth.year}"
    }
    val daysInMonth = remember(currentYearMonth) { currentYearMonth.lengthOfMonth() }

    val selectedDate = remember(selectedDay, currentYearMonth) {
        try {
            val safeDay = selectedDay.coerceIn(1, currentYearMonth.lengthOfMonth())
            currentYearMonth.atDay(safeDay)
        } catch (_: Exception) {
            today
        }
    }

    val selectedDateTasks = remember(selectedDate) {
        getTasksForDate(selectedDate)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row with Month Navigation Arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                currentYearMonth = currentYearMonth.minusMonths(1)
                                selectedDay = 1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Previous Month",
                                tint = PrimarySage,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepForestAccent,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        IconButton(
                            onClick = {
                                currentYearMonth = currentYearMonth.plusMonths(1)
                                selectedDay = 1
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Next Month",
                                tint = PrimarySage,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Monthly 7-Column Day Grid (Days 1 to daysInMonth)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(daysInMonth) { index ->
                        val dayNum = index + 1
                        val dateForDay = try { currentYearMonth.atDay(dayNum) } catch (_: Exception) { today }
                        val tasksOnDay = getTasksForDate(dateForDay)

                        val isToday = dateForDay == today
                        val isSelected = dayNum == selectedDay
                        val hasTasks = tasksOnDay.isNotEmpty()

                        Surface(
                            onClick = { selectedDay = dayNum },
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isSelected -> PrimarySage
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = if (isToday && !isSelected) BorderStroke(1.dp, PrimarySage) else null,
                            modifier = Modifier.height(42.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) SurfaceWhite else DeepForestAccent
                                    )
                                    // Indicator dot renders ONLY if tasks are actually due on this date
                                    if (hasTasks) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) SurfaceWhite else PrimarySage)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Scheduled Tasks for Day $selectedDay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepForestAccent
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (selectedDateTasks.isEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (selectedDay == today.dayOfMonth) "All care tasks for today are completed! Next tasks due in future schedule." else "No care tasks scheduled on Day $selectedDay.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        selectedDateTasks.take(3).forEach { task ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Spa,
                                        contentDescription = null,
                                        tint = PrimarySage,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = task.taskName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepForestAccent
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimarySage)
                ) {
                    Text("Close Calendar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
