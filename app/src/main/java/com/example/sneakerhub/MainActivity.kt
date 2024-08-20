package com.example.sneakerhub

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.sneakerhub.adapters.categoryadapter
import com.example.sneakerhub.adapters.shoeadapter
import com.example.sneakerhub.helpers.ApiHelper
import com.example.sneakerhub.helpers.NetworkHelper
import com.example.sneakerhub.helpers.PrefsHelper
import com.example.sneakerhub.helpers.SQLiteCartHelper
import com.example.sneakerhub.models.category
import com.example.sneakerhub.models.shoes
import com.google.android.material.textview.MaterialTextView
import com.google.gson.GsonBuilder
import com.modcom.medilabsapp.constants.Constants
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private var itemList2: List<shoes> = listOf()
    private lateinit var categoryAdapter: categoryadapter
    private lateinit var shoeAdapter: shoeadapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var secondRecyclerView: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateUserInformation()

        if (!NetworkHelper.checkForInternet(applicationContext)) {
            Toast.makeText(applicationContext, "No Internet", Toast.LENGTH_SHORT).show()
            startActivity(Intent(applicationContext, NoNetActivity::class.java))
            finish()
        } else {
            recyclerView = findViewById(R.id.recycler_view)
            secondRecyclerView = findViewById(R.id.recycler)
            progress = findViewById(R.id.progress)
            swipeRefresh = findViewById(R.id.swipeRefreshLayout)

            setupCategoryRecyclerView()
            setupShoeRecyclerView()
            setupProfileClickListener()
            setupCartClickListener()
            setupSearchFunctionality()

            swipeRefresh.setOnRefreshListener {
                fetchCategories()
            }

            fetchCategories()
        }
    }

    private fun updateUserInformation() {
        val user = findViewById<MaterialTextView>(R.id.user)
        val token = PrefsHelper.getPrefs(applicationContext, "access_token")
        if (token.isEmpty()) {
            user.text = "Not logged in"
            startActivity(Intent(applicationContext, SignIn::class.java))
        } else {
            val surname = PrefsHelper.getPrefs(applicationContext, "surname")
            user.text = surname
        }
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = categoryadapter(applicationContext) { category ->
            fetchShoesByCategory(category.category_id)
        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = categoryAdapter
    }

    private fun setupShoeRecyclerView() {
        shoeAdapter = shoeadapter(this) { shoe ->
            // Handle shoe click if necessary
//             startActivity(Intent(applicationContext, ShoeDetailActivity::class.java))
        }
        secondRecyclerView.layoutManager = GridLayoutManager(this, 2)
        secondRecyclerView.adapter = shoeAdapter
    }

    private fun setupProfileClickListener() {
        val profileLayout = findViewById<RelativeLayout>(R.id.profile)
        profileLayout.setOnClickListener {
            startActivity(Intent(applicationContext, UserProfile::class.java))
        }
    }

    private fun setupCartClickListener() {
        val cartImage = findViewById<ImageView>(R.id.cartimage)
        val badgeText = findViewById<TextView>(R.id.badge)
        cartImage.setOnClickListener {
            startActivity(Intent(applicationContext, MyCart::class.java))
        }
        val helper = SQLiteCartHelper(applicationContext)
        badgeText?.text = helper.getNumItems().toString()
    }

    private fun setupSearchFunctionality() {
        val searchEditText = findViewById<EditText>(R.id.etsearch)
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = text.toString()
                val filteredList = itemList2.filter { it.name.lowercase().contains(searchText.lowercase()) }
                shoeAdapter.filterList(filteredList)
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchCategories() {
        val api = "${Constants.BASE_URL}/shoe_category"
        val helper = ApiHelper(this)
        helper.get(api, object : ApiHelper.CallBack {
            override fun onSuccess(result: JSONArray?) {
                val gson = GsonBuilder().create()
                val categories = gson.fromJson(result.toString(), Array<category>::class.java).toList()
                categoryAdapter.setListItems(categories)
                progress.visibility = View.GONE
                swipeRefresh.isRefreshing = false

                // Fetch shoes for category_id 1 by default
                fetchShoesByCategory("1")
            }

            override fun onSuccess(result: JSONObject?) {
                // Handle JSONObject response if applicable
            }

            override fun onFailure(result: String?) {
                Toast.makeText(applicationContext, "Error: $result", Toast.LENGTH_SHORT).show()
                progress.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }
        })
    }

    private fun fetchShoesByCategory(categoryId: String) {
        val api = "${Constants.BASE_URL}/shoes"
        val body = JSONObject().apply {
            put("category_id", categoryId)
        }
        val helper = ApiHelper(this)
        helper.post(api, body, object : ApiHelper.CallBack {
            override fun onSuccess(result: JSONArray?) {
                // This will not be called because your API returns a JSONObject, not JSONArray
            }

            override fun onSuccess(result: JSONObject?) {
                Log.d("APIResponse", "Raw JSON Result: $result")
                result?.let {
                    try {
                        val shoesArray = it.getJSONArray("shoes")
                        Log.d("APIResponse", "Extracted Shoes Array: $shoesArray")

                        val gson = GsonBuilder().create()
                        val shoesList = gson.fromJson(shoesArray.toString(), Array<shoes>::class.java).toList()
                        Log.d("ShoesList", "Parsed Shoes List Size: ${shoesList.size}")

                        shoeAdapter.setListItems(shoesList)
                        itemList2 = shoesList // Update itemList2 with the fetched data
                        secondRecyclerView.adapter = shoeAdapter
                        progress.visibility = View.GONE
                        swipeRefresh.isRefreshing = false
                    } catch (e: Exception) {
                        Log.e("APIError", "Error parsing shoes data: ${e.message}")
                    }
                }
            }

            override fun onFailure(result: String?) {
                Toast.makeText(applicationContext, "Error: $result", Toast.LENGTH_SHORT).show()
                Log.d("APIError", "Shoes Fetch Error: $result")
                progress.visibility = View.GONE
                swipeRefresh.isRefreshing = false
            }
        })
    }
}
