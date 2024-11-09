package com.dicoding.asclepius.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.helper.ImageClassifierHelper.ClassifierListener
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity(), ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private var currentImageUri: Uri? = null
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageClassifierHelper = ImageClassifierHelper(context = this, classifierListener = this)

        binding.apply {
            galleryButton.setOnClickListener { startGallery() }
            analyzeButton.setOnClickListener { analyzeImage() }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentImageUri = result.data?.data
            showImage()
        } else {
            showToast("Failed to pick image.")
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun showImage() {
        currentImageUri?.let { uri ->
            binding.previewImageView.setImageURI(uri)
        }
    }

    private fun analyzeImage() {
        currentImageUri?.let { uri ->
            imageClassifierHelper.classifyStaticImage(uri)
        } ?: showToast("Please select an image first.")
    }

    override fun onError(error: String) {
        showToast(error)
    }

    override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
        results?.let {
            val topResult = it.firstOrNull()?.categories?.firstOrNull()
            topResult?.let { category ->
                val confidence = DecimalFormat("#.00").format(category.score * 100)
                moveToResult(category.label.toString(), confidence.toString())
            }
        }
    }

    private fun moveToResult(prediction: String, confidence: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("PREDICTION", prediction)
        intent.putExtra("CONFIDENCE", confidence)
        currentImageUri?.let { uri ->
            intent.putExtra("IMAGE_URI", uri.toString())
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
