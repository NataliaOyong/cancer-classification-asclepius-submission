package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.databinding.ItemPredictionBinding
import com.dicoding.asclepius.data.Prediction
import com.bumptech.glide.Glide

class HistoryAdapter(private val predictions: List<Prediction>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemPredictionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val prediction = predictions[position]
        holder.bind(prediction)
    }

    override fun getItemCount(): Int = predictions.size

    inner class HistoryViewHolder(private val binding: ItemPredictionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(prediction: Prediction) {
            binding.labelText.text = prediction.label
            binding.scoreText.text = "${prediction.score.toInt()}%"

            val imageUri = Uri.parse(prediction.imageUri)

            Glide.with(binding.root.context)
                .load(imageUri)
                .into(binding.historyImage)
        }
    }
}
