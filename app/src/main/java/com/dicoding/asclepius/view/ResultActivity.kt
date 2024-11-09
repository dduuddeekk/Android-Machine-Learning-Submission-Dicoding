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
        val imageUri = intent.getParcelableExtra<Uri>("IMAGE_URI")
        val prediction = intent.getStringExtra("PREDICTION")
        val confidence = intent.getStringExtra("CONFIDENCE")

        imageUri?.let {
            binding.resultImage.setImageURI(it)
        }

        val resultText = "$prediction ${confidence}%"
        binding.resultText.text = resultText
    }
}
