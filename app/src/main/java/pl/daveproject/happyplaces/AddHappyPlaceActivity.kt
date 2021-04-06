package pl.daveproject.happyplaces

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOError
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)
        createToolbarWithBackButton()

        dateSetListener = DatePickerDialog
            .OnDateSetListener { view, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        findViewById<AppCompatEditText>(R.id.etDate).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_add_image).setOnClickListener(this)
    }

    private fun createToolbarWithBackButton() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_add_place)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = resources.getString(R.string.addHappyPlaceToolbarTitle)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.etDate -> displayDatePicker()
            R.id.tv_add_image -> displayPictureDialog()
        }
    }

    private fun displayPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle(resources.getString(R.string.selectAction))
        val pictureDialogItems = arrayOf(
            resources.getString(R.string.selectFromGallery),
            resources.getString(R.string.CaptureFromCamera)
        )
        pictureDialog.setItems(pictureDialogItems) { dialog, which ->
            when (which) {
                0 -> checkPermissionsAndChoosePhotoFromGallery()
                1 -> checkPermissionsAndChoosePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            if(requestCode == GALLERY) {
                val contentURI = data.data
                try {
                    val selectedImageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    val imageView = findViewById<AppCompatImageView>(R.id.iv_place_image)
                    val savedImage = saveImageToInternalStorage(selectedImageBitmap)
                    Log.e("Saved image: ", "Path :: $savedImage")
                    imageView.setImageBitmap(selectedImageBitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@AddHappyPlaceActivity,
                        "Failed retrieve image from gallery",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else if (requestCode == CAMERA) {
                val thumbNail = data.extras!!.get("data") as Bitmap
                val imageView = findViewById<AppCompatImageView>(R.id.iv_place_image)
                val savedImage = saveImageToInternalStorage(thumbNail)
                Log.e("Saved image: ", "Path :: $savedImage")
                imageView.setImageBitmap(thumbNail)
            } else {
                throw Exception("No recognize requested code " + requestCode)
            }
        }
    }

    private fun checkPermissionsAndChoosePhotoFromCamera() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val cameraIntent =
                        Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationalDialogForPermissions()
            }

        })
            .onSameThread()
            .check()
    }

    private fun checkPermissionsAndChoosePhotoFromGallery() {
        Dexter.withContext(this).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {

            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()) {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>,
                token: PermissionToken
            ) {
                showRationalDialogForPermissions()
            }

        })
            .onSameThread()
            .check()
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this).setMessage(resources.getString(R.string.permissionAlertMessage))
            .setPositiveButton(resources.getString(R.string.goToSettings)) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton(resources.getString(R.string.Cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun displayDatePicker() {
        DatePickerDialog(
            this@AddHappyPlaceActivity,
            dateSetListener,
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
            .show()
    }

    private fun updateDateInView() {
        val format = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val dateEditText = findViewById<AppCompatEditText>(R.id.etDate)
        dateEditText.setText(sdf.format(cal.time).toString())
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap):Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try{
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        }catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
    }
}