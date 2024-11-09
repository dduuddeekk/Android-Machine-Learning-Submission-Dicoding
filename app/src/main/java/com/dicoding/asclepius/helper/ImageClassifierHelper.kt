package com.dicoding.asclepius.helper

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.camera.core.ImageProxy
import com.dicoding.asclepius.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.IOException

class ImageClassifierHelper(
    var threshold: Float = 0.1f,
    var maxResults: Int = 3,
    val context: Context,
    val model: String = "cancer_classification.tflite",
    val classifierListener: ClassifierListener?
) {

    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                model,  // gunakan model sebagai string
                optionsBuilder.build()
            )
        } catch (e: IOException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, "Failed to load model: ${e.message}")
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        if (imageClassifier == null) {
            setupImageClassifier()
        }

        val bitmap = uriToBitmap(imageUri) ?: run {
            classifierListener?.onError("Failed to decode image.")
            return
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .build()

        val tensorImage = TensorImage(DataType.UINT8)
        tensorImage.load(bitmap)

        val processedImage = imageProcessor.process(tensorImage)
        val startTime = System.currentTimeMillis()
        val results = imageClassifier?.classify(processedImage)
        val endTime = System.currentTimeMillis()
        classifierListener?.onResults(results, endTime - startTime)
    }

    private fun uriToBitmap(uri: Uri): Bitmap? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to Bitmap: ${e.message}")
            null
        }
    }

    fun classifyImageProxy(image: ImageProxy) {
        val bitmap = toBitmap(image)
        classifyStaticImage(Uri.parse(bitmap.toString()))  // Pastikan untuk menggunakan URI dengan benar
    }

    private fun toBitmap(image: ImageProxy): Bitmap {
        val bitmapBuffer = Bitmap.createBitmap(
            image.width,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        image.use { bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer) }
        return bitmapBuffer
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(
            results: List<Classifications>?,
            inferenceTime: Long
        )
    }

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }

}