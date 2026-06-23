package com.possaas

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.possaas.Onboarding.OnboardingActivity

class SplashVideoActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_splash_video)

        videoView = findViewById(R.id.videoView)

        val uri = Uri.parse(
            "android.resource://$packageName/${R.raw.logo_pos}"
        )

        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = false
            videoView.start()
        }

        videoView.setOnCompletionListener {

            videoView.stopPlayback()

            val sessionManager = SessionManager(this)
            if (sessionManager.isLoggedIn()) {

                autoLoginUser(sessionManager)

            } else {

                startActivity(
                    Intent(
                        this,
                        OnboardingActivity::class.java
                    )
                )


                finish()
            }
        }

        videoView.setOnErrorListener { _, _, _ ->

            videoView.stopPlayback()

            val sessionManager = SessionManager(this)
            if (sessionManager.isLoggedIn()) {

                autoLoginUser(sessionManager)

            } else {

                startActivity(
                    Intent(
                        this,
                        OnboardingActivity::class.java
                    )
                )


                finish()
            }

            true
        }
    }

    private fun autoLoginUser(sessionManager: SessionManager) {

        val role = sessionManager.getRole()
        val email = sessionManager.getEmail()
        val password = sessionManager.getPassword()

        when (role) {

            "ADMIN" -> {
                autoLoginAdmin(email, password)
            }

            "KASIR" -> {
                autoLoginKasir(email, password)
            }

            else -> {
                goToOnboarding()
            }
        }
    }

    private fun autoLoginAdmin(email: String?, password: String?) {

        if (email == null || password == null) {
            goToOnboarding()
            return
        }

        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                startActivity(
                    Intent(
                        this,
                        AdminActivity::class.java
                    )
                )

                finish()

            } else {

                SessionManager(this).clearSession()
                goToOnboarding()
            }
        }
    }

    private fun autoLoginKasir(email: String?, password: String?) {

        if (email == null || password == null) {
            goToOnboarding()
            return
        }

        val ref = FirebaseDatabase
            .getInstance()
            .getReference("users")

        ref.get()
            .addOnSuccessListener { snapshot ->

                var loginSuccess = false

                for (userSnapshot in snapshot.children) {

                    val dbEmail = userSnapshot.child("email")
                        .getValue(String::class.java)

                    val dbPassword = userSnapshot.child("password")
                        .getValue(String::class.java)

                    val role = userSnapshot.child("role")
                        .getValue(String::class.java)

                    if (
                        dbEmail == email &&
                        dbPassword == password &&
                        role == "KASIR"
                    ) {

                        loginSuccess = true

                        startActivity(
                            Intent(
                                this,
                                KasirActivity::class.java
                            )
                        )

                        finish()

                        break
                    }
                }

                if (!loginSuccess) {

                    SessionManager(this).clearSession()
                    goToOnboarding()
                }
            }
            .addOnFailureListener {

                SessionManager(this).clearSession()
                goToOnboarding()
            }
    }

    private fun goToOnboarding() {

        startActivity(
            Intent(
                this,
                OnboardingActivity::class.java
            )
        )
        
        finish()
    }

    override fun onPause() {
        super.onPause()

        if (::videoView.isInitialized) {
            videoView.suspend()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }
}