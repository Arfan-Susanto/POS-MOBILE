package com.possaas.Login

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.possaas.R

class LoginKasirActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        // TRANSPARENT STATUS BAR
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        window.statusBarColor =
            Color.TRANSPARENT

        // FULL LAYOUT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(
            R.layout.activity_loginkasir
        )

        val card =
            findViewById<LinearLayout>(
                R.id.cardForm
            )

        val email =
            findViewById<EditText>(
                R.id.edtEmail
            )

        val password =
            findViewById<EditText>(
                R.id.edtPassword
            )

        val btnLogin =
            findViewById<Button>(
                R.id.btnLogin
            )

        // ANIMASI
        val anim =
            AnimationUtils.loadAnimation(
                this,
                R.anim.slide_up_kasir
            )

        card.startAnimation(anim)

        btnLogin.setOnClickListener {

            val emailText =
                email.text.toString().trim()

            val passText =
                password.text.toString().trim()

            if (emailText.isEmpty()) {

                email.error =
                    "Email tidak boleh kosong"

                email.requestFocus()

                return@setOnClickListener
            }

            if (passText.isEmpty()) {

                password.error =
                    "Password tidak boleh kosong"

                password.requestFocus()

                return@setOnClickListener
            }

            // SEMENTARA
            Toast.makeText(
                this,
                "Login Kasir berhasil",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}