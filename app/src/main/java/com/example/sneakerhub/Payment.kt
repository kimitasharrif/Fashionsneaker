package com.example.sneakerhub

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sneakerhub.helpers.ApiHelper
import com.example.sneakerhub.helpers.PrefsHelper
import com.example.sneakerhub.helpers.SQLiteCartHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.modcom.medilabsapp.constants.Constants
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Random

class Payment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val textpay = findViewById<MaterialTextView>(R.id.textpay)
        val helper = SQLiteCartHelper(applicationContext)
        textpay.text = "Please Pay KES "+helper.totalCost()

        val btnpay = findViewById<MaterialButton>(R.id.btnpay)
        btnpay.setOnClickListener {
            val phone = findViewById<EditText>(R.id.phone)
            val api = Constants.BASE_URL+"/payment"
            val body = JSONObject()
            body.put("phone", phone.text.toString())
            body.put("amount", helper.totalCost())



            val invoice_no = PrefsHelper.getPrefs(this, "invoice_no")
            body.put("invoice_no",invoice_no)

            val apiHelper = ApiHelper(applicationContext)
            apiHelper.post2(api, body, object : ApiHelper.CallBack{
                override fun onSuccess(result: JSONArray?) {

                }

                override fun onSuccess(result: JSONObject?) {
                    //SUccess
                    Toast.makeText(applicationContext, result.toString(),
                        Toast.LENGTH_SHORT).show()
                }

                override fun onFailure(result: String?) {
                    //Failure
                    Toast.makeText(applicationContext,
                        result.toString(), Toast.LENGTH_SHORT).show()
                    //                    val json = JSONObject(result.toString())
                    //                    val msg = json.opt("msg")
                    //                    //TODO
                    //                    if (msg == "Token has Expired"){
                    //                        PrefsHelper.clearPrefs(applicationContext)
                    //                        startActivity(Intent(applicationContext, SignInActivity::class.java))
                    //                        finishAffinity()
                    //                    }
                }
            })
        }
    }

}