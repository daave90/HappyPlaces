package pl.daveproject.happyplaces.activity

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
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import pl.daveproject.happyplaces.R
import pl.daveproject.happyplaces.database.HappyPlaceDatabaseHandler
import pl.daveproject.happyplaces.model.HappyPlace
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStorage: Uri? = null
    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var mHappyPlaceDetails: HappyPlace? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)
        createToolbarWithBackButton(null)

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddHappyPlaceActivity,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        dateSetListener = DatePickerDialog
            .OnDateSetListener { view, year, month, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)
        }

        if (mHappyPlaceDetails != null) {
            createToolbarWithBackButton("Edit Happy Place")
            findViewById<AppCompatEditText>(R.id.etTitle).setText(mHappyPlaceDetails!!.title)
            findViewById<AppCompatEditText>(R.id.etDescription).setText(mHappyPlaceDetails!!.description)
            findViewById<AppCompatEditText>(R.id.etDate).setText(mHappyPlaceDetails!!.date)
            findViewById<AppCompatEditText>(R.id.etLocation).setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(mHappyPlaceDetails!!.image)
            findViewById<AppCompatImageView>(R.id.iv_place_image).setImageURI(
                saveImageToInternalStorage
            )
            findViewById<Button>(R.id.btn_save).text = "UPDATE"
        }

        findViewById<AppCompatEditText>(R.id.etDate).setOnClickListener(this)
        findViewById<TextView>(R.id.tv_add_image).setOnClickListener(this)
        findViewById<Button>(R.id.btn_save).setOnClickListener(this)
        findViewById<AppCompatEditText>(R.id.etLocation).setOnClickListener(this)
    }

    private fun createToolbarWithBackButton(title: String?) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_add_place)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            if (title == null) {
                actionBar.title = resources.getString(R.string.addHappyPlaceToolbarTitle)
            } else {
                actionBar.title = title
            }
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.etDate -> displayDatePicker()
            R.id.tv_add_image -> displayPictureDialog()
            R.id.btn_save -> saveHappyPlaceIntoDatabase()
            R.id.etLocation -> startGooglePlacesApiIntent()
        }
    }

    private fun startGooglePlacesApiIntent() {
        try {
            val fields = listOf(
                Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                Place.Field.ADDRESS
            )
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this@AddHappyPlaceActivity)
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveHappyPlaceIntoDatabase() {
        val etTitle = findViewById<AppCompatEditText>(R.id.etTitle)
        val etDescription = findViewById<AppCompatEditText>(R.id.etDescription)
        val etDate = findViewById<AppCompatEditText>(R.id.etDate)
        val etLocation = findViewById<AppCompatEditText>(R.id.etLocation)
        when {
            etTitle.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Please enter title", Toast.LENGTH_LONG).show()
            }
            etDescription.text.isNullOrEmpty() -> {
                Toast.makeText(this, "Please enter description", Toast.LENGTH_LONG).show()
            }
            saveImageToInternalStorage == null -> {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_LONG).show()
            }
            else -> {
                val happyPlace =
                    HappyPlace(
                        if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                        etTitle.text.toString(),
                        saveImageToInternalStorage.toString(),
                        etDescription.text.toString(),
                        etDate.text.toString(),
                        etLocation.text.toString(),
                        mLatitude,
                        mLongitude
                    )

                val dbHandler = HappyPlaceDatabaseHandler(this)
                if (mHappyPlaceDetails == null) {
                    val result = dbHandler.addHappyPlace(happyPlace)
                    if (result > 0) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                } else {
                    val result = dbHandler.updateHappyPlace(happyPlace)
                    if (result > 0) {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
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
            if (requestCode == GALLERY) {
                val contentURI = data.data
                try {
                    val selectedImageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    val imageView = findViewById<AppCompatImageView>(R.id.iv_place_image)
                    saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
                    Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")
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
                saveImageToInternalStorage = saveImageToInternalStorage(thumbNail)
                Log.e("Saved image: ", "Path :: $saveImageToInternalStorage")
                imageView.setImageBitmap(thumbNail)
            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place: Place = Autocomplete.getPlaceFromIntent(data)
                val location = findViewById<AppCompatEditText>(R.id.etLocation)
                location.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
            else {
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

    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}