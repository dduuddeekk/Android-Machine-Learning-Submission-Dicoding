package com.dicoding.asclepius.model

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _currentImageUri = MutableLiveData<Uri?>(null)
    val currentImageUri: LiveData<Uri?> get() = _currentImageUri

    private val _prediction = MutableLiveData<String?>()
//    val prediction: LiveData<String?> get() = _prediction // commented because no use

    private val _confidence = MutableLiveData<String?>()
//    val confidence: LiveData<String?> get() = _confidence // commented because no use

    fun setImageUri(uri: Uri) {
        _currentImageUri.value = uri
    }

    fun setPrediction(prediction: String, confidence: String) {
        _prediction.value = prediction
        _confidence.value = confidence
    }
}
