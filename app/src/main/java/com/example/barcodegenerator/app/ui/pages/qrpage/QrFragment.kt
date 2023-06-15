package com.example.barcodegenerator.app.ui.pages.qrpage

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.example.barcodegenerator.R
import com.example.barcodegenerator.app.ui.base.BaseFragment
import com.example.barcodegenerator.databinding.FragmentQrBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by AralBenli on 12.06.2023.
 */
class QrFragment : BaseFragment<FragmentQrBinding>() {
    private var generatedBarcodeImage: Bitmap? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            downloadImage(generatedBarcodeImage!!)
        } else {
            showCustomToast(Status.Warn, "Please allow permission to download", this@QrFragment)
        }
    }

    override fun getViewBinding(): FragmentQrBinding = FragmentQrBinding.inflate(layoutInflater)

    override fun initViews() {
        val typeValue = requireArguments().getInt("type")
        val currentTime = Calendar.getInstance().time
        val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(currentTime)
        val data = requireArguments().getString("edtData")
        val latitude = requireArguments().getString("latitude")
        val longitude = requireArguments().getString("longitude")
        binding.switchPrefer.isChecked = false

        when (typeValue) {
            noLocationValue -> {
                val formattedInfo = "$data"
                generateBarcode(formattedInfo, false)
                binding.switchPrefer.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        generateBarcode(formattedInfo, true)
                    } else {
                        generateBarcode(formattedInfo, false)
                    }
                }
            }
            withLocationValue -> {
                val formattedInfo = """
    Information: $data
    Time: $formattedTime
    Latitude: $latitude
    Longitude: $longitude
""".trimIndent()
                generateBarcode(formattedInfo, false)
                binding.switchPrefer.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        generateBarcode(formattedInfo, true)
                    } else {
                        generateBarcode(formattedInfo, false)
                    }
                }
            }
        }
        closeButton()
        addSaving()
        generatedBarcodeImage?.let { buttonsFunctionality(it) }
    }

    private fun buttonsFunctionality(btm : Bitmap){
        binding.iconDownload.setOnClickListener {
            downloadImage(btm)
        }
        binding.iconShare.setOnClickListener {
            shareImage(btm)
        }
    }

    private fun generateBarcode(combinedData: String?, isBarCode: Boolean) {
        try {
            val writer = if (isBarCode) Code128Writer()  else  QRCodeWriter()
            val format = if (isBarCode) BarcodeFormat.CODE_128  else BarcodeFormat.QR_CODE

            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
            hints[EncodeHintType.MARGIN] = 5 // Adjust the margin as needed

            val content = combinedData?.take(80) // Truncate the content to maximum 80 characters if necessary

            val width = if (isBarCode) 220 else 512
            val height = if (isBarCode) 56 else 512

            val bitMatrix = writer.encode(content, format, width, height, hints)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            generatedBarcodeImage = bmp
            val base64 = convertBitmapToBase64(bmp)
            val decodedByteArray = Base64.decode(base64, Base64.DEFAULT)
            val decodedBitmap =
                BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.size)
            binding.QrImageView.setImageBitmap(decodedBitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT).replace("\n", "")
    }

    private fun addSaving() {
        binding.QrImageView.setOnLongClickListener {
            if (generatedBarcodeImage != null) {
                openDialog(generatedBarcodeImage!!)
                true
            } else {
                false
            }
        }
    }

    private fun openDialog(bitmap: Bitmap) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.custom_dialog, null)

        // Adjust start and end margins of the dialog view
        dialogView.setPadding(8.dpToPx(), 0, 8.dpToPx(), 0)
        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()

        val btnDownload = dialogView.findViewById<Button>(R.id.btnDownload)
        val btnShare = dialogView.findViewById<Button>(R.id.btnShare)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        btnDownload.setOnClickListener {
            requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            dialog.dismiss()
        }

        btnShare.setOnClickListener {
            dialog.dismiss()
            shareImage(bitmap)
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            Handler(Looper.getMainLooper()).postDelayed({
                view?.post {
                    findNavController().navigate(R.id.homeFragment)
                }
            }, 1500)
        }

        dialog.show()
    }

    private fun closeButton() {
        binding.closeButton.setOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }
    }


    private fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    private fun downloadImage(bitmap: Bitmap) {
        val directoryName = "Download"
        val fileName = "barcode_image_${System.currentTimeMillis()}.png"

        val rootDir = Environment.getExternalStorageDirectory()
        val dir = File(rootDir.absolutePath, directoryName)
        dir.mkdirs()

        val file = File(dir, fileName)

        try {
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            showCustomToast(Status.Success, "Barcode image saved", this@QrFragment)

            Handler(Looper.getMainLooper()).postDelayed({
                view?.post {
                    findNavController().navigate(R.id.homeFragment)
                }
            }, 1500)
        } catch (e: IOException) {
            showCustomToast(Status.Fail, "Failed to save barcode image", this@QrFragment)
            Handler(Looper.getMainLooper()).postDelayed({
                view?.post {
                    findNavController().navigate(R.id.homeFragment)
                }
            }, 1500)
        }
    }


    private fun shareImage(bitmap: Bitmap) {
        val file = File.createTempFile("barcode_image_", ".png", requireContext().cacheDir)
        val fileUri = FileProvider.getUriForFile(
            requireContext(), "${requireContext().packageName}.fileprovider", file
        )

        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share Barcode Image"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

