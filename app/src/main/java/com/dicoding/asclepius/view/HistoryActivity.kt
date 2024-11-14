package com.dicoding.asclepius.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.databinding.ActivityHistoryBinding
import com.dicoding.asclepius.data.AppDatabase

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var db: AppDatabase

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database instance
        db = AppDatabase.getInstance(this)
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)

        // Setup toolbar with back button
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        db.predictionDao().getAllPredictions().observe(this) { predictions ->
            binding.historyRecyclerView.adapter = HistoryAdapter(predictions)
        }
    }
}
