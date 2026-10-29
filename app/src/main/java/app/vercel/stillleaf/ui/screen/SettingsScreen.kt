package app.vercel.stillleaf.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.vercel.stillleaf.R
import app.vercel.stillleaf.data.model.AuthUser
import app.vercel.stillleaf.data.model.SubscriptionStatus
import app.vercel.stillleaf.ui.component.StillLeafTopBar
import app.vercel.stillleaf.ui.theme.AttentionTerracotta
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import app.vercel.stillleaf.ui.theme.PillShape
import app.vercel.stillleaf.ui.theme.SurfaceWhite

@Composable
fun SettingsScreen(
    subscriptionStatus: SubscriptionStatus,
    currentUser: AuthUser? = null,
    onBackRequested: () -> Unit,
    onUpgradeRequested: () -> Unit,
    onRestorePurchasesRequested: () -> Unit = {},
    onSignInRequested: () -> Unit = {},
    onSignOutRequested: () -> Unit = {},
    onNavTabSelected: (NavTab) -> Unit = {},
    onLegalRequested: (app.vercel.stillleaf.ui.component.LegalTab) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var milestoneRemindersEnabled by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Background Image (bg_auth_profile)
        Image(
            painter = painterResource(R.drawable.bg_auth_profile),
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
                currentTab = NavTab.SETTINGS,
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
                    text = "Settings & Profile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CreamyOffWhite
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Profile Card
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(24.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = currentUser?.displayName ?: "Gardener Sanctuary",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (currentUser != null) "${currentUser.email} (${currentUser.authProvider})" else "Guest Account Session",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = onSignOutRequested,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = PillShape,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign Out of Account", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onSignInRequested,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sign In or Register Account", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subscription Tier Banner Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (subscriptionStatus.isPro) SurfaceWhite.copy(alpha = 0.95f) else SurfaceWhite.copy(alpha = 0.95f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (subscriptionStatus.isPro) MaterialTheme.colorScheme.primary else AttentionTerracotta.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (subscriptionStatus.isPro) Icons.Default.Star else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (subscriptionStatus.isPro) MaterialTheme.colorScheme.primary else AttentionTerracotta,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (subscriptionStatus.isPro) "Still Leaf Pro Member" else "Free Account (3 Plant Limit)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (subscriptionStatus.isPro) "Enjoy unlimited botanical tracking, health diagnostics, and a peaceful ad-free experience." else "Upgrade to Still Leaf Pro ($1.99/mo, $14.99/yr, or $29.99 lifetime) for unlimited plants.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (!subscriptionStatus.isPro) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = onUpgradeRequested,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(46.dp),
                                    shape = PillShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = AttentionTerracotta)
                                ) {
                                    Text("Upgrade", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onRestorePurchasesRequested,
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(46.dp),
                                    shape = PillShape,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Restore", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Notifications & Reminders",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CreamyOffWhite
                )

                Spacer(modifier = Modifier.height(12.dp))

                SettingToggleRow(
                    icon = Icons.Default.Notifications,
                    title = "Daily Care Reminders",
                    subtitle = "Morning notifications for due water, feed & rotation tasks",
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                SettingToggleRow(
                    icon = Icons.Default.Spa,
                    title = "90-Day Growth Milestones",
                    subtitle = "Reminders to photograph quarterly visual growth progress",
                    checked = milestoneRemindersEnabled,
                    onCheckedChange = { milestoneRemindersEnabled = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

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
fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        color = SurfaceWhite.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
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
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
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

            Spacer(modifier = Modifier.width(10.dp))

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
