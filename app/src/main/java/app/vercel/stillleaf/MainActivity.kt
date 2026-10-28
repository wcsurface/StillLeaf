package app.vercel.stillleaf

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.vercel.stillleaf.billing.BillingManager
import app.vercel.stillleaf.data.model.CareRequirement
import app.vercel.stillleaf.data.model.HealthStatusType
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.PlantPhoto
import app.vercel.stillleaf.ui.component.BannerAdView
import app.vercel.stillleaf.ui.screen.AddPlantScreen
import app.vercel.stillleaf.ui.screen.CalendarScreen
import app.vercel.stillleaf.ui.screen.NavTab
import app.vercel.stillleaf.ui.screen.PlantDetailScreen
import app.vercel.stillleaf.ui.screen.PlantListScreen
import app.vercel.stillleaf.ui.screen.SettingsScreen
import app.vercel.stillleaf.ui.theme.StillLeafTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        billingManager = BillingManager(applicationContext)

        val initialPlants = createSamplePlantList()

        setContent {
            StillLeafTheme {
                val subscriptionStatus by billingManager.subscriptionStatus.collectAsState()
                var plantList by remember { mutableStateOf(initialPlants) }
                var selectedPlant by remember { mutableStateOf<Plant?>(null) }
                var currentTab by remember { mutableStateOf(NavTab.HOME) }

                // Intercept system back button
                if (selectedPlant != null) {
                    BackHandler {
                        selectedPlant = null
                    }
                } else if (currentTab != NavTab.HOME) {
                    BackHandler {
                        currentTab = NavTab.HOME
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        // Banner Ad is rendered only for FREE tier users; suppressed for PRO
                        BannerAdView(subscriptionStatus = subscriptionStatus)
                    }
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
                                            launchBillingUpgrade()
                                        }
                                    )
                                }
                                activeTab == NavTab.ADD -> {
                                    // 2. Add / AI Plant Identification Screen
                                    AddPlantScreen(
                                        onBackRequested = {
                                            currentTab = NavTab.HOME
                                        },
                                        onPlantAdded = { newPlant ->
                                            plantList = listOf(newPlant) + plantList
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
                                        }
                                    )
                                }
                                activeTab == NavTab.SETTINGS -> {
                                    // 4. Settings & Subscription Preferences
                                    SettingsScreen(
                                        subscriptionStatus = subscriptionStatus,
                                        onBackRequested = {
                                            currentTab = NavTab.HOME
                                        },
                                        onUpgradeRequested = {
                                            launchBillingUpgrade()
                                        }
                                    )
                                }
                                else -> {
                                    // 5. Main Botanical Sanctuary Dashboard (Matching User Mockup)
                                    PlantListScreen(
                                        plants = plantList,
                                        subscriptionStatus = subscriptionStatus,
                                        currentTab = currentTab,
                                        onPlantSelected = { plant ->
                                            selectedPlant = plant
                                        },
                                        onNavTabSelected = { tab ->
                                            if (tab == NavTab.ADD && !subscriptionStatus.isPro && plantList.size >= subscriptionStatus.maxPlantsAllowed) {
                                                Toast.makeText(
                                                    this@MainActivity,
                                                    "Free tier limit reached (2 plants max). Upgrade to Pro for unlimited plants!",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                launchBillingUpgrade()
                                            } else {
                                                currentTab = tab
                                            }
                                        },
                                        onUpgradeRequested = {
                                            launchBillingUpgrade()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun launchBillingUpgrade() {
        val launched = billingManager.launchBillingFlow(this@MainActivity)
        if (!launched) {
            Toast.makeText(
                this@MainActivity,
                "Initiating Pro Upgrade Flow ($4.99)...",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::billingManager.isInitialized) {
            billingManager.endConnection()
        }
    }

    private fun createSamplePlantList(): List<Plant> {
        val today = LocalDate.now()

        val monsteraPhotos = listOf(
            PlantPhoto(
                id = "p1",
                plantId = "monstera_1",
                photoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80",
                dateTaken = today.minusDays(180),
                ageInDays = 0,
                note = "Welcome home! Placed near east-facing window.",
                isQuarterlyMilestone = true,
                milestoneTitle = "Day 0 - Welcome"
            ),
            PlantPhoto(
                id = "p2",
                plantId = "monstera_1",
                photoUrl = "https://images.unsplash.com/photo-1545241047-6083a3684587?auto=format&fit=crop&w=800&q=80",
                dateTaken = today.minusDays(90),
                ageInDays = 90,
                note = "First fenestrated split leaf emerging!",
                isQuarterlyMilestone = true,
                milestoneTitle = "Day 90 - First Split"
            ),
            PlantPhoto(
                id = "p3",
                plantId = "monstera_1",
                photoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80",
                dateTaken = today,
                ageInDays = 180,
                note = "Summer growth surge! Three new vibrant leaves.",
                isQuarterlyMilestone = true,
                milestoneTitle = "Day 180 - Summer Surge"
            )
        )

        val monstera = Plant(
            id = "monstera_1",
            nickname = "Monstera Deliciosa",
            botanicalName = "Monstera deliciosa",
            commonName = "Swiss Cheese Plant",
            primaryPhotoUrl = "https://images.unsplash.com/photo-1614594975525-e45190c55d0b?auto=format&fit=crop&w=800&q=80",
            dateAdded = today.minusDays(180),
            careRequirements = CareRequirement(
                sunlight = "Bright Indirect Light",
                idealTemperature = "18°C - 27°C",
                idealHumidity = "60% - 80%",
                waterIntervalDays = 7,
                feedIntervalDays = 30,
                rotateIntervalDays = 14,
                trimIntervalDays = 90,
                careInsight = "Thrives in gentle ambient light. Let top 2 inches of soil dry before watering."
            ),
            location = "Indoor • Living Room",
            healthStatusType = HealthStatusType.HEALTHY,
            healthStatusText = "Healthy",
            photos = monsteraPhotos
        )

        val snakePlant = Plant(
            id = "snake_1",
            nickname = "Snake Plant",
            botanicalName = "Sansevieria trifasciata",
            commonName = "Mother-in-law's Tongue",
            primaryPhotoUrl = "https://images.unsplash.com/photo-1593482892290-f54927ae1bf6?auto=format&fit=crop&w=800&q=80",
            dateAdded = today.minusDays(120),
            careRequirements = CareRequirement(
                sunlight = "Low to Bright Indirect Light",
                idealTemperature = "15°C - 29°C",
                idealHumidity = "30% - 50%",
                waterIntervalDays = 14,
                feedIntervalDays = 60,
                rotateIntervalDays = 30,
                trimIntervalDays = 180,
                careInsight = "Extremely drought-tolerant. Soil should dry out completely between waterings."
            ),
            location = "Indoor • Office",
            healthStatusType = HealthStatusType.NEEDS_WATER,
            healthStatusText = "Needs Water",
            photos = emptyList()
        )

        val basil = Plant(
            id = "basil_1",
            nickname = "Basil",
            botanicalName = "Ocimum basilicum",
            commonName = "Sweet Basil",
            primaryPhotoUrl = "https://images.unsplash.com/photo-1608686207856-001b95cf60ca?auto=format&fit=crop&w=800&q=80",
            dateAdded = today.minusDays(45),
            careRequirements = CareRequirement(
                sunlight = "Full Sunlight 6+ hours",
                idealTemperature = "20°C - 30°C",
                idealHumidity = "40% - 60%",
                waterIntervalDays = 3,
                feedIntervalDays = 14,
                rotateIntervalDays = 7,
                trimIntervalDays = 14,
                careInsight = "Pinch top leaves regularly to encourage bushy, aromatic growth."
            ),
            location = "Outdoor • Patio",
            healthStatusType = HealthStatusType.HEALTHY,
            healthStatusText = "Healthy",
            photos = emptyList()
        )

        val lavender = Plant(
            id = "lavender_1",
            nickname = "Lavender",
            botanicalName = "Lavandula angustifolia",
            commonName = "English Lavender",
            primaryPhotoUrl = "https://images.unsplash.com/photo-1528183429752-a97d0bf99b5a?auto=format&fit=crop&w=800&q=80",
            dateAdded = today.minusDays(60),
            careRequirements = CareRequirement(
                sunlight = "Full Direct Sunlight",
                idealTemperature = "18°C - 28°C",
                idealHumidity = "30% - 40%",
                waterIntervalDays = 7,
                feedIntervalDays = 30,
                rotateIntervalDays = 14,
                trimIntervalDays = 60,
                careInsight = "Requires well-draining lean soil. Apply organic bloom booster fertilizer for fragrant purple spikes."
            ),
            location = "Outdoor • Garden",
            healthStatusType = HealthStatusType.NEEDS_FERTILIZER,
            healthStatusText = "Needs Fertilizer",
            photos = emptyList()
        )

        return listOf(monstera, snakePlant, basil, lavender)
    }
}
