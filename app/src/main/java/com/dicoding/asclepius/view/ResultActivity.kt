package com.dicoding.asclepius.view

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        displayResult()
    }

    private fun displayResult() {
        val imageUriString = intent.getStringExtra("IMAGE_URI")
        val prediction = intent.getStringExtra("PREDICTION")
        val confidence = intent.getStringExtra("CONFIDENCE")

        val imageUri = Uri.parse(imageUriString)

        val resultText = "$prediction ${confidence}%"
        binding.resultImage.setImageURI(imageUri)
        binding.resultText.text = resultText
    }
}
