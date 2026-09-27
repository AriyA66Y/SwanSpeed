package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TestPhase
import com.example.ui.components.NetworkStabilityChart
import com.example.ui.components.ResultCard
import com.example.ui.components.ServerSelectorSheet
import com.example.ui.components.SpeedometerGauge
import com.example.ui.theme.AmberLatency
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.EmeraldSpeed
import com.example.ui.viewmodel.SpeedTestViewModel
import java.util.Locale

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel,
    modifier: Modifier = Modifier
) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val servers by viewModel.servers.collectAsStateWithLifecycle()
    val selectedServer by viewModel.selectedServer.collectAsStateWithLifecycle()
    val connectionType by viewModel.connectionType.collectAsStateWithLifecycle()
    val latestResult by viewModel.latestResult.collectAsStateWithLifecycle()
    val isPingSweeping by viewModel.isPingSweeping.collectAsStateWithLifecycle()

    var showServerSheet by remember { mutableStateOf(false) }

    val isTesting = progress.phase in listOf(
        TestPhase.PING_SWEEP,
        TestPhase.PING,
        TestPhase.DOWNLOAD,
        TestPhase.UPLOAD
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Header & Connection Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Swan",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Speed",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = CyanNeon
                    )
                }
                Text(
                    text = "High-precision internet telemetry",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Connection Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Connection",
                        tint = CyanNeon,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = connectionType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Server Picker Selector Pill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .clickable(enabled = !isTesting) {
                    showServerSheet = true
                }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("server_picker_pill")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Server",
                        tint = CyanNeon,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = selectedServer.flag,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedServer.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = if (isTesting) progress.activeServer.location else selectedServer.location,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = if (isTesting) "Testing..." else "Change >",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CyanNeon
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Center Speedometer Gauge
        SpeedometerGauge(
            speedMbps = progress.currentSpeedMbps,
            phase = progress.phase,
            progressRatio = progress.progressRatio,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        // Live Real-Time Telemetry Metrics Bar (Ping, Jitter, Download, Upload)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                .padding(vertical = 12.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ping
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (progress.pingMs > 0) "${progress.pingMs.toInt()} ms" else "--",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberLatency
                )
            }

            // Jitter
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "JITTER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (progress.jitterMs > 0) "${String.format(Locale.US, "%.1f", progress.jitterMs)} ms" else "--",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Download (Final / Live)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "DOWN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                }
                Text(
                    text = if (progress.downloadSpeedMbps > 0) String.format(Locale.US, "%.1f", progress.downloadSpeedMbps) else "--",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Upload (Final / Live)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = EmeraldSpeed,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "UP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSpeed
                    )
                }
                Text(
                    text = if (progress.uploadSpeedMbps > 0) String.format(Locale.US, "%.1f", progress.uploadSpeedMbps) else "--",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Start / Cancel Test Action Button
        if (!isTesting) {
            Button(
                onClick = { viewModel.startSpeedTest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .testTag("start_speed_test_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanNeon,
                    contentColor = Color(0xFF00363F)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Test",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "START SPEED TEST",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        } else {
            OutlinedButton(
                onClick = { viewModel.cancelSpeedTest() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("cancel_speed_test_button"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF5252)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Cancel",
                    tint = Color(0xFFFF5252),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CANCEL TEST",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Status Message Banner
        Text(
            text = progress.statusMessage,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Network Stability Chart (Visible during test & after test)
        NetworkStabilityChart(
            samples = progress.samples,
            currentPhase = progress.phase,
            stabilityScore = progress.stabilityScore,
            isLive = isTesting,
            title = if (isTesting) "Live Network Stability Graph" else "Network Stability Curve"
        )

        // Result Card on completion with Image Share button
        if (latestResult != null && !isTesting) {
            Spacer(modifier = Modifier.height(20.dp))
            ResultCard(
                result = latestResult!!,
                onTestAgain = { viewModel.startSpeedTest() }
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showServerSheet) {
        ServerSelectorSheet(
            servers = servers,
            selectedServer = selectedServer,
            onSelectServer = { viewModel.selectServer(it) },
            onRefreshPings = { viewModel.sweepServerPings() },
            isPinging = isPingSweeping,
            onDismiss = { showServerSheet = false }
        )
    }
}
