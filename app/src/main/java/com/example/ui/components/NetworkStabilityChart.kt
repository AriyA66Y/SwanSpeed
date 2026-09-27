package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SpeedSample
import com.example.data.model.TestPhase
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldSpeed
import kotlin.math.max

@Composable
fun NetworkStabilityChart(
    samples: List<SpeedSample>,
    currentPhase: TestPhase,
    stabilityScore: Double,
    modifier: Modifier = Modifier,
    title: String = "Network Stability Graph",
    isLive: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val validSamples = samples.filter { it.speedMbps > 0.0 }
    val maxSpeed = max(10.0, (validSamples.maxOfOrNull { it.speedMbps } ?: 20.0) * 1.15)
    val avgSpeed = if (validSamples.isNotEmpty()) validSamples.map { it.speedMbps }.average() else 0.0

    val stabilityColor = when {
        stabilityScore >= 85 -> EmeraldSpeed
        stabilityScore >= 70 -> CyanNeon
        stabilityScore >= 50 -> Color(0xFFFFB300)
        else -> Color(0xFFFF5252)
    }

    val stabilityText = when {
        stabilityScore >= 90 -> "Ultra Stable"
        stabilityScore >= 75 -> "Stable"
        stabilityScore >= 60 -> "Moderate"
        stabilityScore >= 40 -> "Variable"
        else -> "Fluctuating"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
            .testTag("stability_chart_card")
    ) {
        // Top Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CyanNeon.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = "Stability",
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isLive) "Live jitter & throughput telemetry" else "Throughput consistency over time",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stability Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(stabilityColor.copy(alpha = 0.15f))
                    .border(1.dp, stabilityColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(stabilityColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stabilityScore.toInt()}% $stabilityText",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = stabilityColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Graph Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val paddingBottom = 16.dp.toPx()
                val paddingTop = 12.dp.toPx()
                val graphHeight = h - paddingBottom - paddingTop

                // 1. Draw Grid Lines
                val gridLines = 3
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

                for (i in 0..gridLines) {
                    val y = paddingTop + (graphHeight / gridLines) * i
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = dashEffect
                    )
                }

                // 2. Draw Average reference line if samples available
                if (validSamples.size > 2 && avgSpeed > 0) {
                    val avgY = paddingTop + graphHeight - ((avgSpeed / maxSpeed).toFloat() * graphHeight)
                    drawLine(
                        color = CyanGlow.copy(alpha = 0.35f),
                        start = Offset(0f, avgY),
                        end = Offset(w, avgY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    )
                }

                if (validSamples.isEmpty()) {
                    // Empty state subtle flat pulse line
                    val flatY = paddingTop + graphHeight * 0.75f
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(0f, flatY),
                        end = Offset(w, flatY),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    return@Canvas
                }

                // 3. Construct Smooth Curve
                val curvePath = Path()
                val fillPath = Path()

                val stepX = w / max(1, validSamples.size - 1).toFloat()

                val points = validSamples.mapIndexed { index, sample ->
                    val x = index * stepX
                    val normalized = (sample.speedMbps / maxSpeed).coerceIn(0.0, 1.0).toFloat()
                    val y = paddingTop + graphHeight - (normalized * graphHeight)
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    curvePath.moveTo(points[0].x, points[0].y)
                    fillPath.moveTo(points[0].x, paddingTop + graphHeight)
                    fillPath.lineTo(points[0].x, points[0].y)

                    for (i in 0 until points.size - 1) {
                        val p0 = points[i]
                        val p1 = points[i + 1]
                        val controlX = (p0.x + p1.x) / 2f
                        curvePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                        fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                    }

                    val lastPoint = points.last()
                    fillPath.lineTo(lastPoint.x, paddingTop + graphHeight)
                    fillPath.close()

                    // Fill under curve
                    val areaBrush = Brush.verticalGradient(
                        colors = listOf(
                            CyanNeon.copy(alpha = 0.35f),
                            CyanNeon.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = paddingTop,
                        endY = paddingTop + graphHeight
                    )
                    drawPath(fillPath, brush = areaBrush)

                    // Draw line
                    drawPath(
                        curvePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                CyanNeon,
                                if (currentPhase == TestPhase.UPLOAD) EmeraldSpeed else CyanGlow
                            )
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Live Indicator dot at the latest point
                    if (isLive) {
                        // Outer pulsating ring
                        drawCircle(
                            color = CyanNeon.copy(alpha = 0.3f * pulseAlpha),
                            radius = 12.dp.toPx(),
                            center = lastPoint
                        )
                        // Inner ring
                        drawCircle(
                            color = CyanNeon,
                            radius = 5.dp.toPx(),
                            center = lastPoint
                        )
                        // Center white core
                        drawCircle(
                            color = Color.White,
                            radius = 2.5.dp.toPx(),
                            center = lastPoint
                        )
                    }
                }
            }
        }

        // Bottom Stats Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0 Mbps",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Avg: ${String.format(java.util.Locale.US, "%.1f", avgSpeed)} Mbps",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanGlow
                )
            }

            Text(
                text = "${maxSpeed.toInt()} Mbps (Peak)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
