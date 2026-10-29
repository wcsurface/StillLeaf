package app.vercel.stillleaf.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.vercel.stillleaf.ui.theme.CreamyOffWhite
import java.time.Year

enum class LegalTab {
    TERMS,
    PRIVACY,
    FAQ
}

@Composable
fun StillLeafFooter(
    onLegalRequested: (LegalTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentYear = rememberYear()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terms of Service",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CreamyOffWhite,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onLegalRequested(LegalTab.TERMS) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Text(
                text = " • ",
                style = MaterialTheme.typography.labelMedium,
                color = CreamyOffWhite.copy(alpha = 0.7f)
            )

            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CreamyOffWhite,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onLegalRequested(LegalTab.PRIVACY) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Text(
                text = " • ",
                style = MaterialTheme.typography.labelMedium,
                color = CreamyOffWhite.copy(alpha = 0.7f)
            )

            Text(
                text = "FAQ",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CreamyOffWhite,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onLegalRequested(LegalTab.FAQ) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "© $currentYear Surface Apps. All rights reserved.",
            style = MaterialTheme.typography.labelSmall,
            color = CreamyOffWhite.copy(alpha = 0.85f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun rememberYear(): Int {
    return try {
        Year.now().value
    } catch (_: Exception) {
        2026
    }
}
