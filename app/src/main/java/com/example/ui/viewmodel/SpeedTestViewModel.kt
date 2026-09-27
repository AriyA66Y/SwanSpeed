package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.SpeedTestDatabase
import com.example.data.model.ServerInfo
import com.example.data.model.SpeedTestProgress
import com.example.data.model.SpeedTestSummary
import com.example.data.model.TestPhase
import com.example.data.repository.SpeedTestRepository
import com.example.network.NetworkDetector
import com.example.network.SpeedTestEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpeedTestViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SpeedTestRepository
    private val engine = SpeedTestEngine()

    private val _progress = MutableStateFlow(SpeedTestProgress())
    val progress: StateFlow<SpeedTestProgress> = _progress.asStateFlow()

    private val _servers = MutableStateFlow(ServerInfo.DEFAULT_SERVERS)
    val servers: StateFlow<List<ServerInfo>> = _servers.asStateFlow()

    private val _selectedServer = MutableStateFlow(ServerInfo.AUTO)
    val selectedServer: StateFlow<ServerInfo> = _selectedServer.asStateFlow()

    private val _connectionType = MutableStateFlow("Detecting...")
    val connectionType: StateFlow<String> = _connectionType.asStateFlow()

    private val _latestResult = MutableStateFlow<SpeedTestSummary?>(null)
    val latestResult: StateFlow<SpeedTestSummary?> = _latestResult.asStateFlow()

    private val _isPingSweeping = MutableStateFlow(false)
    val isPingSweeping: StateFlow<Boolean> = _isPingSweeping.asStateFlow()

    private val _selectedHistoryItem = MutableStateFlow<SpeedTestSummary?>(null)
    val selectedHistoryItem: StateFlow<SpeedTestSummary?> = _selectedHistoryItem.asStateFlow()

    val historyList: StateFlow<List<SpeedTestSummary>>

    private var testJob: Job? = null

    init {
        val db = SpeedTestDatabase.getDatabase(application)
        repository = SpeedTestRepository(db.speedTestDao())

        historyList = repository.allTests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        refreshConnectionType()
        sweepServerPings()
    }

    fun refreshConnectionType() {
        val conn = NetworkDetector.getConnectionType(getApplication())
        _connectionType.value = conn
    }

    fun selectServer(server: ServerInfo) {
        _selectedServer.value = server
        _progress.value = _progress.value.copy(activeServer = server)
    }

    fun sweepServerPings() {
        if (_isPingSweeping.value) return
        viewModelScope.launch {
            _isPingSweeping.value = true
            try {
                val pingMap = engine.sweepAllServers()
                _servers.value = _servers.value.map { srv ->
                    if (srv.id in pingMap) {
                        srv.copy(lastPingMs = pingMap[srv.id])
                    } else {
                        srv
                    }
                }
            } catch (e: Exception) {
                // Ignore transient sweep errors
            } finally {
                _isPingSweeping.value = false
            }
        }
    }

    fun startSpeedTest() {
        if (_progress.value.phase in listOf(TestPhase.PING_SWEEP, TestPhase.PING, TestPhase.DOWNLOAD, TestPhase.UPLOAD)) {
            return
        }

        refreshConnectionType()
        testJob?.cancel()

        testJob = viewModelScope.launch {
            val server = _selectedServer.value
            val conn = _connectionType.value

            _latestResult.value = null

            engine.runSpeedTest(server, conn).collect { update ->
                _progress.value = update

                if (update.phase == TestPhase.FINISHED) {
                    val summary = SpeedTestSummary(
                        serverName = update.activeServer.name,
                        serverLocation = update.activeServer.location,
                        serverFlag = update.activeServer.flag,
                        downloadMbps = update.downloadSpeedMbps,
                        uploadMbps = update.uploadSpeedMbps,
                        pingMs = update.pingMs,
                        jitterMs = update.jitterMs,
                        stabilityScore = update.stabilityScore,
                        stabilityGrade = SpeedTestSummary.calculateStabilityGrade(update.stabilityScore),
                        timestamp = System.currentTimeMillis(),
                        connectionType = conn,
                        samples = update.samples
                    )
                    _latestResult.value = summary

                    // Persist to Room database
                    val id = repository.saveTest(summary)
                    _latestResult.value = summary.copy(id = id)
                }
            }
        }
    }

    fun cancelSpeedTest() {
        testJob?.cancel()
        _progress.value = SpeedTestProgress(
            phase = TestPhase.CANCELLED,
            statusMessage = "Test cancelled"
        )
    }

    fun selectHistoryForDetail(summary: SpeedTestSummary?) {
        _selectedHistoryItem.value = summary
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteTest(id)
            if (_selectedHistoryItem.value?.id == id) {
                _selectedHistoryItem.value = null
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAll()
            _selectedHistoryItem.value = null
        }
    }
}
