package app.vercel.stillleaf.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import app.vercel.stillleaf.data.model.CareTaskType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.PlantDiagnosis
import app.vercel.stillleaf.data.model.PlantPhoto
import app.vercel.stillleaf.data.model.SubscriptionStatus
import app.vercel.stillleaf.ui.component.AffiliateItem
import app.vercel.stillleaf.ui.component.ZenAffiliateCard
import app.vercel.stillleaf.ui.theme.AttentionTerracotta
import app.vercel.stillleaf.ui.theme.DeepForestAccent
import app.vercel.stillleaf.ui.theme.PillShape
import coil.compose.AsyncImage
import java.time.LocalDate

@Composable
fun PlantDetailScreen(
    plant: Plant,
    subscriptionStatus: SubscriptionStatus,
    onBackRequested: () -> Unit,
    onUpgradeRequested: () -> Unit,
    onEditPlantRequested: (Plant, String, String) -> Unit = { _, _, _ -> },
    onDeletePlantRequested: (Plant) -> Unit = {},
    onDiagnosisCreated: (Plant, PlantDiagnosis, Boolean) -> Unit = { _, _, _ -> },
    onTaskToggled: (plantId: String, taskType: CareTaskType, isCompleted: Boolean) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()
    val today = remember { LocalDate.now() }

    // Helper to get interval for a task type
    fun getIntervalForTask(taskType: CareTaskType): Int {
        return when (taskType) {
            CareTaskType.WATER -> plant.careRequirements.waterIntervalDays
            CareTaskType.FEED -> plant.careRequirements.feedIntervalDays
            CareTaskType.ROTATE -> plant.careRequirements.rotateIntervalDays
            CareTaskType.TRIM -> plant.careRequirements.trimIntervalDays
            else -> 14
        }
    }

    // Helper checking if task is within active completed care cycle
    fun isTaskCompletedInActiveCycle(taskType: CareTaskType): Boolean {
        val interval = getIntervalForTask(taskType)
        val latestLogDate = plant.recentLogs
            .filter { it.taskType == taskType }
            .maxByOrNull { it.completedAt }
            ?.completedAt
            ?.toLocalDate() ?: return false

        val daysSinceLog = java.time.temporal.ChronoUnit.DAYS.between(latestLogDate, today)
        return daysSinceLog in 0 until interval
    }

    var selectedComparisonPhoto by remember { mutableStateOf<PlantPhoto?>(null) }

    // Dialog & Modal states for Environment & Care Profile
    var showLightModal by remember { mutableStateOf(false) }
    var showWaterModal by remember { mutableStateOf(false) }
    var showNourishModal by remember { mutableStateOf(false) }
    var showHumidityModal by remember { mutableStateOf(false) }

    var showDiagnoseModal by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    // Filter photos specifically for Quarterly Growth Journal milestones (Day 0 Whole Plant, Day 90, etc.)
    val milestonePhotos = remember(plant.photos) {
        plant.photos.filter { it.isQuarterlyMilestone }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        // 1. Plant Image Header Banner (9:16 Vertical Portrait Format)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            AsyncImage(
                model = plant.primaryPhotoUrl,
                contentDescription = plant.nickname,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim overlay for legibility
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            ),
                            startY = 0f
                        )
                    )
            )

            // Back button overlay
            IconButton(
                onClick = onBackRequested,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp)
                    .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Sanctuary",
                    tint = Color.White
                )
            }

            // Title & Badges on top of image
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${plant.healthStatusText} • ${plant.location}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = plant.nickname,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White
                )

                Text(
                    text = "${plant.botanicalName} (${plant.commonName})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Environment Care Chips - 2 Cards Wide Stacked (2x2 Grid)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Environment & Care Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Row 1: Light & Water (Clickable Modals)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CareChip(
                    icon = Icons.Default.LightMode,
                    label = "Light",
                    value = "${plant.careRequirements.sunlight} (Tap Info)",
                    modifier = Modifier.weight(1f),
                    onClick = { showLightModal = true }
                )
                CareChip(
                    icon = Icons.Default.WaterDrop,
                    label = "Water",
                    value = "Every ${plant.careRequirements.waterIntervalDays}d (Tap Info)",
                    modifier = Modifier.weight(1f),
                    onClick = { showWaterModal = true }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Nourish & Humidity (Clickable Modals)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CareChip(
                    icon = Icons.Default.Spa,
                    label = "Nourish",
                    value = "Every ${plant.careRequirements.feedIntervalDays}d (Tap Info)",
                    modifier = Modifier.weight(1f),
                    onClick = { showNourishModal = true }
                )
                CareChip(
                    icon = Icons.Default.Opacity,
                    label = "Humidity",
                    value = "${plant.careRequirements.idealHumidity} (Tap Info)",
                    modifier = Modifier.weight(1f),
                    onClick = { showHumidityModal = true }
                )
            }

            // Guidance Summary
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "“${plant.careRequirements.careInsight}”",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Interactive Task Action Cards (Connected to Unified CareLog Handler)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Care Routine & Tasks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            CareTaskActionCard(
                taskType = CareTaskType.WATER,
                icon = Icons.Default.WaterDrop,
                title = "Water Plant",
                subtitle = "Keep topsoil evenly moist",
                isCompleted = isTaskCompletedInActiveCycle(CareTaskType.WATER),
                onCompleteToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTaskToggled(plant.id, CareTaskType.WATER, !isTaskCompletedInActiveCycle(CareTaskType.WATER))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CareTaskActionCard(
                taskType = CareTaskType.FEED,
                icon = Icons.Default.Spa,
                title = "Nourish & Feed",
                subtitle = "Apply diluted organic fertilizer (Tap for info)",
                isCompleted = isTaskCompletedInActiveCycle(CareTaskType.FEED),
                onCompleteToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTaskToggled(plant.id, CareTaskType.FEED, !isTaskCompletedInActiveCycle(CareTaskType.FEED))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CareTaskActionCard(
                taskType = CareTaskType.ROTATE,
                icon = Icons.Default.Refresh,
                title = "Rotate Pot 90°",
                subtitle = "Promote balanced leaf growth",
                isCompleted = isTaskCompletedInActiveCycle(CareTaskType.ROTATE),
                onCompleteToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTaskToggled(plant.id, CareTaskType.ROTATE, !isTaskCompletedInActiveCycle(CareTaskType.ROTATE))
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            CareTaskActionCard(
                taskType = CareTaskType.TRIM,
                icon = Icons.Default.ContentCut,
                title = "Prune & Clean Leaves",
                subtitle = "Remove yellowing foliage",
                isCompleted = isTaskCompletedInActiveCycle(CareTaskType.TRIM),
                onCompleteToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onTaskToggled(plant.id, CareTaskType.TRIM, !isTaskCompletedInActiveCycle(CareTaskType.TRIM))
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botanical Care & Essentials Card Section
        val soilAffiliateItem = remember(plant) {
            val name = plant.commonName.lowercase()
            if (name.contains("succulent") || name.contains("cactus") || name.contains("snake")) {
                AffiliateItem(
                    title = "Organic Desert Succulent & Cactus Potting Soil",
                    brand = "FoxFarm Ocean Forest",
                    recommendationNote = "Fast-draining volcanic pumice & perlite blend ideal for ${plant.nickname} to prevent root moisture rot.",
                    affiliateUrl = "https://www.amazon.com/s?k=organic+succulent+cactus+potting+soil+mix&tag=stillleaf-20",
                    categoryLabel = "Essential Potting Soil"
                )
            } else {
                AffiliateItem(
                    title = "Chunky Premium Aroid & Tropical Potting Bark Blend",
                    brand = "Sustee Botanical Co.",
                    recommendationNote = "Aerated potting bark & sphagnum moss mix that balances root air flow and hydration for ${plant.nickname}.",
                    affiliateUrl = "https://www.amazon.com/s?k=organic+chunky+aroid+potting+bark+mix&tag=stillleaf-20",
                    categoryLabel = "Essential Potting Soil"
                )
            }
        }

        val nutrientAffiliateItem = remember(plant) {
            AffiliateItem(
                title = "Organic Gentle Liquid Plant Food & Nutrient Concentrate",
                brand = "Miracle-Gro Organic",
                recommendationNote = "Provides essential nitrogen and micronutrients every ${plant.careRequirements.feedIntervalDays} days for vibrant leaf and bloom growth.",
                affiliateUrl = "https://www.amazon.com/s?k=organic+liquid+plant+food+fertilizer&tag=stillleaf-20",
                categoryLabel = "Essential Fertilizer & Nutrients"
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Botanical Care Essentials",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            ZenAffiliateCard(item = soilAffiliateItem)
            ZenAffiliateCard(item = nutrientAffiliateItem)
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 4. 3-Month Visual Growth Gallery (Filtering specifically for Whole Plant Quarterly Milestones)
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quarterly Growth Journal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Compare visual progression over time",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(milestonePhotos) { photo ->
                    GrowthMilestoneCard(
                        photo = photo,
                        onClick = {
                            selectedComparisonPhoto = photo
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 5. Botanical Health & Diagnostics Section
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Botanical Health & Diagnostics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Active Saved Diagnosis Card
            plant.activeDiagnosis?.let { diagnosis ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = AttentionTerracotta.copy(alpha = 0.12f)),
                    border = BorderStroke(1.dp, AttentionTerracotta.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MedicalServices,
                                    contentDescription = null,
                                    tint = AttentionTerracotta,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = diagnosis.diagnosisTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                color = AttentionTerracotta,
                                shape = PillShape
                            ) {
                                Text(
                                    text = diagnosis.severityLevel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = diagnosis.rootCauseSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showDiagnoseModal = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = AttentionTerracotta)
                        ) {
                            Text("View Treatment Cure & Re-Diagnose", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Diagnose Button
            Button(
                onClick = { showDiagnoseModal = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Diagnose Plant Health",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 6. Edit Plant & Delete Plant Buttons Row
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Plant Management",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Edit Plant Button
                Button(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Plant", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Delete Plant Button
                OutlinedButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = PillShape,
                    border = BorderStroke(1.dp, AttentionTerracotta),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AttentionTerracotta)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Delete Plant", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

    // Modal 1: Light Details Dialog
    if (showLightModal) {
        LightDetailsDialog(
            plant = plant,
            onDismiss = { showLightModal = false }
        )
    }

    // Modal 2: Water Details Dialog
    if (showWaterModal) {
        WaterDetailsDialog(
            plant = plant,
            onDismiss = { showWaterModal = false }
        )
    }

    // Modal 3: Nourishment Requirements Popup Dialog
    if (showNourishModal) {
        NourishDetailsDialog(
            plant = plant,
            onDismiss = { showNourishModal = false }
        )
    }

    // Modal 4: Humidity Details Dialog
    if (showHumidityModal) {
        HumidityDetailsDialog(
            plant = plant,
            onDismiss = { showHumidityModal = false }
        )
    }

    // Modal 5: Botanical Health Diagnostic Dialog
    if (showDiagnoseModal) {
        DiagnosePlantDialog(
            plant = plant,
            onDismiss = { showDiagnoseModal = false },
            onDiagnosisComplete = { diagnosisResult, scheduleOnCalendar ->
                showDiagnoseModal = false
                onDiagnosisCreated(plant, diagnosisResult, scheduleOnCalendar)
            }
        )
    }

    // Modal 6: Edit Plant Dialog
    if (showEditDialog) {
        EditPlantDialog(
            plant = plant,
            onDismiss = { showEditDialog = false },
            onSave = { updatedNickname, updatedLocation ->
                showEditDialog = false
                onEditPlantRequested(plant, updatedNickname, updatedLocation)
            }
        )
    }

    // Modal 7: Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AttentionTerracotta,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Delete Plant?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove “${plant.nickname}” from your Sanctuary? This will also delete all care logs and growth photos.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDeletePlantRequested(plant)
                        onBackRequested()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AttentionTerracotta)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Side-by-Side Milestone Comparison Modal
    val activeModalPhoto = selectedComparisonPhoto
    if (activeModalPhoto != null) {
        val initialPhoto = plant.photos.firstOrNull() ?: activeModalPhoto
        MilestoneComparisonDialog(
            initialPhoto = initialPhoto,
            milestonePhoto = activeModalPhoto,
            botanicalName = plant.nickname,
            onDismiss = { selectedComparisonPhoto = null }
        )
    }
}

@Composable
fun LightDetailsDialog(
    plant: Plant,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LightMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lighting Requirements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${plant.nickname} (${plant.botanicalName})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = PillShape
                ) {
                    Text(
                        text = plant.careRequirements.sunlight,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val lightExplanation = when {
                    plant.careRequirements.sunlight.contains("Bright", ignoreCase = true) ->
                        "${plant.nickname} thrives best in Bright Indirect Sunlight. Place it near an East or South-facing window where it receives abundant ambient daylight without direct scorching sun rays."
                    plant.careRequirements.sunlight.contains("Full", ignoreCase = true) ->
                        "${plant.nickname} requires Full Direct Sunlight (6+ hours daily). Place it directly on a sunny windowsill, outdoor patio, or garden spot."
                    else ->
                        "${plant.nickname} prefers low to moderate ambient daylight. Keep it away from direct harsh sun to protect its foliage."
                }

                Text(
                    text = lightExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/s?k=full+spectrum+led+grow+lights+for+indoor+plants&tag=stillleaf-20"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = PillShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "View LED Grow Light",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WaterDetailsDialog(
    plant: Plant,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Watering Requirements",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${plant.nickname} (${plant.botanicalName})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = PillShape
                ) {
                    Text(
                        text = "Every ${plant.careRequirements.waterIntervalDays} Days",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val waterExplanation = "${plant.nickname} should be watered approximately every ${plant.careRequirements.waterIntervalDays} days. Always test the soil first: water thoroughly when the top 1-2 inches feel dry to the touch, and ensure excess water drains completely from the pot bottom."

                Text(
                    text = waterExplanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/s?k=cool+mist+humidifier+for+indoor+plants&tag=stillleaf-20"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = PillShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "View Soil Moisture Meter",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun NourishDetailsDialog(
    plant: Plant,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Nourishment & Feeding",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${plant.nickname} (${plant.botanicalName})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = PillShape
                ) {
                    Text(
                        text = "Every ${plant.careRequirements.feedIntervalDays} Days",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Nourish ${plant.nickname} with diluted organic plant food every ${plant.careRequirements.feedIntervalDays} days during active spring and summer growth seasons. Reduce or pause feeding during winter dormancy.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/s?k=organic+liquid+plant+food+fertilizer&tag=stillleaf-20"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = PillShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "View Organic Nutrients",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun HumidityDetailsDialog(
    plant: Plant,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Opacity,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Humidity Goals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${plant.nickname} (${plant.botanicalName})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = PillShape
                ) {
                    Text(
                        text = plant.careRequirements.idealHumidity,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${plant.nickname} thrives best in an ideal humidity level of ${plant.careRequirements.idealHumidity}. If indoor ambient air is dry during winter heating, group plants together or place on a moist pebble tray.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.com/s?k=cool+mist+humidifier+for+indoor+plants&tag=stillleaf-20"))
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    shape = PillShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "View Plant Humidifier",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CareChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CareTaskActionCard(
    taskType: CareTaskType,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    onCompleteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCompleteToggle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (isCompleted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
                    .background(if (isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GrowthMilestoneCard(
    photo: PlantPhoto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(180.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = photo.photoUrl,
                contentDescription = photo.milestoneTitle ?: "Growth photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 60f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = "Day ${photo.ageInDays}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = photo.milestoneTitle ?: "Check-in",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MilestoneComparisonDialog(
    initialPhoto: PlantPhoto,
    milestonePhoto: PlantPhoto,
    botanicalName: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Growth Comparison",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = botanicalName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.medium)
                    ) {
                        AsyncImage(
                            model = initialPhoto.photoUrl,
                            contentDescription = "Baseline Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "Day ${initialPhoto.ageInDays} (Start)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(MaterialTheme.shapes.medium)
                    ) {
                        AsyncImage(
                            model = milestonePhoto.photoUrl,
                            contentDescription = "Milestone Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.6f))
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "Day ${milestonePhoto.ageInDays} (Milestone)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = milestonePhoto.note ?: "Visual comparison shows foliage expansion and stem node development over ${milestonePhoto.ageInDays - initialPhoto.ageInDays} days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Close Comparison")
                }
            }
        }
    }
}

@Composable
fun DiagnosePlantDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onDiagnosisComplete: (diagnosis: PlantDiagnosis, scheduleOnCalendar: Boolean) -> Unit
) {
    var issueInput by remember { mutableStateOf("") }
    var isAnalyzing by remember { mutableStateOf(false) }
    var diagnosisResult by remember { mutableStateOf<PlantDiagnosis?>(null) }
    var scheduleOnCalendar by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Botanical Health Diagnostic",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${plant.nickname} (${plant.botanicalName})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (diagnosisResult == null) {
                    Text(
                        text = "Describe the symptoms or ailment:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = issueInput,
                        onValueChange = { issueInput = it },
                        placeholder = {
                            Text(
                                text = "What is your concern? (e.g. yellowing lower leaves, brown tips, or wilting...)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isAnalyzing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Analyzing Symptoms & Foliage...", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                isAnalyzing = true
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    isAnalyzing = false
                                    diagnosisResult = PlantDiagnosis(
                                        id = "diag_${System.currentTimeMillis()}",
                                        issueDescription = issueInput,
                                        diagnosisTitle = "Moisture Imbalance & Root Stress",
                                        confidenceScore = 94,
                                        severityLevel = "Moderate",
                                        rootCauseSummary = "Lower leaf chlorosis and tip necrosis caused by soil moisture retention at root level.",
                                        cureSteps = listOf(
                                            "1. Allow top 2 inches of soil to dry completely before next watering.",
                                            "2. Prune yellowed foliage with sterile shears at a 45° angle.",
                                            "3. Apply organic neem oil liquid spray to undersides of remaining leaves."
                                        ),
                                        specialTreatmentTaskName = "Special Care: Neem Oil & Moisture Recovery",
                                        diagnosedAt = "Today"
                                    )
                                }, 1500)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Analyze & Diagnose Plant Health", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    val result = diagnosisResult!!

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = AttentionTerracotta.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, AttentionTerracotta.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = result.diagnosisTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    color = AttentionTerracotta,
                                    shape = PillShape
                                ) {
                                    Text(
                                        text = "${result.confidenceScore}% Certainty",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = result.rootCauseSummary,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Treatment Protocol:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = DeepForestAccent
                            )

                            result.cureSteps.forEach { step ->
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onDiagnosisComplete(result, scheduleOnCalendar)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Save Diagnosis to Plant Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditPlantDialog(
    plant: Plant,
    onDismiss: () -> Unit,
    onSave: (updatedNickname: String, updatedLocation: String) -> Unit
) {
    var nicknameInput by remember { mutableStateOf(plant.nickname) }

    val initialIsOutdoor = remember(plant.location) { plant.location.contains("Outdoor", ignoreCase = true) }
    val initialDetail = remember(plant.location) {
        plant.location
            .replace("Indoor", "")
            .replace("Outdoor", "")
            .replace("•", "")
            .replace("-", "")
            .trim()
    }

    var isOutdoorEnv by remember { mutableStateOf(initialIsOutdoor) }
    var locationDetailInput by remember { mutableStateOf(initialDetail) }
    var locationError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Edit Plant Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    label = { Text("Plant Nickname") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Indoor / Outdoor Toggle Segmented Selector
                Text(
                    text = "Environment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepForestAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = { isOutdoorEnv = false },
                            shape = PillShape,
                            color = if (!isOutdoorEnv) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Indoor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (!isOutdoorEnv) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }

                        Surface(
                            onClick = { isOutdoorEnv = true },
                            shape = PillShape,
                            color = if (isOutdoorEnv) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Outdoor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isOutdoorEnv) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location Error Callout
                if (locationError != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = locationError!!,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Specific Location Detail Field (Required)
                OutlinedTextField(
                    value = locationDetailInput,
                    onValueChange = {
                        locationDetailInput = it
                        locationError = null
                    },
                    label = { Text("Specific Location (Required)") },
                    placeholder = { Text("e.g. Living Room, Patio, Office, Bedroom...") },
                    isError = locationError != null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val trimmedDetail = locationDetailInput.trim()
                        if (trimmedDetail.isBlank()) {
                            locationError = "Location detail is required. Please specify where your plant is placed (e.g. Living Room, Patio, Office)."
                            return@Button
                        }

                        val envType = if (isOutdoorEnv) "Outdoor" else "Indoor"
                        val formattedLocation = "$envType • $trimmedDetail"

                        onSave(nicknameInput.ifBlank { plant.nickname }.trim(), formattedLocation)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
