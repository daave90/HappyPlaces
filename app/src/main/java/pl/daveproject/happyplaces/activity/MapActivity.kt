package pl.daveproject.happyplaces.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import pl.daveproject.happyplaces.R

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        createToolbarWithBackButton()

    }

    private fun createToolbarWithBackButton() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_map)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = resources.getString(R.string.happyPlaceMapToolbarTitle)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}