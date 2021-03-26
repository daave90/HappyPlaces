package pl.daveproject.happyplaces

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
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
        }
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
}