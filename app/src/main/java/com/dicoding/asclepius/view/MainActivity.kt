package com.dicoding.asclepius.view

import android.app.Activity
import android.content.Intent
import android.icu.text.DecimalFormat
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.asclepius.helper.ImageClassifierHelper.ClassifierListener
import com.dicoding.asclepius.model.MainViewModel
import org.tensorflow.lite.task.vision.classifier.Classifications

class MainActivity : AppCompatActivity(), ClassifierListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        imageClassifierHelper = ImageClassifierHelper(context = this, classifierListener = this)

        mainViewModel.currentImageUri.observe(this) { uri ->
            showImage(uri)
        }

        binding.apply {
            galleryButton.setOnClickListener { startGallery() }
            analyzeButton.setOnClickListener { analyzeImage() }
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                mainViewModel.setImageUri(uri)
            } else {
                showToast("Failed to pick image.")
            }
        } else {
            showToast("Image selection cancelled.")
        }
    }

    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun showImage(uri: Uri?) {
        uri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        mainViewModel.currentImageUri.value?.let { uri ->
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
                mainViewModel.setPrediction(category.label, confidence.toString())
                moveToResult(category.label, confidence)
            }
        }
    }

    private fun moveToResult(prediction: String, confidence: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("PREDICTION", prediction)
        intent.putExtra("CONFIDENCE", confidence)
        mainViewModel.currentImageUri.value?.let { uri ->
            intent.putExtra("IMAGE_URI", uri.toString())
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
