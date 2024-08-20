package com.example.sneakerhub

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sneakerhub.helpers.SQLiteCartHelper
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ShoeDetailActivity : AppCompatActivity() {

    private lateinit var shoeImageView: ImageView
    private lateinit var shoeNameTextView: TextView
    private lateinit var shoeDescriptionTextView: TextView
    private lateinit var shoePriceTextView: TextView
    private lateinit var shoeBrandTextView: TextView
    private lateinit var buttonMinus: Button
    private lateinit var sizeSpinner:Spinner
    private lateinit var buttonPlus: Button
    private lateinit var editQuantity: EditText
    private lateinit var orderNowButton: Button
    private lateinit var cartHelper: SQLiteCartHelper

    private var shoeId: String? = null
    private var shoeName: String? = null
    private var shoePrice: String? = null
    private var categoryId: String? = null
    private var description: String? = null
    private var brandName: String? = null
    private var photoUrl: String? = null
    private var selectedSize:String = "6"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shoe_detail)

        // Initialize views
        shoeImageView = findViewById(R.id.shoe_image)
        shoeNameTextView = findViewById(R.id.shoe_name)
        shoeDescriptionTextView = findViewById(R.id.shoe_description)
        shoePriceTextView = findViewById(R.id.shoe_price)
        shoeBrandTextView = findViewById(R.id.shoe_brand)
        buttonMinus = findViewById(R.id.button_minus)
        sizeSpinner = findViewById(R.id.size_spinner)
        buttonPlus = findViewById(R.id.button_plus)
        editQuantity = findViewById(R.id.edit_quantity)
        orderNowButton = findViewById(R.id.order_now_button)
        cartHelper = SQLiteCartHelper(this)

        // Retrieve data from Intent
        shoeId = intent.getStringExtra("shoe_id")
        shoeName = intent.getStringExtra("name")
        shoePrice = intent.getStringExtra("price")
        categoryId = intent.getStringExtra("category_id")
        description = intent.getStringExtra("description")
        brandName = intent.getStringExtra("brand_name")
        photoUrl = intent.getStringExtra("photo_url")

        // Log the photoUrl to check if it's correct
        Log.d("ShoeDetailActivity", "photoUrl: $photoUrl")

        // Set the shoe details to the views
        shoeNameTextView.text = shoeName
        shoeDescriptionTextView.text = description
        shoePriceTextView.text = "KES " + shoePrice
        shoeBrandTextView.text = brandName

        Picasso.get()
            .load(photoUrl) // Replace `photoUrl` with the URL of your image
            .into(shoeImageView, object : Callback {
                override fun onSuccess() {
                    Log.d("ShoeDetailActivity", "Picasso: Image loaded successfully")
                }

                override fun onError(e: Exception?) {
                    Log.e("ShoeDetailActivity", "Picasso: Image load failed", e)
                    Toast.makeText(this@ShoeDetailActivity, "Image load failed: ${e?.message}", Toast.LENGTH_LONG).show()
                }
            })

        // Set initial quantity
        editQuantity.setText("1")

        // Set up size spinner listener
        sizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSize = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // Set up button click listeners
        setupListeners()
    }

    private fun setupListeners() {
        buttonMinus.setOnClickListener {
            updateQuantity(-1)
        }

        buttonPlus.setOnClickListener {
            updateQuantity(1)
        }

        orderNowButton.setOnClickListener {
            val quantity = editQuantity.text.toString().toInt()
            orderNowButton.text = "Please wait..."
            orderNowButton.isEnabled = false
            checkQuantityAndAddToCart(shoeId!!, quantity)
        }
    }

    private fun updateQuantity(change: Int) {
        val currentQuantity = editQuantity.text.toString().toInt()
        val newQuantity = (currentQuantity + change).coerceAtLeast(1) // Ensure quantity is at least 1
        editQuantity.setText(newQuantity.toString())
    }

    private fun checkQuantityAndAddToCart(shoeId: String, quantity: Int) {
        val url = "https://shoeapp2.pythonanywhere.com/shoe_quantity?shoe_id=$shoeId"
        val request = Request.Builder()
            .url(url)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@ShoeDetailActivity, "Error fetching quantity: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse)
                    val availableQuantity = jsonObject.optInt("quantity", -1)

                    runOnUiThread {
                        if (availableQuantity == -1) {
                            Toast.makeText(this@ShoeDetailActivity, "Shoe not found", Toast.LENGTH_SHORT).show()
                        } else if (quantity > availableQuantity) {
                            Toast.makeText(this@ShoeDetailActivity, "Selected quantity exceeds available stock . Available quantity is $availableQuantity", Toast.LENGTH_SHORT).show()
                        } else {
                            // Save the quantity and shoe details using CartHelper
                            cartHelper.insert(
                                shoeId, shoeName!!, shoePrice!!, categoryId!!, description!!, quantity, photoUrl!!
                            )
                            Toast.makeText(this@ShoeDetailActivity, "Added $quantity items to cart", Toast.LENGTH_SHORT).show()
                        }
                        orderNowButton.text = "Add to Cart"
                        // Re-enable the button
                        orderNowButton.isEnabled = true
                    }
                } else {
                    runOnUiThread {
                        orderNowButton.text = "Add to Cart"
                        // Re-enable the button
                        orderNowButton.isEnabled = true
                        Toast.makeText(this@ShoeDetailActivity, "Error fetching quantity: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    override fun onBackPressed() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finishAffinity()
    }
}
