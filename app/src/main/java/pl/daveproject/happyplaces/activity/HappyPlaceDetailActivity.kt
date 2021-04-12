package pl.daveproject.happyplaces.activity

import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import pl.daveproject.happyplaces.R
import pl.daveproject.happyplaces.model.HappyPlace

class HappyPlaceDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_detail)

        var happyPlaceDetail: HappyPlace? = null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            happyPlaceDetail =
                intent.getParcelableExtra(MainActivity.EXTRA_PLACE_DETAILS)!!
        }

        if(happyPlaceDetail != null) {
            createToolbarWithBackButton(happyPlaceDetail)
            bindHappyPlaceDetail(happyPlaceDetail)
        }
    }

    private fun createToolbarWithBackButton(hapyPlaceDetail: HappyPlace) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_happy_place_detail)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = hapyPlaceDetail.title
        }
        toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun bindHappyPlaceDetail(happyPlaceDetail: HappyPlace) {
        val iv_place_image = findViewById<AppCompatImageView>(R.id.iv_place_image)
        val tv_description = findViewById<TextView>(R.id.tv_description)
        val tv_location = findViewById<TextView>(R.id.tv_location)

        iv_place_image.setImageURI(Uri.parse(happyPlaceDetail.image))
        tv_description.text = happyPlaceDetail.description
        tv_location.text = happyPlaceDetail.location
    }
}