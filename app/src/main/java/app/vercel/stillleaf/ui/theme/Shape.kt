package app.vercel.stillleaf.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val PillShape = RoundedCornerShape(100.dp)
val SmallControlShape = RoundedCornerShape(12.dp)
val StandardCardShape = RoundedCornerShape(18.dp)
val LargePanelShape = RoundedCornerShape(24.dp)

val ZenShapes = Shapes(
    small = SmallControlShape,
    medium = StandardCardShape,
    large = LargePanelShape,
    extraLarge = RoundedCornerShape(32.dp)
)
