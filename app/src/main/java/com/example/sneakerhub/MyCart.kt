package com.example.sneakerhub

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sneakerhub.adapters.ShoesCartAdapter
import com.example.sneakerhub.helpers.PrefsHelper
import com.example.sneakerhub.helpers.SQLiteCartHelper
import com.example.sneakerhub.models.Item
import com.example.sneakerhub.models.Order
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.modcom.medilabsapp.constants.Constants
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class MyCart : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_my_cart)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        userId = PrefsHelper.getPrefs(applicationContext, "user_id")

        val invoice_no = generateInvoiceNumber()

        //Save Invoice No  to Prefs
        PrefsHelper.savePrefs(applicationContext, "invoice_no", invoice_no)

        val helper = SQLiteCartHelper(applicationContext)
        val checkout = findViewById<MaterialButton>(R.id.checkoutbtn)
        if (helper.totalCost() == 0.0) {
            checkout.visibility = View.GONE
        }

        checkout.setOnClickListener {
            handleCheckout() // Call handleCheckout on click
        }

        val total = findViewById<MaterialTextView>(R.id.totalcost)
        total.text = helper.totalCost().toString()

        val recycler = findViewById<RecyclerView>(R.id.cartrecycler)
        recycler.layoutManager = LinearLayoutManager(applicationContext)
        recycler.setHasFixedSize(true)

        if (helper.getNumItems() == 0) {
            Toast.makeText(applicationContext, "Your cart is empty", Toast.LENGTH_SHORT).show()
        } else {
            val adapter = ShoesCartAdapter(applicationContext)
            adapter.setListItems(helper.getAllItems())
            recycler.adapter = adapter
        }

        // Set up the EditText for location
        val locationEditText = findViewById<MaterialTextView>(R.id.location)
        locationEditText.setOnClickListener {
            requestLocation() // Request location when the EditText is clicked
        }
    }
    //Generate Invoice Number Function
    fun generateInvoiceNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMddHHmmss")
        val currentTime = Date()
        val timestamp = dateFormat.format(currentTime)

        // You can use a random number or a sequential number to add uniqueness to the invoice number
        // For example, using a random number:
        val random = Random()
        val randomNumber = random.nextInt(1000) // Change the upper bound as needed

        // Combine the timestamp and random number to create the invoice number
        return "INV-$timestamp-$randomNumber" //Unique
    }

    fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                123
            )
        } else {
            getLocation() // Get Lat and Lon
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val place = getAddress(LatLng(it.latitude, it.longitude))
                    Toast.makeText(applicationContext, "Here: $place", Toast.LENGTH_SHORT).show()

                    // Update the location EditText with the current location
                    val locationEditText = findViewById<MaterialTextView>(R.id.location)
                    locationEditText.text = "Current Location \n $place"

                    requestNewLocation()
                } ?: run {
                    Toast.makeText(applicationContext, "Searching Location", Toast.LENGTH_SHORT).show()
                    requestNewLocation()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, "Error: $e", Toast.LENGTH_SHORT).show()
                requestNewLocation()
            }
    }

    @SuppressLint("MissingPermission")
    fun requestNewLocation() {
        Log.d("LocationRequest", "Requesting New Location")
        val mLocationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 10000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    if (location != null) {
                        // Save the coordinates in Prefs
                        PrefsHelper.savePrefs(applicationContext, "latitude", location.latitude.toString())
                        PrefsHelper.savePrefs(applicationContext, "longitude", location.longitude.toString())
                    } else {
                        Toast.makeText(applicationContext, "Check GPS", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
    }

    private fun handleCheckout() {
        val helper = SQLiteCartHelper(applicationContext)
        val items = helper.getAllItems().map {
            JSONObject().apply {
                put("shoeId", it.shoe_id)
                put("quantity", it.quantity.toInt())
                put("size", it.size) // Add size information here
            }
        }

        val userIdInt = userId?.toIntOrNull() ?: 0

        val jsonObject = JSONObject().apply {
            put("userId", userIdInt)
            put("items", JSONArray(items))
        }

        val client = OkHttpClient()
        val url = "${Constants.BASE_URL}/create_order"

        // Get the checkout button
        val checkoutButton = findViewById<MaterialButton>(R.id.checkoutbtn)

        // Change button text to "Processing your order..."
        checkoutButton.text = "Processing your order..."
        checkoutButton.isEnabled = false  // Optionally disable the button to prevent further clicks

        val jsonBody = jsonObject.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${PrefsHelper.getPrefs(applicationContext, "access_token")}")
            .post(jsonBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Revert button text to original
                    checkoutButton.text = "Checkout"
                    checkoutButton.isEnabled = true

                    Toast.makeText(applicationContext, "Order creation failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(applicationContext, "Order created successfully", Toast.LENGTH_SHORT).show()

                        // Start PaymentActivity after successful order creation
                        val intent = Intent(this@MyCart, Payment::class.java)
                        startActivity(intent)
                        finish() // Optionally close the current activity
                    } else {
                        val errorBody = response.body?.string()
                        Toast.makeText(applicationContext, "Error: ${response.message}\nDetails: $errorBody", Toast.LENGTH_SHORT).show()
                        Log.e("ResponseError", "Error: ${response.message}\nDetails: $errorBody")
                    }

                    // Revert button text to original
                    checkoutButton.text = "Checkout"
                    checkoutButton.isEnabled = true
                }
            }
        })
    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        val i = Intent(applicationContext, MainActivity::class.java)
        startActivity(i)
        finishAffinity()
    }

    fun getAddress(latlng: LatLng): String {
        val geoCoder = Geocoder(this)
        val list = geoCoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
        return list!![0].getAddressLine(0)
    }
}
