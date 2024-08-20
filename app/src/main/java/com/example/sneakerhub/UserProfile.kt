package com.example.sneakerhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sneakerhub.helpers.PrefsHelper
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

class UserProfile : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val name = findViewById<MaterialTextView>(R.id.profilesurname)
        val email = findViewById<MaterialTextView>(R.id.profileemail)

        val surname = PrefsHelper.getPrefs(applicationContext, "surname")
        val pemail = PrefsHelper.getPrefs(applicationContext,"email")
        name.text = surname
        email.text = pemail

        val logout = findViewById<MaterialButton>(R.id.logout)
        logout.setOnClickListener {
            PrefsHelper.clearPrefs(applicationContext)
            startActivity(Intent(applicationContext, SignIn::class.java))
            finishAffinity()
        }

    }
}