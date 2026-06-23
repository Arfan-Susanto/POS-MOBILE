package com.possaas

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.possaas.Onboarding.OnboardingActivity

class SplashVideoActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FULLSCREEN
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        // TRANSPARENT STATUS BAR
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // NO LIMIT
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

            startActivity(
                Intent(
                    this,
                    OnboardingActivity::class.java
                )
            )

            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )

            finish()
        }

        videoView.setOnErrorListener { _, _, _ ->

            videoView.stopPlayback()

            startActivity(
                Intent(
                    this,
                    OnboardingActivity::class.java
                )
            )

            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )

            finish()

            true
        }
    }

    override fun onPause() {
        super.onPause()

        if (::videoView.isInitialized) {
            videoView.suspend()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::videoView.isInitialized) {
            videoView.stopPlayback()
        }
    }
}