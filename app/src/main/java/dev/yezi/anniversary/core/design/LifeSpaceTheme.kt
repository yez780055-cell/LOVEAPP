package dev.yezi.anniversary.core.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object AppColors {
    val primary = Color(0xFF6D929E)
    val onPrimary = Color.White
    val background = Color(0xFFFFFCF8)
    val surface = Color.White
    val ink = Color(0xFF17232D)
    val muted = Color(0xFF71808A)
    val accent = Color(0xFFE68C87)
}
object AppTypography { val tokens = Typography() }
object AppSpacing { val xSmall = 4.dp; val small = 8.dp; val medium = 16.dp; val large = 24.dp; val xLarge = 32.dp }
object AppShapes { val card = RoundedCornerShape(20.dp); val control = RoundedCornerShape(14.dp) }

@Composable
fun LifeSpaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(primary = AppColors.primary, onPrimary = AppColors.onPrimary, background = AppColors.background, surface = AppColors.surface, onSurface = AppColors.ink, secondary = AppColors.accent), typography = AppTypography.tokens, content = content)
}
