package com.dicoding.asclepius.model

import android.net.Uri
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var currentImageUri: Uri? = null
    var prediction: String? = null
    var confidence: String? = null
}