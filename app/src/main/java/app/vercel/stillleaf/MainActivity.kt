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
import app.vercel.stillleaf.data.model.Plant
import app.vercel.stillleaf.data.model.PlantPhoto
import app.vercel.stillleaf.ui.component.BannerAdView
import app.vercel.stillleaf.ui.screen.PlantDetailScreen
import app.vercel.stillleaf.ui.screen.PlantListScreen
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

                // Intercept system back button when viewing plant details
                if (selectedPlant != null) {
                    BackHandler {
                        selectedPlant = null
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
                            targetState = selectedPlant,
                            label = "ScreenTransition"
                        ) { targetPlant ->
                            if (targetPlant != null) {
                                // Detailed Plant Profile View
                                PlantDetailScreen(
                                    plant = targetPlant,
                                    subscriptionStatus = subscriptionStatus,
                                    onBackRequested = {
                                        selectedPlant = null
                                    },
                                    onUpgradeRequested = {
                                        launchBillingUpgrade()
                                    }
                                )
                            } else {
                                // Main Botanical Sanctuary Dashboard
                                PlantListScreen(
                                    plants = plantList,
                                    subscriptionStatus = subscriptionStatus,
                                    onPlantSelected = { plant ->
                                        selectedPlant = plant
                                    },
                                    onAddPlantRequested = {
                                        if (subscriptionStatus.isPro || plantList.size < subscriptionStatus.maxPlantsAllowed) {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Opening Camera AI Plant Identifier...",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                this@MainActivity,
                                                "Free tier limit reached (2 plants max). Upgrade to Pro for unlimited plants!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            launchBillingUpgrade()
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
            nickname = "Monstera Queen",
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
            photos = monsteraPhotos
        )

        val figPhotos = listOf(
            PlantPhoto(
                id = "p4",
                plantId = "fig_1",
                photoUrl = "https://images.unsplash.com/photo-1530968464165-7a1861cbaf9f?auto=format&fit=crop&w=800&q=80",
                dateTaken = today.minusDays(90),
                ageInDays = 0,
                note = "Repotted in terra cotta vessel with perlite soil.",
                isQuarterlyMilestone = true,
                milestoneTitle = "Day 0 - Repotted"
            )
        )

        val fiddleFig = Plant(
            id = "fig_1",
            nickname = "Fiddle Fig",
            botanicalName = "Ficus lyrata",
            commonName = "Fiddle-Leaf Fig",
            primaryPhotoUrl = "https://images.unsplash.com/photo-1530968464165-7a1861cbaf9f?auto=format&fit=crop&w=800&q=80",
            dateAdded = today.minusDays(90),
            careRequirements = CareRequirement(
                sunlight = "Direct Bright Light",
                idealTemperature = "18°C - 24°C",
                idealHumidity = "50% - 70%",
                waterIntervalDays = 10,
                feedIntervalDays = 30,
                rotateIntervalDays = 7,
                trimIntervalDays = 90,
                careInsight = "Avoid moving position frequently. Dust glossy leaves regularly to maximize photosynthesis."
            ),
            photos = figPhotos
        )

        return listOf(monstera, fiddleFig)
    }
}
