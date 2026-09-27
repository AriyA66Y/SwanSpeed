package com.example.data.model

enum class TestPhase {
    IDLE,
    PING_SWEEP,
    PING,
    DOWNLOAD,
    UPLOAD,
    FINISHED,
    CANCELLED,
    ERROR
}

data class SpeedSample(
    val elapsedMs: Long,
    val speedMbps: Double,
    val phase: TestPhase
)

data class SpeedTestProgress(
    val phase: TestPhase = TestPhase.IDLE,
    val currentSpeedMbps: Double = 0.0,
    val peakDownloadMbps: Double = 0.0,
    val peakUploadMbps: Double = 0.0,
    val progressRatio: Float = 0f,
    val pingMs: Double = 0.0,
    val jitterMs: Double = 0.0,
    val downloadSpeedMbps: Double = 0.0,
    val uploadSpeedMbps: Double = 0.0,
    val stabilityScore: Double = 100.0,
    val statusMessage: String = "Ready",
    val activeServer: ServerInfo = ServerInfo.AUTO,
    val samples: List<SpeedSample> = emptyList(),
    val errorMessage: String? = null
)

data class SpeedTestSummary(
    val id: Long = 0,
    val serverName: String,
    val serverLocation: String,
    val serverFlag: String,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val pingMs: Double,
    val jitterMs: Double,
    val stabilityScore: Double,
    val stabilityGrade: String,
    val timestamp: Long,
    val connectionType: String,
    val samples: List<SpeedSample> = emptyList()
) {
    companion object {
        fun calculateStabilityGrade(score: Double): String {
            return when {
                score >= 90 -> "Ultra Stable"
                score >= 75 -> "Stable"
                score >= 60 -> "Moderate"
                score >= 40 -> "Variable"
                else -> "Unstable"
            }
        }
    }
}
