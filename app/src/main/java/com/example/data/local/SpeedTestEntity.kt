package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.SpeedSample
import com.example.data.model.SpeedTestSummary
import com.example.data.model.TestPhase

@Entity(tableName = "speed_tests")
data class SpeedTestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val serverName: String,
    val serverLocation: String,
    val serverFlag: String,
    val downloadSpeedMbps: Double,
    val uploadSpeedMbps: Double,
    val pingMs: Double,
    val jitterMs: Double,
    val stabilityScore: Double,
    val stabilityGrade: String,
    val connectionType: String,
    val samplesCsv: String = ""
) {
    fun toSummary(): SpeedTestSummary {
        val sampleList = if (samplesCsv.isBlank()) {
            emptyList()
        } else {
            samplesCsv.split(";").mapNotNull { entry ->
                val parts = entry.split(",")
                if (parts.size >= 3) {
                    val ms = parts[0].toLongOrNull() ?: 0L
                    val speed = parts[1].toDoubleOrNull() ?: 0.0
                    val phase = try {
                        TestPhase.valueOf(parts[2])
                    } catch (e: Exception) {
                        TestPhase.DOWNLOAD
                    }
                    SpeedSample(ms, speed, phase)
                } else null
            }
        }

        return SpeedTestSummary(
            id = id,
            serverName = serverName,
            serverLocation = serverLocation,
            serverFlag = serverFlag,
            downloadMbps = downloadSpeedMbps,
            uploadMbps = uploadSpeedMbps,
            pingMs = pingMs,
            jitterMs = jitterMs,
            stabilityScore = stabilityScore,
            stabilityGrade = stabilityGrade,
            timestamp = timestamp,
            connectionType = connectionType,
            samples = sampleList
        )
    }

    companion object {
        fun fromSummary(summary: SpeedTestSummary): SpeedTestEntity {
            val csv = summary.samples.take(200).joinToString(";") {
                "${it.elapsedMs},${"%.2f".format(it.speedMbps)},${it.phase.name}"
            }
            return SpeedTestEntity(
                id = summary.id,
                timestamp = summary.timestamp,
                serverName = summary.serverName,
                serverLocation = summary.serverLocation,
                serverFlag = summary.serverFlag,
                downloadSpeedMbps = summary.downloadMbps,
                uploadSpeedMbps = summary.uploadMbps,
                pingMs = summary.pingMs,
                jitterMs = summary.jitterMs,
                stabilityScore = summary.stabilityScore,
                stabilityGrade = summary.stabilityGrade,
                connectionType = summary.connectionType,
                samplesCsv = csv
            )
        }
    }
}
