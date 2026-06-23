package com.possaas.Role

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.possaas.Login.LoginAdminActivity
import com.possaas.Login.LoginKasirActivity
import com.possaas.R

class RoleActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        window.statusBarColor = Color.TRANSPARENT

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_role)

        val cardAdmin =
            findViewById<RelativeLayout>(R.id.cardAdmin)

        val cardKasir =
            findViewById<RelativeLayout>(R.id.cardKasir)

        cardAdmin.setClickAnimation()
        cardKasir.setClickAnimation()

        cardAdmin.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    LoginAdminActivity::class.java
                )
            )
        }

        cardKasir.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    LoginKasirActivity::class.java
                )
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}

fun View.setClickAnimation() {

    this.setOnTouchListener { v, event ->

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                v.animate()
                    .scaleX(0.96f)
                    .scaleY(0.96f)
                    .setDuration(100)
                    .start()
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {

                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
        }

        false
    }
}