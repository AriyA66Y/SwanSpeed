package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.example.data.model.SpeedTestSummary
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object ResultImageShareHelper {

    fun generateAndShareResultImage(context: Context, result: SpeedTestSummary) {
        try {
            val bitmap = createResultCardBitmap(result)
            val shareDir = File(context.cacheDir, "shared_results")
            if (!shareDir.exists()) {
                shareDir.mkdirs()
            }
            val imageFile = File(shareDir, "swanspeed_result_${result.timestamp}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val authority = "${context.packageName}.fileprovider"
            val contentUri = FileProvider.getUriForFile(context, authority, imageFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Tested my connection with SwanSpeed!\n" +
                    "⚡ Download: ${"%.1f".format(result.downloadMbps)} Mbps\n" +
                    "🚀 Upload: ${"%.1f".format(result.uploadMbps)} Mbps\n" +
                    "⏱ Ping: ${"%.0f".format(result.pingMs)} ms | Jitter: ${"%.1f".format(result.jitterMs)} ms\n" +
                    "📊 Stability: ${"%.0f".format(result.stabilityScore)}% (${result.stabilityGrade})"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share SwanSpeed Result")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createResultCardBitmap(result: SpeedTestSummary): Bitmap {
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Gradient
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                intArrayOf(
                    Color.parseColor("#080D1A"),
                    Color.parseColor("#0F172A"),
                    Color.parseColor("#091024")
                ),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Subtle decorative glow arcs
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.parseColor("#1500E5FF")
        }
        canvas.drawCircle(width * 0.85f, height * 0.15f, 320f, glowPaint)
        canvas.drawCircle(width * 0.15f, height * 0.85f, 280f, glowPaint)

        // 2. Header: Logo & Title
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Swan icon representation
        val swanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            style = Paint.Style.FILL
        }
        val swanPath = Path().apply {
            moveTo(100f, 105f)
            cubicTo(120f, 75f, 150f, 70f, 175f, 60f)
            cubicTo(165f, 85f, 145f, 105f, 110f, 115f)
            close()
        }
        canvas.drawPath(swanPath, swanPaint)

        // Swan Neck
        val neckPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        val neckPath = Path().apply {
            moveTo(100f, 140f)
            cubicTo(105f, 120f, 115f, 100f, 125f, 90f)
            cubicTo(135f, 82f, 145f, 90f, 138f, 110f)
            cubicTo(130f, 125f, 120f, 135f, 110f, 140f)
            close()
        }
        canvas.drawPath(neckPath, neckPaint)

        // App Title
        paint.color = Color.WHITE
        paint.textSize = 52f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SwanSpeed", 185f, 120f, paint)

        // Subtitle / Date
        paint.color = Color.parseColor("#00E5FF")
        paint.textSize = 24f
        paint.typeface = Typeface.DEFAULT
        val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy  •  HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date(result.timestamp))
        canvas.drawText(dateString, 185f, 155f, paint)

        // Connection Badge (Top right)
        val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val badgeRect = RectF(width - 320f, 90f, width - 80f, 150f)
        canvas.drawRoundRect(badgeRect, 30f, 30f, badgeBgPaint)

        val badgeBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(badgeRect, 30f, 30f, badgeBorderPaint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 26f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("📶 ${result.connectionType}", width - 300f, 130f, paint)

        // 3. Server Card
        val serverCardRect = RectF(80f, 195f, width - 80f, 290f)
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#131D33")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(serverCardRect, 24f, 24f, cardBgPaint)

        val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E2D4A")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(serverCardRect, 24f, 24f, cardBorderPaint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("SERVER NODE", 115f, 235f, paint)

        paint.color = Color.WHITE
        paint.textSize = 32f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${result.serverFlag} ${result.serverName}", 115f, 272f, paint)

        paint.color = Color.parseColor("#38BDF8")
        paint.textSize = 26f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(result.serverLocation, width - 120f - paint.measureText(result.serverLocation), 260f, paint)

        // 4. Primary Metrics: Download & Upload (Two large cards side-by-side)
        val cardWidth = (width - 160f - 30f) / 2f

        // Download Card
        val dlRect = RectF(80f, 320f, 80f + cardWidth, 540f)
        canvas.drawRoundRect(dlRect, 28f, 28f, cardBgPaint)
        canvas.drawRoundRect(dlRect, 28f, 28f, cardBorderPaint)

        paint.color = Color.parseColor("#00E5FF")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("↓ DOWNLOAD", 115f, 370f, paint)

        paint.color = Color.WHITE
        paint.textSize = 76f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val dlVal = "%.1f".format(result.downloadMbps)
        canvas.drawText(dlVal, 115f, 460f, paint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 26f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Mbps", 120f + paint.measureText(dlVal) + 40f, 445f, paint)

        paint.color = Color.parseColor("#38BDF8")
        paint.textSize = 22f
        canvas.drawText("Throughput Speed", 115f, 505f, paint)

        // Upload Card
        val ulRect = RectF(80f + cardWidth + 30f, 320f, width - 80f, 540f)
        canvas.drawRoundRect(ulRect, 28f, 28f, cardBgPaint)
        canvas.drawRoundRect(ulRect, 28f, 28f, cardBorderPaint)

        paint.color = Color.parseColor("#00E676")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("↑ UPLOAD", 80f + cardWidth + 65f, 370f, paint)

        paint.color = Color.WHITE
        paint.textSize = 76f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val ulVal = "%.1f".format(result.uploadMbps)
        canvas.drawText(ulVal, 80f + cardWidth + 65f, 460f, paint)

        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 26f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Mbps", 80f + cardWidth + 70f + paint.measureText(ulVal) + 40f, 445f, paint)

        paint.color = Color.parseColor("#4ADE80")
        paint.textSize = 22f
        canvas.drawText("Throughput Speed", 80f + cardWidth + 65f, 505f, paint)

        // 5. Secondary Metrics: Ping, Jitter, Stability
        val metricsRect = RectF(80f, 570f, width - 80f, 700f)
        canvas.drawRoundRect(metricsRect, 24f, 24f, cardBgPaint)
        canvas.drawRoundRect(metricsRect, 24f, 24f, cardBorderPaint)

        val colWidth = (width - 160f) / 3f

        // Column 1: Ping
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("PING LATENCY", 115f, 615f, paint)

        paint.color = Color.WHITE
        paint.textSize = 46f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${"%.0f".format(result.pingMs)} ms", 115f, 665f, paint)

        // Column 2: Jitter
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("JITTER", 115f + colWidth, 615f, paint)

        paint.color = Color.WHITE
        paint.textSize = 46f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${"%.1f".format(result.jitterMs)} ms", 115f + colWidth, 665f, paint)

        // Column 3: Stability
        paint.color = Color.parseColor("#94A3B8")
        paint.textSize = 22f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("STABILITY", 115f + colWidth * 2f, 615f, paint)

        paint.color = Color.parseColor("#38BDF8")
        paint.textSize = 46f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${"%.0f".format(result.stabilityScore)}%", 115f + colWidth * 2f, 665f, paint)

        // 6. Network Stability Chart Box
        val chartBoxRect = RectF(80f, 730f, width - 80f, 1180f)
        canvas.drawRoundRect(chartBoxRect, 28f, 28f, cardBgPaint)
        canvas.drawRoundRect(chartBoxRect, 28f, 28f, cardBorderPaint)

        // Chart Header
        paint.color = Color.WHITE
        paint.textSize = 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Network Stability Graph", 115f, 785f, paint)

        paint.color = Color.parseColor("#00E5FF")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val stabilityBadge = "${result.stabilityGrade} (${"%.0f".format(result.stabilityScore)}%)"
        canvas.drawText(stabilityBadge, width - 120f - paint.measureText(stabilityBadge), 785f, paint)

        // Draw graph area
        drawStabilityGraphOnCanvas(canvas, chartBoxRect, result)

        // 7. Footer Watermark
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 24f
        paint.typeface = Typeface.DEFAULT
        val footerText = "Tested with SwanSpeed • Accurate Network Telemetry"
        canvas.drawText(footerText, (width - paint.measureText(footerText)) / 2f, 1260f, paint)

        return bitmap
    }

    private fun drawStabilityGraphOnCanvas(canvas: Canvas, container: RectF, result: SpeedTestSummary) {
        val graphLeft = container.left + 50f
        val graphRight = container.right - 50f
        val graphTop = container.top + 100f
        val graphBottom = container.bottom - 60f
        val graphHeight = graphBottom - graphTop
        val graphWidth = graphRight - graphLeft

        // Grid lines (3 horizontal levels)
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E2D4A")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 20f
        }

        val maxSpeed = max(10.0, max(result.downloadMbps, result.uploadMbps) * 1.25)

        for (i in 0..3) {
            val y = graphBottom - (graphHeight / 3f) * i
            canvas.drawLine(graphLeft, y, graphRight, y, gridPaint)
            val speedLabel = "${"%.0f".format((maxSpeed / 3.0) * i)} Mbps"
            canvas.drawText(speedLabel, graphLeft + 10f, y - 8f, labelPaint)
        }

        val samples = result.samples
        if (samples.size >= 2) {
            val path = Path()
            val fillPath = Path()

            val stepX = graphWidth / (samples.size - 1).toFloat()

            samples.forEachIndexed { index, sample ->
                val x = graphLeft + index * stepX
                val normalizedSpeed = (sample.speedMbps / maxSpeed).coerceIn(0.0, 1.0).toFloat()
                val y = graphBottom - (normalizedSpeed * graphHeight)

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, graphBottom)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                if (index == samples.size - 1) {
                    fillPath.lineTo(x, graphBottom)
                    fillPath.close()
                }
            }

            // Fill gradient
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, graphTop, 0f, graphBottom,
                    intArrayOf(Color.parseColor("#4400E5FF"), Color.parseColor("#0500E5FF")),
                    null,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
            canvas.drawPath(fillPath, fillPaint)

            // Line curve
            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00E5FF")
                strokeWidth = 5f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            canvas.drawPath(path, linePaint)
        } else {
            // Stylized representative stability wave if sample count is low
            val synthPoints = 12
            val path = Path()
            val fillPath = Path()
            val stepX = graphWidth / (synthPoints - 1).toFloat()
            val baseNormalized = (result.downloadMbps / maxSpeed).coerceIn(0.1, 0.9).toFloat()

            for (i in 0 until synthPoints) {
                val x = graphLeft + i * stepX
                val variation = if (i % 2 == 0) 0.05f else -0.05f
                val y = graphBottom - ((baseNormalized + variation).coerceIn(0.1f, 0.95f) * graphHeight)
                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, graphBottom)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                if (i == synthPoints - 1) {
                    fillPath.lineTo(x, graphBottom)
                    fillPath.close()
                }
            }

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, graphTop, 0f, graphBottom,
                    intArrayOf(Color.parseColor("#4400E5FF"), Color.parseColor("#0500E5FF")),
                    null,
                    Shader.TileMode.CLAMP
                )
                style = Paint.Style.FILL
            }
            canvas.drawPath(fillPath, fillPaint)

            val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00E5FF")
                strokeWidth = 5f
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            canvas.drawPath(path, linePaint)
        }
    }
}
