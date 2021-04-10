package pl.daveproject.happyplaces.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.daveproject.happyplaces.R
import pl.daveproject.happyplaces.database.DatabaseHandler

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.fabAddHappyPlace).setOnClickListener {
            displayAddHappyPlaceActivity()
        }
    }

    private fun displayAddHappyPlaceActivity() {
        val intent = Intent(this, AddHappyPlaceActivity::class.java)
        startActivity(intent)
    }

    private fun getHappyPlacesFromLocalDb(){
        val dbHandler = DatabaseHandler(this)
        val happyPlaces = dbHandler.getHappyPlaces()
    }
}