package app.vercel.stillleaf

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.vercel.stillleaf.billing.BillingManager
import app.vercel.stillleaf.data.model.AuthUser
import app.vercel.stillleaf.data.model.HealthStatusType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.SubscriptionStatus
import app.vercel.stillleaf.data.repository.PlantPersistenceManager
import app.vercel.stillleaf.ui.component.LegalTab
import app.vercel.stillleaf.ui.component.PaywallBottomSheet
import app.vercel.stillleaf.ui.screen.AddPlantScreen
import app.vercel.stillleaf.ui.screen.AuthScreen
import app.vercel.stillleaf.ui.screen.CalendarScreen
import app.vercel.stillleaf.ui.screen.LegalInfoScreen
import app.vercel.stillleaf.ui.screen.NavTab
import app.vercel.stillleaf.ui.screen.PlantDetailScreen
import app.vercel.stillleaf.ui.screen.PlantListScreen
import app.vercel.stillleaf.ui.screen.SettingsScreen
import app.vercel.stillleaf.ui.theme.StillLeafTheme

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide Status Bar (Clock, Battery, Wi-Fi, Notifications) for 100% Immersive Full-Screen Experience
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // Initialize Notification Channels for Daily Care Reminders & 90-Day Milestones
        app.vercel.stillleaf.util.CareNotificationManager.createNotificationChannels(applicationContext)

        billingManager = BillingManager(applicationContext)

        setContent {
            StillLeafTheme {
                val subscriptionStatus by billingManager.subscriptionStatus.collectAsState()

                // Load saved plants from persistent disk storage
                val restoredPlants = remember { PlantPersistenceManager.loadPlantsLocally(applicationContext) }
                var plantList by remember { mutableStateOf<List<Plant>>(restoredPlants) }

                var selectedPlant by remember { mutableStateOf<Plant?>(null) }
                var currentTab by remember { mutableStateOf(NavTab.HOME) }
                var currentUser by remember { mutableStateOf<AuthUser?>(null) }
                var showPaywallSheet by remember { mutableStateOf(false) }
                var activeLegalTab by remember { mutableStateOf<LegalTab?>(null) }

                // Calculate total pending care tasks due today across sanctuary
                val pendingTasksCount = remember(plantList) {
                    val today = java.time.LocalDate.now()
                    plantList.sumOf { plant ->
                        val waterDone = plant.recentLogs.any { it.taskType == app.vercel.stillleaf.data.model.CareTaskType.WATER && it.completedAt.toLocalDate() == today }
                        val feedDone = plant.recentLogs.any { it.taskType == app.vercel.stillleaf.data.model.CareTaskType.FEED && it.completedAt.toLocalDate() == today }
                        val rotateDone = plant.recentLogs.any { it.taskType == app.vercel.stillleaf.data.model.CareTaskType.ROTATE && it.completedAt.toLocalDate() == today }

                        var count = 0
                        if (!waterDone) count++
                        if (!feedDone) count++
                        if (!rotateDone) count++
                        count
                    }
                }

                // Helper to update plant list & persist to disk
                fun updateAndPersistPlants(newList: List<Plant>) {
                    plantList = newList
                    PlantPersistenceManager.savePlantsLocally(applicationContext, newList)
                }

                // Intercept system back button
                if (currentUser != null) {
                    if (activeLegalTab != null) {
                        BackHandler {
                            activeLegalTab = null
                        }
                    } else if (selectedPlant != null) {
                        BackHandler {
                            selectedPlant = null
                        }
                    } else if (currentTab != NavTab.HOME) {
                        BackHandler {
                            currentTab = NavTab.HOME
                        }
                    }
                }

                // Mandatory Authentication Gate: User CANNOT enter until signed in
                if (currentUser == null) {
                    if (activeLegalTab != null) {
                        LegalInfoScreen(
                            initialTab = activeLegalTab!!,
                            onBackRequested = { activeLegalTab = null }
                        )
                    } else {
                        AuthScreen(
                            onAuthSuccess = { authenticatedUser ->
                                currentUser = authenticatedUser
                                Toast.makeText(
                                    this@MainActivity,
                                    "Welcome to your Sanctuary, ${authenticatedUser.displayName}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            onLegalRequested = { tab ->
                                activeLegalTab = tab
                            }
                        )
                    }
                } else if (activeLegalTab != null) {
                    // Legal Info Screen (Terms, Privacy, FAQ)
                    LegalInfoScreen(
                        initialTab = activeLegalTab!!,
                        onBackRequested = { activeLegalTab = null }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            Crossfade(
                                targetState = Pair(selectedPlant, currentTab),
                                label = "MainScreenTransition"
                            ) { (activePlant, activeTab) ->
                                when {
                                    activePlant != null -> {
                                        // 1. Detailed Plant Profile View
                                        PlantDetailScreen(
                                            plant = activePlant,
                                            subscriptionStatus = subscriptionStatus,
                                            onBackRequested = {
                                                selectedPlant = null
                                            },
                                            onUpgradeRequested = {
                                                showPaywallSheet = true
                                            },
                                            onEditPlantRequested = { targetPlant, newNickname, newLocation ->
                                                val updatedPlant = targetPlant.copy(
                                                    nickname = newNickname,
                                                    location = newLocation
                                                )
                                                val updatedList = plantList.map { if (it.id == targetPlant.id) updatedPlant else it }
                                                updateAndPersistPlants(updatedList)
                                                selectedPlant = updatedPlant
                                                Toast.makeText(this@MainActivity, "Updated ${updatedPlant.nickname}!", Toast.LENGTH_SHORT).show()
                                            },
                                            onDeletePlantRequested = { targetPlant ->
                                                val updatedList = plantList.filter { it.id != targetPlant.id }
                                                updateAndPersistPlants(updatedList)
                                                selectedPlant = null
                                                Toast.makeText(this@MainActivity, "Removed ${targetPlant.nickname} from Sanctuary", Toast.LENGTH_SHORT).show()
                                            },
                                            onDiagnosisCreated = { targetPlant, diagnosisResult, scheduleOnCalendar ->
                                                val updatedPlant = targetPlant.copy(
                                                    activeDiagnosis = diagnosisResult,
                                                    healthStatusType = HealthStatusType.NEEDS_WATER,
                                                    healthStatusText = "Treatment Active"
                                                )
                                                val updatedList = plantList.map { if (it.id == targetPlant.id) updatedPlant else it }
                                                updateAndPersistPlants(updatedList)
                                                selectedPlant = updatedPlant

                                                val msg = if (scheduleOnCalendar) {
                                                    "Diagnosis saved & Special Care Treatment scheduled on Calendar!"
                                                } else {
                                                    "Diagnosis saved to ${updatedPlant.nickname}'s profile!"
                                                }
                                                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    }
                                    activeTab == NavTab.ADD -> {
                                        // 2. Add / AI Plant Identification Screen
                                        AddPlantScreen(
                                            onBackRequested = {
                                                currentTab = NavTab.HOME
                                            },
                                            onNavTabSelected = { tab ->
                                                currentTab = tab
                                            },
                                            onLegalRequested = { tab ->
                                                activeLegalTab = tab
                                            },
                                            pendingTasksCount = pendingTasksCount,
                                            onPlantAdded = { newPlant ->
                                                val updatedList = listOf(newPlant) + plantList
                                                updateAndPersistPlants(updatedList)
                                                currentTab = NavTab.HOME
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "“${newPlant.nickname}” saved to your Sanctuary!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    }
                                    activeTab == NavTab.CALENDAR -> {
                                        // 3. Care & Calendar Dashboard
                                        CalendarScreen(
                                            plants = plantList,
                                            onBackRequested = {
                                                currentTab = NavTab.HOME
                                            },
                                            onNavTabSelected = { tab ->
                                                currentTab = tab
                                            },
                                            onLegalRequested = { tab ->
                                                activeLegalTab = tab
                                            },
                                            pendingTasksCount = pendingTasksCount,
                                            onTaskToggled = { plantId, taskType, isCompleted ->
                                                val updatedList = plantList.map { plant ->
                                                    if (plant.id == plantId) {
                                                        val updatedLogs = if (isCompleted) {
                                                            plant.recentLogs + app.vercel.stillleaf.data.model.CareLog(
                                                                id = "log_${System.currentTimeMillis()}",
                                                                plantId = plantId,
                                                                taskType = taskType,
                                                                completedAt = java.time.LocalDateTime.now()
                                                            )
                                                        } else {
                                                            plant.recentLogs.filterNot { log ->
                                                                log.taskType == taskType && log.completedAt.toLocalDate() == java.time.LocalDate.now()
                                                            }
                                                        }
                                                        plant.copy(recentLogs = updatedLogs)
                                                    } else {
                                                        plant
                                                    }
                                                }
                                                updateAndPersistPlants(updatedList)
                                            }
                                        )
                                    }
                                    activeTab == NavTab.SETTINGS -> {
                                        // 4. Settings & Account Profile Preferences
                                        SettingsScreen(
                                            subscriptionStatus = subscriptionStatus,
                                            currentUser = currentUser,
                                            onBackRequested = {
                                                currentTab = NavTab.HOME
                                            },
                                            onNavTabSelected = { tab ->
                                                currentTab = tab
                                            },
                                            onLegalRequested = { tab ->
                                                activeLegalTab = tab
                                            },
                                            pendingTasksCount = pendingTasksCount,
                                            onUpgradeRequested = {
                                                showPaywallSheet = true
                                            },
                                            onRestorePurchasesRequested = {
                                                billingManager.queryExistingPurchases()
                                                Toast.makeText(this@MainActivity, "Restoring Google Play purchases...", Toast.LENGTH_SHORT).show()
                                            },
                                            onSignOutRequested = {
                                                currentUser = null
                                                Toast.makeText(this@MainActivity, "Signed out of account", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                    else -> {
                                        // 5. Main Botanical Sanctuary Dashboard
                                        PlantListScreen(
                                            plants = plantList,
                                            subscriptionStatus = subscriptionStatus,
                                            currentUser = currentUser,
                                            currentTab = currentTab,
                                            pendingTasksCount = pendingTasksCount,
                                            onPlantSelected = { plant ->
                                                selectedPlant = plant
                                            },
                                            onNavTabSelected = { tab ->
                                                if (tab == NavTab.ADD && !subscriptionStatus.isPro && plantList.size >= subscriptionStatus.maxPlantsAllowed) {
                                                    Toast.makeText(
                                                        this@MainActivity,
                                                        "Free tier limit reached (3 plants max). Upgrade to Pro for unlimited plants!",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                    showPaywallSheet = true
                                                } else {
                                                    currentTab = tab
                                                }
                                            },
                                            onUpgradeRequested = {
                                                showPaywallSheet = true
                                            },
                                            onLegalRequested = { tab ->
                                                activeLegalTab = tab
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Paywall Bottom Sheet
                if (showPaywallSheet) {
                    PaywallBottomSheet(
                        onDismissRequest = { showPaywallSheet = false },
                        onSubscribeRequested = { targetTier ->
                            showPaywallSheet = false
                            if (targetTier == SubscriptionStatus.LIFETIME) {
                                billingManager.launchLifetimePurchaseFlow(this@MainActivity)
                            } else {
                                billingManager.launchSubscriptionFlow(this@MainActivity, targetTier)
                            }
                        },
                        onRestorePurchasesRequested = {
                            showPaywallSheet = false
                            billingManager.queryExistingPurchases()
                            Toast.makeText(this@MainActivity, "Restoring Google Play purchases...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingManager.isInitialized) {
            billingManager.endConnection()
        }
    }
}
