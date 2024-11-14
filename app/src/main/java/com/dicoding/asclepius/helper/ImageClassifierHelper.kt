package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.IOException

class ImageClassifierHelper(
    private var threshold: Float = 0.1f,
    private var maxResults: Int = 3,
    private val modelName: String = "cancer_classification.tflite",
    val context: Context,
    val classifierListener: ClassifierListener?
) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyStaticImage(imageUri: Uri): Pair<String, Float>? {
        // TODO:Mengklasifikasikan imageUri dari gambar statis
        return try {
            val bitmap = loadBitmapFromUri(imageUri)
            val tensorImage = TensorImage.fromBitmap(bitmap)

            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
                .build()

            val processedImage = imageProcessor.process(tensorImage)
            val results = imageClassifier?.classify(processedImage)?.firstOrNull()

            results?.let {
                val category = it.categories.firstOrNull()
                category?.let { classification ->
                    Pair(classification.label ?: "Unknown", classification.score)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error loading image: ${e.message}")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Classification error: ${e.message}")
            null
        }
    }

    @Suppress("DEPRECATION")
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>?)
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }
}
