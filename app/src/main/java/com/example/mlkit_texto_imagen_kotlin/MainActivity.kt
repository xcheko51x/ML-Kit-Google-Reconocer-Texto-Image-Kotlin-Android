package com.example.mlkit_texto_imagen_kotlin

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mlkit_texto_imagen_kotlin.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var fotPath: String? = null

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted ->

            if (isGranted) {
                captureImage()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            success ->

            if (success) {
                fotPath?.let { path ->
                    val bitmap = BitmapFactory.decodeFile(path)
                    binding.ivImage.setImageBitmap(bitmap)
                    recognizeText(bitmap)
                }
            }
        }

        binding.btnTomarFoto.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

    }

    private fun crearArchivoImagen(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile("JPEG_${timeStamp}_", "jpg", storageDir).apply {
            fotPath = absolutePath
        }
    }

    private fun captureImage() {
        val fotoFile: File? = try {
            crearArchivoImagen()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error ocurred while creating the file", Toast.LENGTH_SHORT).show()
            null
        }

        fotoFile?.also {
            val fotoUri: Uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", it)
            takePictureLauncher.launch(fotoUri)
        }
    }

    private fun recognizeText(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image).addOnSuccessListener { ocrText ->
            binding.tvTextoLeido.text = ocrText.text
            binding.tvTextoLeido.movementMethod = ScrollingMovementMethod()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to recognize text", Toast.LENGTH_SHORT).show()
        }
    }
}