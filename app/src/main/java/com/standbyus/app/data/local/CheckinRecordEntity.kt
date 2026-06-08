package com.standbyus.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.standbyus.app.data.model.CheckinData

@Entity(
    tableName = "checkin_records",
    indices = [
        Index(value = ["userId", "timestamp"], unique = true)
    ]
)
data class CheckinRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val timestamp: Long,
    val note: String = ""
)

fun CheckinRecordEntity.toData(): CheckinData = CheckinData(
    id = id,
    userId = userId,
    timestamp = timestamp,
    note = note
)

fun CheckinData.toEntity(): CheckinRecordEntity = CheckinRecordEntity(
    userId = userId,
    timestamp = timestamp,
    note = note
)
