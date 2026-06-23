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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.possaas.AdminActivity
import com.possaas.R
import com.possaas.SessionManager

class LoginAdminActivity : AppCompatActivity() {

    private lateinit var auth:
            FirebaseAuth

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
            R.layout.activity_loginadmin
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
                R.id.imgTogglePasswordAdmin
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

        auth =
            FirebaseAuth.getInstance()

        val anim =
            AnimationUtils.loadAnimation(
                this,
                R.anim.slide_up_admin
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

            loginFirebase(
                emailText,
                passText
            )
        }
    }

    private fun loginFirebase(
        email: String,
        password: String
    ) {

        auth.signInWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val uid =
                    auth.currentUser?.uid

                if (uid == null) {

                    Toast.makeText(
                        this,
                        "Terjadi kesalahan, coba lagi",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@addOnCompleteListener
                }

                val ref =
                    FirebaseDatabase
                        .getInstance()
                        .getReference("users")
                        .child(uid)

                ref.get()
                    .addOnSuccessListener { snapshot ->

                        if (!snapshot.exists()) {

                            Toast.makeText(
                                this,
                                "Data user tidak ditemukan",
                                Toast.LENGTH_LONG
                            ).show()

                            auth.signOut()

                            return@addOnSuccessListener
                        }

                        val role =
                            snapshot.child("role")
                                .getValue(String::class.java)

                        if (role == "ADMIN") {

                            Toast.makeText(
                                this,
                                "Login berhasil",
                                Toast.LENGTH_SHORT
                            ).show()

                            val sessionManager = SessionManager(this)
                            sessionManager.saveLoginSession(
                                email = email,
                                password = password,
                                role = "ADMIN",
                                uid = uid
                            )

                            startActivity(
                                Intent(
                                    this,
                                    AdminActivity::class.java
                                )
                            )

                            finish()

                        } else {

                            Toast.makeText(
                                this,
                                "Akun ini bukan admin",
                                Toast.LENGTH_SHORT
                            ).show()

                            auth.signOut()
                        }

                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            "Gagal mengambil data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            } else {

                Toast.makeText(
                    this,
                    "Email atau password salah",
                    Toast.LENGTH_SHORT
                ).show()
            }
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