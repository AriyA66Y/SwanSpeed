package com.example.data.repository

import com.example.data.local.SpeedTestDao
import com.example.data.local.SpeedTestEntity
import com.example.data.model.SpeedTestSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SpeedTestRepository(private val dao: SpeedTestDao) {

    val allTests: Flow<List<SpeedTestSummary>> = dao.getAllTests().map { entities ->
        entities.map { it.toSummary() }
    }

    suspend fun saveTest(summary: SpeedTestSummary): Long = withContext(Dispatchers.IO) {
        val entity = SpeedTestEntity.fromSummary(summary)
        dao.insertTest(entity)
    }

    suspend fun deleteTest(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTest(id)
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        dao.clearAll()
    }
}
