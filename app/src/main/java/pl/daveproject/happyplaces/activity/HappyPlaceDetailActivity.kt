package pl.daveproject.happyplaces.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import pl.daveproject.happyplaces.R

class HappyPlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)
        createToolbarWithBackButton()
    }

    private fun createToolbarWithBackButton() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_happy_place_detail)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = resources.getString(R.string.happyPlaceDetailToolbarTitle)
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }
}