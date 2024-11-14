package com.dicoding.asclepius.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.yalantis.ucrop.UCrop
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val GALLERY_REQUEST_CODE = 123
        private const val UCROP_REQUEST_CODE = 124
    }

    private var tempUri: Uri? = null
    private var originalUri: Uri? = null
    private var isFirstImageSelection = true  // Flag to track the first image selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        // Handle history icon click
        val historyIcon: ImageButton = findViewById(R.id.historyIcon)
        historyIcon.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        // Handle notes icon click
        val notesIcon: ImageButton = findViewById(R.id.notesIcon)
        notesIcon.setOnClickListener {
            val intent = Intent(this, IndoHealthNewsActivity::class.java)
            startActivity(intent)
        }

        viewModel.currentImageUri.observe(this) { uri ->
            uri?.let {
                println("Image URI updated: $uri")
                binding.previewImageView.setImageURI(it)
            }
        }

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.analyzeButton.setOnClickListener { analyzeImage() }
    }

    @Suppress("DEPRECATION")
    private fun startGallery() {
        // TODO:Menentukan galeri untuk memilih gambar
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun showImage() {
        // TODO:Menampilkan gambar sesuai Gallery yang dipilih
        viewModel.currentImageUri.value?.let { uri ->
            binding.previewImageView.setImageURI(uri)
        } ?: showToast("Image not found")
    }

    private fun analyzeImage() {
        // TODO:Menganalisa gambar yang berhasil ditampilkan
        viewModel.currentImageUri.value?.let {
            moveToResultActivity()
        } ?: showToast("Please select an image first")
    }

    private fun moveToResultActivity() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("imageUri", viewModel.currentImageUri.value)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                tempUri = uri
                originalUri = uri
                binding.previewImageView.setImageURI(uri)
                startCrop(uri)
            }
        } else if (requestCode == UCROP_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    val resultUri = UCrop.getOutput(data!!)
                    viewModel.setCurrentImageUri(resultUri)
                    showImage()
                    isFirstImageSelection = false  // Reset flag after first image selection
                }
                UCrop.RESULT_ERROR -> {
                    val cropError = UCrop.getError(data!!)
                    showToast("Crop failed: ${cropError?.message}")
                }
                RESULT_CANCELED -> {
                    if (isFirstImageSelection) {
                        binding.previewImageView.setImageURI(null)
                        isFirstImageSelection = false  // Reset flag after the first cancellation
                    } else {
                        viewModel.currentImageUri.value?.let {
                            binding.previewImageView.setImageURI(it)
                        } ?: showToast("No image to display")
                    }
                }
            }
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "cropped_image.jpg"))
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(800, 800)
            .start(this, UCROP_REQUEST_CODE)
    }
}

