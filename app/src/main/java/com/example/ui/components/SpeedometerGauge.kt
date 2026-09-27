package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TestPhase
import com.example.ui.theme.AmberLatency
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldSpeed
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SpeedometerGauge(
    speedMbps: Double,
    phase: TestPhase,
    progressRatio: Float,
    modifier: Modifier = Modifier
) {
    // Logarithmic-like scaling for gauge: 0..1000 Mbps mapped to 0..1
    // 0 -> 0.0, 10 -> 0.25, 50 -> 0.50, 100 -> 0.65, 300 -> 0.82, 1000 -> 1.0
    val targetFraction = calculateGaugeFraction(speedMbps)
    val animatedFraction by animateFloatAsState(
        targetValue = targetFraction.toFloat(),
        animationSpec = tween(durationMillis = 180),
        label = "GaugeSweep"
    )

    val activeColor = when (phase) {
        TestPhase.DOWNLOAD -> CyanNeon
        TestPhase.UPLOAD -> EmeraldSpeed
        TestPhase.PING, TestPhase.PING_SWEEP -> AmberLatency
        TestPhase.FINISHED -> CyanGlow
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .size(270.dp)
            .testTag("speedometer_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val diameter = min(size.width, size.height) - strokeWidth * 2
            val radius = diameter / 2f
            val center = Offset(size.width / 2f, size.height / 2f + 10.dp.toPx())
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            val startAngle = 150f
            val totalSweep = 240f

            // 1. Inactive background track
            drawArc(
                color = Color(0x25FFFFFF),
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 2. Active arc with gradient
            val currentSweep = totalSweep * animatedFraction.coerceIn(0f, 1f)
            if (currentSweep > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color(0xFF00B0FF),
                        0.5f to CyanNeon,
                        1.0f to EmeraldSpeed
                    ),
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // 3. Tick Marks
            val tickCount = 9
            val tickRadiusInner = radius - strokeWidth - 6.dp.toPx()
            val tickRadiusOuter = radius - strokeWidth - 14.dp.toPx()

            for (i in 0 until tickCount) {
                val fraction = i.toFloat() / (tickCount - 1)
                val angleDeg = startAngle + totalSweep * fraction
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val p1 = Offset(
                    center.x + (tickRadiusInner * cos(angleRad)).toFloat(),
                    center.y + (tickRadiusInner * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    center.x + (tickRadiusOuter * cos(angleRad)).toFloat(),
                    center.y + (tickRadiusOuter * sin(angleRad)).toFloat()
                )

                val tickColor = if (fraction <= animatedFraction) activeColor else Color(0x33FFFFFF)
                drawLine(
                    color = tickColor,
                    start = p1,
                    end = p2,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // 4. Sweeping Needle Tip Indicator Dot
            if (animatedFraction > 0.01f) {
                val needleAngle = startAngle + totalSweep * animatedFraction.coerceIn(0f, 1f)
                val needleRad = Math.toRadians(needleAngle.toDouble())
                val tipPos = Offset(
                    center.x + (radius * cos(needleRad)).toFloat(),
                    center.y + (radius * sin(needleRad)).toFloat()
                )

                // Glowing outer halo
                drawCircle(
                    color = activeColor.copy(alpha = 0.35f),
                    radius = strokeWidth * 1.1f,
                    center = tipPos
                )
                // Solid center dot
                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.45f,
                    center = tipPos
                )
            }
        }

        // Digital Speed Display in Center
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            val formattedSpeed = when {
                phase == TestPhase.IDLE -> "0.0"
                phase == TestPhase.PING || phase == TestPhase.PING_SWEEP -> "..."
                speedMbps < 10.0 -> String.format(java.util.Locale.US, "%.2f", speedMbps)
                speedMbps < 100.0 -> String.format(java.util.Locale.US, "%.1f", speedMbps)
                else -> String.format(java.util.Locale.US, "%.0f", speedMbps)
            }

            Text(
                text = formattedSpeed,
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = (-1).sp
            )

            Text(
                text = "Mbps",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = activeColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            val phaseLabel = when (phase) {
                TestPhase.IDLE -> "READY"
                TestPhase.PING_SWEEP -> "SCANNING"
                TestPhase.PING -> "PINGING"
                TestPhase.DOWNLOAD -> "DOWNLOAD"
                TestPhase.UPLOAD -> "UPLOAD"
                TestPhase.FINISHED -> "COMPLETE"
                TestPhase.ERROR -> "ERROR"
                TestPhase.CANCELLED -> "CANCELLED"
            }

            Text(
                text = phaseLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                letterSpacing = 2.sp
            )
        }
    }
}

private fun calculateGaugeFraction(speedMbps: Double): Double {
    if (speedMbps <= 0.0) return 0.0
    // Smooth nonlinear progression
    return when {
        speedMbps <= 10.0 -> (speedMbps / 10.0) * 0.25
        speedMbps <= 50.0 -> 0.25 + ((speedMbps - 10.0) / 40.0) * 0.25
        speedMbps <= 100.0 -> 0.50 + ((speedMbps - 50.0) / 50.0) * 0.18
        speedMbps <= 300.0 -> 0.68 + ((speedMbps - 100.0) / 200.0) * 0.17
        speedMbps <= 1000.0 -> 0.85 + ((speedMbps - 300.0) / 700.0) * 0.15
        else -> 1.0
    }
}
