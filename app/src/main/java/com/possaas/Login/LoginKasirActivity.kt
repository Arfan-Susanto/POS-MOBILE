package com.possaas.Login

import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.*
import android.graphics.Paint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.FirebaseDatabase
import com.possaas.KasirActivity
import com.possaas.R
import com.possaas.SessionManager

class LoginKasirActivity : AppCompatActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        window.statusBarColor =
            Color.TRANSPARENT

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

        val imgTogglePassword =
            findViewById<ImageView>(
                R.id.imgTogglePasswordKasir
            )

        var isPasswordVisible = false

        imgTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            
            if (isPasswordVisible) {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imgTogglePassword.setImageResource(R.drawable.ic_eye_closed)
            } else {
                password.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imgTogglePassword.setImageResource(R.drawable.ic_eye_open)
            }

            val typeface = ResourcesCompat.getFont(this, R.font.plus_jakarta)
            password.typeface = typeface
            
            password.setSelection(password.text.length)
        }

        val btnLogin =
            findViewById<Button>(
                R.id.btnLogin
            )

        val txtWASekarang =
            findViewById<TextView>(
                R.id.txtWASekarang
            )

        txtWASekarang.paintFlags =
            txtWASekarang.paintFlags or
                    Paint.UNDERLINE_TEXT_FLAG

        txtWASekarang.setOnClickListener {
            val phoneNumber =
                "6283112471696"

            val waLink =
                Uri.parse(
                    "https://wa.me/$phoneNumber"
                )

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    waLink
                )

            startActivity(intent)
        }

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

            if (!isInternetAvailable()) {

                Toast.makeText(
                    this,
                    "Tidak ada koneksi internet",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            loginRealtimeDatabase(
                emailText,
                passText
            )
        }
    }

    private fun loginRealtimeDatabase(
        email: String,
        password: String
    ) {

        val ref =
            FirebaseDatabase
                .getInstance()
                .getReference("users")

        ref.get()
            .addOnSuccessListener { snapshot ->

                var loginSuccess = false

                for (userSnapshot in snapshot.children) {

                    val dbEmail =
                        userSnapshot.child("email")
                            .getValue(String::class.java)

                    val dbPassword =
                        userSnapshot.child("password")
                            .getValue(String::class.java)

                    val role =
                        userSnapshot.child("role")
                            .getValue(String::class.java)

                    if (
                        dbEmail == email &&
                        dbPassword == password
                    ) {

                        loginSuccess = true

                        if (role == "KASIR") {

                            Toast.makeText(
                                this,
                                "Login berhasil",
                                Toast.LENGTH_SHORT
                            ).show()

                            val sessionManager = SessionManager(this)
                            sessionManager.saveLoginSession(
                                email = email,
                                password = password,
                                role = "KASIR",
                                uid = userSnapshot.key.toString()
                            )

                            startActivity(
                                Intent(
                                    this,
                                    KasirActivity::class.java
                                )
                            )

                            finish()

                        } else {

                            Toast.makeText(
                                this,
                                "Akun ini bukan kasir",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        break
                    }
                }

                if (!loginSuccess) {

                    Toast.makeText(
                        this,
                        "Email atau password salah",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Gagal mengambil data",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun isInternetAvailable(): Boolean {

        val cm =
            getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            cm.activeNetwork ?: return false

        val capabilities =
            cm.getNetworkCapabilities(network)
                ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
    }
}