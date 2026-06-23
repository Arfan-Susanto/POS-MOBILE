package com.possaas.Onboarding

import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.possaas.Role.RoleActivity
import com.possaas.R

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager:
            ViewPager2

    private lateinit var dotsLayout:
            LinearLayout

    private lateinit var handler:
            Handler

    private val delay = 8000L

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
            R.layout.activity_onboarding
        )

        viewPager =
            findViewById(R.id.viewPager)

        dotsLayout =
            findViewById(R.id.layoutDots)

        val btnMasuk =
            findViewById<Button>(R.id.btnMasuk)

        btnMasuk.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    RoleActivity::class.java
                )
            )


            finish()
        }

        val list = listOf(

            OnboardingItem(
                R.drawable.slide1,
                "Pilih Menu",
                "Kasir memilih menu yang dipesan pelanggan\n" +
                        "dan menambahkannya ke daftar pesanan."
            ),

            OnboardingItem(
                R.drawable.slide2,
                "Proses Pembayaran",
                "Kasir memproses pembayaran\n" +
                        "sesuai metode yang dipilih\n" +
                        "dan memastikan total transaksi sesuai."
            ),

            OnboardingItem(
                R.drawable.slide3,
                "Hasil Pembayaran",
                "Sistem menampilkan hasil pembayaran\n" +
                        "sebagai bukti transaksi\n" +
                        "yang telah berhasil dilakukan."
            )

        )

        viewPager.adapter =
            OnboardingAdapter(list)
        viewPager.post {
            setupDots(list.size, viewPager.currentItem)
            dotsLayout.visibility = View.VISIBLE
        }

        viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(
                    position: Int
                ) {

                    setupDots(
                        list.size,
                        position
                    )
                }
            }
        )

        handler =
            Handler(Looper.getMainLooper())

        val runnable =
            object : Runnable {

                override fun run() {

                    val next =
                        if (
                            viewPager.currentItem ==
                            list.size - 1
                        ) 0
                        else
                            viewPager.currentItem + 1

                    viewPager.setCurrentItem(
                        next,
                        true
                    )

                    handler.postDelayed(
                        this,
                        delay
                    )
                }
            }

        handler.postDelayed(
            runnable,
            delay
        )
    }

    private fun setupDots(
        count: Int,
        position: Int
    ) {

        dotsLayout.removeAllViews()

        for (i in 0 until count) {

            val dot = View(this)

            val params =
                LinearLayout.LayoutParams(
                    18,
                    18
                )

            params.setMargins(
                8,
                0,
                8,
                0
            )

            dot.layoutParams = params

            dot.setBackgroundResource(
                if (i == position)
                    R.drawable.dot_active
                else
                    R.drawable.dot_inactive
            )

            dotsLayout.addView(dot)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}