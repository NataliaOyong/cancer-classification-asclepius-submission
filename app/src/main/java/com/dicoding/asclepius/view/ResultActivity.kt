package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.dicoding.asclepius.databinding.ActivityResultBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.data.AppDatabase
import com.dicoding.asclepius.data.Prediction
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class ResultActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener {
    private lateinit var binding: ActivityResultBinding
    private lateinit var imageUri: Uri
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private lateinit var db: AppDatabase

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        imageUri = intent.getParcelableExtra("imageUri") ?: return showError("No image data received")

        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = this
        )

        db = AppDatabase.getInstance(this)
        loadAndDisplayImage(imageUri)
        classifyImage(imageUri)

        binding.saveButton.setOnClickListener { savePrediction() }
    }

    private fun loadAndDisplayImage(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .signature(ObjectKey(System.currentTimeMillis()))
            .into(binding.resultImage)
    }

    private fun savePrediction() {
        val resultText = binding.resultText.text.toString()
        val label = resultText.substringBeforeLast(" ")
        val scoreText = resultText.substringAfterLast(" ")

        val score = try {
            scoreText.substringBefore("%").toFloat()
        } catch (e: NumberFormatException) {
            0f
        }

        val newImageUri = saveImageToInternalStorage(imageUri)

        lifecycleScope.launch {
            val prediction = Prediction(
                imageUri = newImageUri.toString(),
                label = label,
                score = score
            )

            db.predictionDao().insertPrediction(prediction)
            Toast.makeText(this@ResultActivity, "Prediction saved!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): Uri {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)

            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            return Uri.fromFile(file)
        } catch (e: IOException) {
            showError("Error saving image: ${e.message}")
            throw e
        }
    }

    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            showError("Error loading image: ${e.message}")
            null
        }
    }

    @SuppressLint("DefaultLocale")
    private fun classifyImage(imageUri: Uri) {
        val bitmap = loadBitmapFromUri(imageUri)
        bitmap?.let {
            val results = imageClassifierHelper.classifyStaticImage(imageUri)

            results?.let { (label, score) ->
                val formattedScore = String.format("%.0f", score * 100)
                val resultText = "$label $formattedScore%"
                binding.resultText.text = resultText
            } ?: run {
                showError("Error in image classification")
            }
        } ?: run {
            showError("Error loading image.")
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onError(error: String) {
        showError(error)
    }

    @SuppressLint("DefaultLocale")
    override fun onResults(results: List<org.tensorflow.lite.task.vision.classifier.Classifications>?) {
        results?.let { classifications ->
            val classification = classifications.firstOrNull()

            classification?.let { nonNullClassification ->
                val label = nonNullClassification.categories.firstOrNull()?.label ?: "Unknown"
                val score = nonNullClassification.categories.firstOrNull()?.score ?: 0f
                val formattedScore = String.format("%.0f", score * 100)
                val resultText = "$label $formattedScore%"
                binding.resultText.text = resultText
            }
        } ?: run {
            showError("No classification results found.")
        }
    }
}
