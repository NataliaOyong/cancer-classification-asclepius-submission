package com.dicoding.asclepius.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PredictionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: Prediction): Long

    @Query("SELECT * FROM predictions ORDER BY timestamp DESC")
    fun getAllPredictions(): LiveData<List<Prediction>>
}

