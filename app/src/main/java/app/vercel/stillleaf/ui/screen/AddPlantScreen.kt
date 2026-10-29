package app.vercel.stillleaf.ui.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.ZoomIn
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.vercel.stillleaf.R
import app.vercel.stillleaf.data.api.AiIdentificationResult
import app.vercel.stillleaf.data.api.OpenAiVisionService
import app.vercel.stillleaf.data.model.HealthStatusType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.PlantPhoto
import app.vercel.stillleaf.ui.component.StillLeafTopBar
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import app.vercel.stillleaf.ui.theme.DeepForestAccent
import app.vercel.stillleaf.ui.theme.PillShape
import app.vercel.stillleaf.ui.theme.PrimarySage
import app.vercel.stillleaf.ui.theme.SurfaceWhite
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

fun saveBitmapToCacheUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val file = File(context.cacheDir, "captured_plant_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

@Composable
fun AddPlantScreen(
    onBackRequested: () -> Unit,
    onPlantAdded: (Plant) -> Unit,
    onNavTabSelected: (NavTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isAnalyzing by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf<AiIdentificationResult?>(null) }

    // Photo 1: Close-up AI Identification Photo
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Photo 2: Day 0 Whole Plant Growth Snapshot Photo
    var wholePlantImageUri by remember { mutableStateOf<Uri?>(null) }
    var wholePlantCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var nicknameInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("Indoor • Living Room") }

    val defaultSamplePhoto = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80"
    val scrollState = rememberScrollState()

    // Helper to trigger OpenAI Vision API Analysis
    fun analyzeImage(uri: Uri? = null, bitmap: Bitmap? = null) {
        coroutineScope.launch {
            isAnalyzing = true
            aiResult = null

            val result = OpenAiVisionService.identifyPlant(context, uri, bitmap)
            aiResult = result
            nicknameInput = result.commonName
            isAnalyzing = false
        }
    }

    // 1. Gallery Photo Picker Launcher for Close-Up Detail Photo
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmap = null
            analyzeImage(uri = uri)
        }
    }

    // 2. Take Photo Camera Launcher for Close-Up Detail Photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            capturedBitmap = bitmap
            val cacheUri = saveBitmapToCacheUri(context, bitmap)
            if (cacheUri != null) {
                selectedImageUri = cacheUri
            }
            analyzeImage(bitmap = bitmap)
        }
    }

    // 3. Whole Plant Gallery Launcher
    val wholePlantGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            wholePlantImageUri = uri
            wholePlantCapturedBitmap = null
        }
    }

    // 4. Whole Plant Camera Launcher
    val wholePlantCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            wholePlantCapturedBitmap = bitmap
            val cacheUri = saveBitmapToCacheUri(context, bitmap)
            if (cacheUri != null) {
                wholePlantImageUri = cacheUri
            }
        }
    }

    // 5. Camera Permission Launcher to prevent crashes
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Opening camera...", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is required to take plant photos.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Background Image (bg_add)
        Image(
            painter = painterResource(R.drawable.bg_add),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Screen Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Unified Top Logo & Navbar
            StillLeafTopBar(
                currentTab = NavTab.ADD,
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
                    text = "Identify & Add Plant",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CreamyOffWhite
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 9:16 Vertical Portrait Camera Preview Frame
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp) // 9:16 vertical format
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceWhite.copy(alpha = 0.92f))
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        capturedBitmap != null -> {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Captured Camera Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        selectedImageUri != null -> {
                            AsyncImage(
                                model = selectedImageUri,
                                contentDescription = "Selected Gallery Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    tint = PrimarySage,
                                    modifier = Modifier.size(52.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Position leaf inside frame",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForestAccent
                                )
                                Text(
                                    text = "9:16 Portrait Capture Format",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons Row: "Take Photo" & "Gallery"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val hasCameraPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCameraPermission) {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Opening camera...", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Take Photo", fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceWhite.copy(alpha = 0.92f))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gallery", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Initial Best Practices Guidance Card (Before Photo Selected)
                if (aiResult == null && !isAnalyzing) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Spa,
                                    contentDescription = null,
                                    tint = PrimarySage,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Photo Best Practices for 99% AI Accuracy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForestAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Nailing your first close-up photo ensures the exact species and initial care baseline. (Quarterly growth photos can be taken at any angle or lighting!)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            BestPracticeRow(
                                icon = Icons.Default.ZoomIn,
                                title = "Focus on Leaf & Flower Detail",
                                description = "Center a single clear leaf margin, vein pattern, or flower bloom in the frame."
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            BestPracticeRow(
                                icon = Icons.Default.LightMode,
                                title = "Natural Indirect Lighting",
                                description = "Ensure bright ambient daylight—avoid heavy shadows or dark indoor rooms."
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            BestPracticeRow(
                                icon = Icons.Default.PhotoCamera,
                                title = "Isolate Single Plant",
                                description = "Keep background foliage or secondary houseplants out of the camera view."
                            )
                        }
                    }
                }

                // AI Loading Indicator
                if (isAnalyzing) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Analyzing leaf structure...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Identifying botanical features...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // AI Identification Results Card
                val result = aiResult
                if (result != null && !isAnalyzing) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = PillShape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${result.confidenceScore}% Confidence",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = result.commonName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = result.botanicalName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Day 0 Whole Plant Growth Snapshot Section (Recommended)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = null,
                                            tint = PrimarySage,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Day 0 Whole Plant Photo (Optional)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepForestAccent
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Take a full shot of your plant in its pot to start your 90-Day Growth Journal timeline baseline.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    if (wholePlantImageUri != null || wholePlantCapturedBitmap != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        ) {
                                            if (wholePlantCapturedBitmap != null) {
                                                Image(
                                                    bitmap = wholePlantCapturedBitmap!!.asImageBitmap(),
                                                    contentDescription = "Whole Plant Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                AsyncImage(
                                                    model = wholePlantImageUri,
                                                    contentDescription = "Whole Plant Photo",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { wholePlantCameraLauncher.launch(null) },
                                                modifier = Modifier.weight(1f),
                                                shape = PillShape,
                                                border = BorderStroke(1.dp, PrimarySage)
                                            ) {
                                                Text("Take Whole Shot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimarySage)
                                            }

                                            OutlinedButton(
                                                onClick = { wholePlantGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                                modifier = Modifier.weight(1f),
                                                shape = PillShape,
                                                border = BorderStroke(1.dp, PrimarySage)
                                            ) {
                                                Text("Pick Whole Shot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimarySage)
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Custom Name & Location Input Fields
                            OutlinedTextField(
                                value = nicknameInput,
                                onValueChange = { nicknameInput = it },
                                label = { Text("Plant Nickname") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = locationInput,
                                onValueChange = { locationInput = it },
                                label = { Text("Location (e.g. Indoor • Living Room)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Save Button
                            Button(
                                onClick = {
                                    val plantId = "plant_${System.currentTimeMillis()}"

                                    // Close-up detail photo URL
                                    val detailPhotoUrl = selectedImageUri?.toString()
                                        ?: (if (capturedBitmap != null) saveBitmapToCacheUri(context, capturedBitmap!!)?.toString() else null)
                                        ?: defaultSamplePhoto

                                    // Day 0 Whole plant photo URL (or detail photo if whole plant shot omitted)
                                    val wholePlantUrl = wholePlantImageUri?.toString()
                                        ?: (if (wholePlantCapturedBitmap != null) saveBitmapToCacheUri(context, wholePlantCapturedBitmap!!)?.toString() else null)
                                        ?: detailPhotoUrl

                                    val detailPhotoObj = PlantPhoto(
                                        id = "photo_detail_${System.currentTimeMillis()}",
                                        plantId = plantId,
                                        photoUrl = detailPhotoUrl,
                                        dateTaken = LocalDate.now(),
                                        ageInDays = 0,
                                        note = "Day 0 - AI Detail Identification Shot",
                                        isQuarterlyMilestone = false
                                    )

                                    val wholePlantMilestonePhotoObj = PlantPhoto(
                                        id = "photo_whole_${System.currentTimeMillis() + 1}",
                                        plantId = plantId,
                                        photoUrl = wholePlantUrl,
                                        dateTaken = LocalDate.now(),
                                        ageInDays = 0,
                                        note = "Day 0 - Baseline Whole Plant Snapshot",
                                        isQuarterlyMilestone = true,
                                        milestoneTitle = "Day 0 - Baseline"
                                    )

                                    val photosList = if (wholePlantUrl != detailPhotoUrl) {
                                        listOf(wholePlantMilestonePhotoObj, detailPhotoObj)
                                    } else {
                                        listOf(wholePlantMilestonePhotoObj)
                                    }

                                    val newPlant = Plant(
                                        id = plantId,
                                        nickname = nicknameInput.ifBlank { result.commonName },
                                        botanicalName = result.botanicalName,
                                        commonName = result.commonName,
                                        primaryPhotoUrl = wholePlantUrl,
                                        dateAdded = LocalDate.now(),
                                        careRequirements = result.careRequirements,
                                        location = locationInput.ifBlank { "Indoor • Living Room" },
                                        healthStatusType = HealthStatusType.HEALTHY,
                                        healthStatusText = "Healthy",
                                        photos = photosList
                                    )
                                    onPlantAdded(newPlant)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Save to Sanctuary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun BestPracticeRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = PrimarySage.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimarySage,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = DeepForestAccent
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
