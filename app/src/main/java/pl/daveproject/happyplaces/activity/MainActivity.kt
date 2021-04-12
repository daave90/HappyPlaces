package pl.daveproject.happyplaces.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import pl.daveproject.happyplaces.R
import pl.daveproject.happyplaces.adapter.HappyPlaceAdapter
import pl.daveproject.happyplaces.database.HappyPlaceDatabaseHandler
import pl.daveproject.happyplaces.model.HappyPlace

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<FloatingActionButton>(R.id.fabAddHappyPlace).setOnClickListener {
            displayAddHappyPlaceActivity()
        }
        getHappyPlacesFromLocalDb()
    }

    private fun displayAddHappyPlaceActivity() {
        val intent = Intent(this, AddHappyPlaceActivity::class.java)
        startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
    }

    private fun getHappyPlacesFromLocalDb() {
        val dbHandler = HappyPlaceDatabaseHandler(this)
        val happyPlaces = dbHandler.getHappyPlaces()
        if (happyPlaces.isNotEmpty()) {
            val recyclerView = findViewById<RecyclerView>(R.id.rvHappyPlacesList)
            recyclerView.visibility = View.VISIBLE
            val noRecordsTv = findViewById<TextView>(R.id.tvNoRecordsAvailable)
            noRecordsTv.visibility = View.GONE
            setupHappyPlacesRecyclerView(happyPlaces)
        } else {
            val recyclerView = findViewById<RecyclerView>(R.id.rvHappyPlacesList)
            recyclerView.visibility = View.GONE
            val noRecordsTv = findViewById<TextView>(R.id.tvNoRecordsAvailable)
            noRecordsTv.visibility = View.VISIBLE
        }
    }

    private fun setupHappyPlacesRecyclerView(happyPlaces: List<HappyPlace>) {
        val recyclerView = findViewById<RecyclerView>(R.id.rvHappyPlacesList)
        val adapter = HappyPlaceAdapter(this, happyPlaces)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        adapter.setOnClickListener(object: HappyPlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlace) {
                val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
                startActivity(intent)
            }
        })
        recyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            getHappyPlacesFromLocalDb()
        } else {
            Log.e("Activity", "Cancelled or back pressed")
        }
    }

    companion object {
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }
}