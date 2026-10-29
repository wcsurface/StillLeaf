package app.vercel.stillleaf.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercel.stillleaf.data.model.SubscriptionStatus
import app.vercel.stillleaf.ui.theme.BorderStrokeColor
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import app.vercel.stillleaf.ui.theme.DeepForestAccent
import app.vercel.stillleaf.ui.theme.LakkiReddyFontFamily
import app.vercel.stillleaf.ui.theme.PillShape
import app.vercel.stillleaf.ui.theme.PrimarySage
import app.vercel.stillleaf.ui.theme.SurfaceVariantCream
import app.vercel.stillleaf.ui.theme.SurfaceWhite
import app.vercel.stillleaf.ui.theme.TerracottaAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    onDismissRequest: () -> Unit,
    onSubscribeRequested: (SubscriptionStatus) -> Unit,
    onRestorePurchasesRequested: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var selectedTier by remember { mutableStateOf(SubscriptionStatus.ANNUAL) }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = CreamyOffWhite,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Close Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = SurfaceVariantCream,
                    shape = PillShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = TerracottaAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("STILL LEAF PRO", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TerracottaAccent)
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = DeepForestAccent)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = "Nourish Your Botanical Sanctuary",
                fontFamily = LakkiReddyFontFamily,
                fontSize = 26.sp,
                color = DeepForestAccent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Unlock unlimited plants, unlimited AI vision scans, and 90-day growth milestone exports.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Feature Comparison List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BorderStrokeColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    FeatureCheckRow("Unlimited Sanctuary Plant Profiles", "Free tier limited to 3 plants")
                    FeatureCheckRow("Unlimited AI GPT-4o Vision Scans", "Free tier limited to 3 total scans")
                    FeatureCheckRow("90-Day Growth Milestone Exports", "High-res timelapse exports")
                    FeatureCheckRow("Zen Ad-Free Experience", "Zero banners or marketing interruptions")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tier Selector Cards
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Annual Plan (Best Value Highlighted)
                PaywallTierOptionCard(
                    title = "Annual Membership",
                    price = "$14.99 / year",
                    subtitle = "$1.24/month • Save 37%",
                    badgeText = "BEST VALUE",
                    isSelected = selectedTier == SubscriptionStatus.ANNUAL,
                    onClick = { selectedTier = SubscriptionStatus.ANNUAL }
                )

                // Monthly Plan
                PaywallTierOptionCard(
                    title = "Monthly Plan",
                    price = "$1.99 / month",
                    subtitle = "Cancel anytime in Play Store",
                    isSelected = selectedTier == SubscriptionStatus.MONTHLY,
                    onClick = { selectedTier = SubscriptionStatus.MONTHLY }
                )

                // Lifetime Plan
                PaywallTierOptionCard(
                    title = "Lifetime Access",
                    price = "$29.99 one-time",
                    subtitle = "Pay once, access forever on all devices",
                    badgeText = "LIFETIME UNLOCK",
                    isSelected = selectedTier == SubscriptionStatus.LIFETIME,
                    onClick = { selectedTier = SubscriptionStatus.LIFETIME }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subscribe Button
            Button(
                onClick = { onSubscribeRequested(selectedTier) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = PillShape,
                colors = ButtonDefaults.buttonColors(containerColor = PrimarySage)
            ) {
                Text(
                    text = when (selectedTier) {
                        SubscriptionStatus.MONTHLY -> "Subscribe Monthly ($1.99/mo)"
                        SubscriptionStatus.ANNUAL -> "Start Annual Sanctuary ($14.99/yr)"
                        SubscriptionStatus.LIFETIME -> "Unlock Lifetime Access ($29.99)"
                        else -> "Upgrade to Pro"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Restore Purchases Link
            TextButton(onClick = onRestorePurchasesRequested) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore Previous Purchases", style = MaterialTheme.typography.bodyMedium, color = PrimarySage)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun FeatureCheckRow(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = SurfaceVariantCream,
            shape = PillShape,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = PrimarySage,
                modifier = Modifier
                    .padding(4.dp)
                    .size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepForestAccent)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PaywallTierOptionCard(
    title: String,
    price: String,
    subtitle: String,
    badgeText: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SurfaceVariantCream else SurfaceWhite
        ),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) PrimarySage else BorderStrokeColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepForestAccent)
                    if (badgeText != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = TerracottaAccent,
                            shape = PillShape
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SurfaceWhite,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text(text = price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimarySage)
        }
    }
}
