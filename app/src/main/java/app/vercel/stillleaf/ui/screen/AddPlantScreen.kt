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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.File
import java.time.LocalDate

fun saveBitmapToCacheUri(context: Context, bitmap: Bitmap): Uri? {
    return try {
        val photosDir = File(context.filesDir, "plant_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val portraitBitmap = if (bitmap.width > bitmap.height) {
            val matrix = android.graphics.Matrix().apply { postRotate(90f) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }

        val file = File(photosDir, "captured_plant_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            portraitBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        null
    }
}

fun savePersistentPhotoUrl(context: Context, uri: Uri?, bitmap: Bitmap?): String {
    try {
        val photosDir = File(context.filesDir, "plant_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val destFile = File(photosDir, "plant_photo_${System.currentTimeMillis()}_${(1000..9999).random()}.jpg")

        val srcBitmap: Bitmap? = when {
            bitmap != null -> {
                if (bitmap.width > bitmap.height) {
                    val matrix = android.graphics.Matrix().apply { postRotate(90f) }
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else {
                    bitmap
                }
            }
            uri != null -> {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return uri.toString()
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

                val maxDim = 1024
                var inSampleSize = 1
                if (options.outHeight > maxDim || options.outWidth > maxDim) {
                    val halfHeight = options.outHeight / 2
                    val halfWidth = options.outWidth / 2
                    while (halfHeight / inSampleSize >= maxDim && halfWidth / inSampleSize >= maxDim) {
                        inSampleSize *= 2
                    }
                }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { this.inSampleSize = inSampleSize })
            }
            else -> null
        }

        if (srcBitmap != null) {
            val maxDim = 1024
            val width = srcBitmap.width
            val height = srcBitmap.height
            val scale = (maxDim.toFloat() / Math.max(width, height)).coerceAtMost(1.0f)
            val scaledBitmap = if (scale < 1.0f) {
                Bitmap.createScaledBitmap(srcBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                srcBitmap
            }

            destFile.outputStream().use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            return Uri.fromFile(destFile).toString()
        }

        return uri?.toString() ?: ""
    } catch (e: Exception) {
        return uri?.toString() ?: ""
    }
}

@Composable
fun AddPlantScreen(
    onBackRequested: () -> Unit,
    onPlantAdded: (Plant) -> Unit,
    onNavTabSelected: (NavTab) -> Unit = {},
    onLegalRequested: (app.vercel.stillleaf.ui.component.LegalTab) -> Unit = {},
    pendingTasksCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isAnalyzing by remember { mutableStateOf(false) }
    var aiResult by remember { mutableStateOf<AiIdentificationResult?>(null) }

    // Photo 1: Close-up Leaf/Bloom Detail Photo
    var detailImageUri by remember { mutableStateOf<Uri?>(null) }
    var detailCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Photo 2: Whole Plant Overview Photo
    var wholePlantImageUri by remember { mutableStateOf<Uri?>(null) }
    var wholePlantCapturedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var activeCaptureTarget by remember { mutableStateOf<Int>(1) } // 1 = Detail, 2 = Whole Plant

    var nicknameInput by remember { mutableStateOf("") }

    // Indoor / Outdoor Toggle & Empty Location Detail Field
    var isOutdoorEnv by remember { mutableStateOf(false) } // false = Indoor, true = Outdoor
    var locationDetailInput by remember { mutableStateOf("") } // MUST BE COMPLETED
    var locationError by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    // Automatically trigger Botanical Identification whenever BOTH photos are captured
    LaunchedEffect(detailImageUri, wholePlantImageUri, detailCapturedBitmap, wholePlantCapturedBitmap) {
        val hasPhoto1 = detailImageUri != null || detailCapturedBitmap != null
        val hasPhoto2 = wholePlantImageUri != null || wholePlantCapturedBitmap != null

        if (hasPhoto1 && hasPhoto2) {
            aiResult = null
            isAnalyzing = true

            val result = OpenAiVisionService.identifyPlantWithDualPhotos(
                context = context,
                closeUpUri = detailImageUri,
                closeUpBitmap = detailCapturedBitmap,
                wholePlantUri = wholePlantImageUri,
                wholePlantBitmap = wholePlantCapturedBitmap
            )
            aiResult = result
            nicknameInput = result.commonName
            isAnalyzing = false
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val cacheUri = saveBitmapToCacheUri(context, bitmap)
            if (activeCaptureTarget == 1) {
                detailCapturedBitmap = bitmap
                detailImageUri = cacheUri
            } else {
                wholePlantCapturedBitmap = bitmap
                wholePlantImageUri = cacheUri
            }
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            if (activeCaptureTarget == 1) {
                detailImageUri = uri
                detailCapturedBitmap = null
            } else {
                wholePlantImageUri = uri
                wholePlantCapturedBitmap = null
            }
        }
    }

    // Camera Permission Launcher
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

    fun startCameraForTarget(target: Int) {
        activeCaptureTarget = target
        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Opening camera...", Toast.LENGTH_SHORT).show()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun startGalleryForTarget(target: Int) {
        activeCaptureTarget = target
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.bg_add),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            StillLeafTopBar(
                currentTab = NavTab.ADD,
                onNavTabSelected = onNavTabSelected,
                pendingTasksCount = pendingTasksCount
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

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // 2-Photo Dual Capture Container (Photo 1 Close-Up + Photo 2 Whole Plant)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "2-Step Photo Capture (Required for Precision)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepForestAccent
                        )
                        Text(
                            text = "Combining close-up leaf structure with full plant overview guarantees maximum identification precision.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Photo 1: Close-Up Detail Card
                        CaptureSlotCard(
                            stepNumber = "1",
                            title = "Photo 1: Close-Up Detail Shot",
                            description = "Center a single leaf margin, vein pattern, or bloom.",
                            uri = detailImageUri,
                            bitmap = detailCapturedBitmap,
                            onTakeCamera = { startCameraForTarget(1) },
                            onPickGallery = { startGalleryForTarget(1) }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Photo 2: Whole Plant Overview Card
                        CaptureSlotCard(
                            stepNumber = "2",
                            title = "Photo 2: Whole Plant Snapshot",
                            description = "Full shot of the plant in its pot (Sets Day 0 Growth Timeline).",
                            uri = wholePlantImageUri,
                            bitmap = wholePlantCapturedBitmap,
                            onTakeCamera = { startCameraForTarget(2) },
                            onPickGallery = { startGalleryForTarget(2) }
                        )
                    }
                }

                // Initial Guidance Best Practices (Before both photos captured)
                if (aiResult == null && !isAnalyzing) {
                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                                    text = "Photo Best Practices for 99% Accuracy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForestAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            BestPracticeRow(
                                icon = Icons.Default.ZoomIn,
                                title = "Focus on Leaf & Flower Detail",
                                description = "Center a single clear leaf margin, vein pattern, or flower bloom in Photo 1."
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            BestPracticeRow(
                                icon = Icons.Default.LightMode,
                                title = "Natural Ambient Lighting",
                                description = "Ensure bright ambient daylight—avoid heavy shadows or dark indoor rooms."
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            BestPracticeRow(
                                icon = Icons.Default.PhotoCamera,
                                title = "Isolate Single Plant",
                                description = "Keep background foliage out of view for instant botanical precision."
                            )
                        }
                    }
                }

                // Analyzing Loading State
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
                                    text = "Analyzing dual-photo botanical structure...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepForestAccent
                                )
                                Text(
                                    text = "Comparing leaf veins & growth habit...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Botanical Results Card
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

                                Text(
                                    text = "Botanical Verification",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
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

                            Spacer(modifier = Modifier.height(16.dp))

                            // Indoor / Outdoor Toggle Pill Selector
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
                                            color = if (!isOutdoorEnv) SurfaceWhite else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                            color = if (isOutdoorEnv) SurfaceWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 10.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Error Callout if location detail is missing
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

                            // Optional Notes Text Field
                            OutlinedTextField(
                                value = locationDetailInput,
                                onValueChange = {
                                    locationDetailInput = it
                                },
                                label = { Text("Notes (Optional)") },
                                placeholder = { Text("e.g. Care notes, potting date, light preferences...") },
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
                                    val trimmedNotes = locationDetailInput.trim()
                                    val envType = if (isOutdoorEnv) "Outdoor" else "Indoor"
                                    val formattedLocation = if (trimmedNotes.isNotBlank()) "$envType • $trimmedNotes" else envType

                                    val plantId = "plant_${System.currentTimeMillis()}"

                                    val closeUpUrl = savePersistentPhotoUrl(context, detailImageUri, detailCapturedBitmap)
                                    val wholePlantUrl = savePersistentPhotoUrl(context, wholePlantImageUri, wholePlantCapturedBitmap).ifBlank { closeUpUrl }

                                    val detailPhotoObj = PlantPhoto(
                                        id = "photo_detail_${System.currentTimeMillis()}",
                                        plantId = plantId,
                                        photoUrl = closeUpUrl,
                                        dateTaken = LocalDate.now(),
                                        ageInDays = 0,
                                        note = "Day 0 - Close-Up Identification Detail",
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

                                    val newPlant = Plant(
                                        id = plantId,
                                        nickname = nicknameInput.ifBlank { result.commonName },
                                        botanicalName = result.botanicalName,
                                        commonName = result.commonName,
                                        primaryPhotoUrl = wholePlantUrl,
                                        dateAdded = LocalDate.now(),
                                        careRequirements = result.careRequirements,
                                        location = formattedLocation,
                                        healthStatusType = HealthStatusType.HEALTHY,
                                        healthStatusText = "Healthy",
                                        photos = listOf(wholePlantMilestonePhotoObj, detailPhotoObj)
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

                // Footer Links & Copyright
                app.vercel.stillleaf.ui.component.StillLeafFooter(
                    onLegalRequested = onLegalRequested
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun CaptureSlotCard(
    stepNumber: String,
    title: String,
    description: String,
    uri: Uri?,
    bitmap: Bitmap?,
    onTakeCamera: () -> Unit,
    onPickGallery: () -> Unit
) {
    val isCaptured = uri != null || bitmap != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCaptured) PrimarySage.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (isCaptured) PrimarySage else MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isCaptured) PrimarySage else DeepForestAccent,
                        shape = CircleShape,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stepNumber,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepForestAccent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (isCaptured) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = PrimarySage,
                        shape = PillShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SurfaceWhite, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ready", style = MaterialTheme.typography.labelSmall, color = SurfaceWhite, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(10.dp))

            if (isCaptured) {
                // 9:16 Portrait Preview Thumbnail
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp) // Locked 9:16 vertical portrait format
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = uri,
                            contentDescription = title,
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
                    Button(
                        onClick = onTakeCamera,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = PillShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Camera",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onPickGallery,
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = PillShape,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Gallery",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
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
