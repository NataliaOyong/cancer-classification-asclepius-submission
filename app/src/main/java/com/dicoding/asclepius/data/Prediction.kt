package com.dicoding.asclepius.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "predictions")
data class Prediction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val imageUri: String,
    val label: String,
    val score: Float,
    val timestamp: Long = System.currentTimeMillis()
)


