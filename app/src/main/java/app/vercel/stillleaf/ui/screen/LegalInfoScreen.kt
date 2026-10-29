package app.vercel.stillleaf.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercel.stillleaf.R
import app.vercel.stillleaf.ui.component.LegalTab
import app.vercel.stillleaf.ui.component.StillLeafFooter
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import app.vercel.stillleaf.ui.theme.DeepForestAccent
import app.vercel.stillleaf.ui.theme.PillShape
import app.vercel.stillleaf.ui.theme.SurfaceWhite

@Composable
fun LegalInfoScreen(
    initialTab: LegalTab = LegalTab.TERMS,
    onBackRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(initialTab) }
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(R.drawable.bg_auth_profile),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackRequested) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = CreamyOffWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = when (activeTab) {
                        LegalTab.TERMS -> "Terms of Service"
                        LegalTab.PRIVACY -> "Privacy Policy"
                        LegalTab.FAQ -> "Frequently Asked Questions"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = CreamyOffWhite
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Selector Pill Row
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    color = SurfaceWhite.copy(alpha = 0.92f),
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Surface(
                            onClick = { activeTab = LegalTab.TERMS },
                            shape = PillShape,
                            color = if (activeTab == LegalTab.TERMS) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Terms",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == LegalTab.TERMS) SurfaceWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            onClick = { activeTab = LegalTab.PRIVACY },
                            shape = PillShape,
                            color = if (activeTab == LegalTab.PRIVACY) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Privacy",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == LegalTab.PRIVACY) SurfaceWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        Surface(
                            onClick = { activeTab = LegalTab.FAQ },
                            shape = PillShape,
                            color = if (activeTab == LegalTab.FAQ) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "FAQ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == LegalTab.FAQ) SurfaceWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite.copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        when (activeTab) {
                            LegalTab.TERMS -> TermsOfServiceContent()
                            LegalTab.PRIVACY -> PrivacyPolicyContent()
                            LegalTab.FAQ -> FaqContent()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer Links & Copyright
                StillLeafFooter(
                    onLegalRequested = { tab -> activeTab = tab }
                )
            }
        }
    }
}

@Composable
private fun TermsOfServiceContent() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Terms of Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepForestAccent)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("Effective Date: January 1, 2026 • Surface Apps", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("1. Acceptance of Terms")
        SectionBody("By downloading, accessing, or using Still Leaf - Whole Plant Care, you agree to be bound by these Terms of Service. If you do not agree, please do not use the application. Still Leaf is developed and operated by Surface Apps.")

        SectionTitle("2. Botanical Care Guidance Disclaimer")
        SectionBody("Still Leaf provides AI-assisted plant species identification, care schedules, and diagnostic health guidance for informational and educational purposes only. Environmental conditions, soil composition, and regional climates vary. Surface Apps is not liable for plant loss or agricultural damage resulting from reliance on guidance provided by the app.")

        SectionTitle("3. Subscriptions & In-App Purchases")
        SectionBody("Still Leaf offers both a Free Tier and Pro Membership options ($1.99/month, $14.99/year, or $29.99 lifetime unlock). All payments, recurring renewals, and cancellations are processed securely through Google Play Billing. Subscriptions renew automatically unless canceled at least 24 hours prior to the end of the billing period in your Google Play Account settings.")

        SectionTitle("4. Account Responsibilities & Data Rights")
        SectionBody("You are responsible for maintaining the security of your account credentials. You retain ownership of all plant photos and journal notes uploaded to your sanctuary. Surface Apps reserves the right to terminate accounts that violate system security or perform abusive API rate-limiting activities.")

        SectionTitle("5. Governing Law & Contact")
        SectionBody("These Terms shall be governed by and construed in accordance with applicable laws. For support or inquiry regarding Surface Apps products, contact support@surfaceapps.com.")
    }
}

@Composable
private fun PrivacyPolicyContent() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Privacy Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepForestAccent)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("Effective Date: January 1, 2026 • Surface Apps", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        SectionTitle("1. Information We Collect")
        SectionBody("Still Leaf collects minimal information required to deliver your sanctuary experience:\n• Account Info: Email address and display name upon sign-in.\n• Plant Photos & Notes: Photos you upload are stored securely in Cloudflare R2 object storage.\n• Usage Data: Anonymous diagnostics and subscription entitlement status verified via Google Play Services.")

        SectionTitle("2. Zero Data Selling Commitment")
        SectionBody("Surface Apps strictly values your privacy. We NEVER sell, rent, trade, or monetize your personal data, email address, or plant photos to third-party advertisers or data brokers.")

        SectionTitle("3. Cloud Storage & Encryption")
        SectionBody("Your plant photos are transmitted over encrypted HTTPS connections and stored securely in Cloudflare R2 cloud storage. Photos are bound strictly to your authenticated user account ID.")

        SectionTitle("4. User Control & Data Deletion")
        SectionBody("You have full control over your data. You may delete individual plants or growth photos at any time within the app. To request complete account and data deletion, contact support@surfaceapps.com.")

        SectionTitle("5. Policy Updates")
        SectionBody("Surface Apps may update this Privacy Policy periodically to reflect app enhancements or legal compliance standards. Continued use of Still Leaf constitutes acceptance of updated terms.")
    }
}

@Composable
private fun FaqContent() {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text("Frequently Asked Questions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepForestAccent)
        }

        Spacer(modifier = Modifier.height(16.dp))

        FaqItem(
            question = "How does plant identification work?",
            answer = "Take Photo 1 (Close-Up detail of the leaf/flower) and Photo 2 (Whole plant in its pot). Still Leaf analyzes both photos together to identify the exact botanical species and generate a custom care schedule."
        )

        FaqItem(
            question = "What are the Free vs. Pro tier limits?",
            answer = "Free tier allows tracking up to 3 plants and 3 free identifications. Still Leaf Pro ($1.99/mo, $14.99/yr, or $29.99 lifetime) unlocks unlimited plant profiles, unlimited identifications, and 90-day growth exports."
        )

        FaqItem(
            question = "How do 90-Day Growth Milestones work?",
            answer = "Every 90 days, Still Leaf reminds you to photograph your plant in its pot. You can view side-by-side growth comparisons (Day 0 vs Day 90 vs Day 180) in your plant's Quarterly Growth Journal."
        )

        FaqItem(
            question = "How do I restore my Pro purchase on a new phone?",
            answer = "Go to Settings & Profile and tap 'Restore'. Still Leaf queries Google Play Services and instantly restores your Pro access on any phone logged into your Google Play Account."
        )

        FaqItem(
            question = "How do I cancel a recurring subscription?",
            answer = "You can manage or cancel your subscription anytime in the Google Play Store app under Subscriptions. You maintain Pro benefits until the end of your billing cycle."
        )

        FaqItem(
            question = "Who develops Still Leaf?",
            answer = "Still Leaf - Whole Plant Care is proudly developed and maintained by Surface Apps."
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = DeepForestAccent,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SectionBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp
    )
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Q: $question",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepForestAccent
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = answer,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}
