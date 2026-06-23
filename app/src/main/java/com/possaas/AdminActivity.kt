    package com.possaas

    import android.animation.AnimatorSet
    import android.animation.ObjectAnimator
    import android.graphics.Color
    import android.os.Bundle
    import android.view.View
    import android.view.WindowManager
    import android.view.animation.AccelerateDecelerateInterpolator
    import android.widget.ImageView
    import android.widget.LinearLayout
    import android.widget.TextView
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.content.ContextCompat
    import androidx.core.content.res.ResourcesCompat
    import androidx.fragment.app.Fragment
    import com.possaas.Admin.HomeAdminFragment
    import com.possaas.Admin.ProfileAdminFragment

    class AdminActivity : AppCompatActivity() {

        private lateinit var txtSplash: TextView
        private lateinit var splashBg: View
        private lateinit var viewCircle: View
        private lateinit var imgSplashIcon: ImageView
        private lateinit var navHome: LinearLayout
        private lateinit var navProfile: LinearLayout
        private lateinit var textHome: TextView
        private lateinit var textProfile: TextView
        private lateinit var iconHome: ImageView
        private lateinit var iconProfile: ImageView
        private var currentMenu = 1
        private var isSplashRunning = false
        private var isBackPressedOnce = false

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
                R.layout.activity_admin
            )

            txtSplash =
                findViewById(R.id.txtSplash)

            splashBg =
                findViewById(R.id.viewSplash)

            viewCircle =
                findViewById(R.id.viewCircle)

            imgSplashIcon =
                findViewById(R.id.imgSplashIcon)

            navHome =
                findViewById(R.id.navHome)

            navProfile =
                findViewById(R.id.navProfile)

            textHome =
                findViewById(R.id.textHome)

            textProfile =
                findViewById(R.id.textProfile)

            iconHome =
                findViewById(R.id.iconHome)

            iconProfile =
                findViewById(R.id.iconProfile)

            loadFragment(
                HomeAdminFragment()
            )

            setActiveState(
                textHome,
                iconHome
            )

            navHome.setOnClickListener {

                if (currentMenu == 1 || isSplashRunning)
                    return@setOnClickListener

                currentMenu = 1

                switchMenu(
                    splash = "HOME",
                    activeText = textHome,
                    activeIcon = iconHome,
                    inactiveText = textProfile,
                    inactiveIcon = iconProfile,
                    fragment = HomeAdminFragment()
                )
            }

            navProfile.setOnClickListener {

                if (currentMenu == 2 || isSplashRunning)
                    return@setOnClickListener

                currentMenu = 2

                switchMenu(
                    splash = "PROFILE",
                    activeText = textProfile,
                    activeIcon = iconProfile,
                    inactiveText = textHome,
                    inactiveIcon = iconHome,
                    fragment = ProfileAdminFragment()
                )
            }
        }

        private fun switchMenu(
            splash: String,
            activeText: TextView,
            activeIcon: ImageView,
            inactiveText: TextView,
            inactiveIcon: ImageView,
            fragment: Fragment
        ) {

            isSplashRunning = true

            animateSelected(
                activeText,
                activeIcon
            )

            animateUnselected(
                inactiveText,
                inactiveIcon
            )

            playSplash(
                splash
            ) {

                loadFragment(fragment)

                isSplashRunning = false
            }
        }

        private fun setActiveState(
            text: TextView,
            icon: ImageView
        ) {

            text.visibility = View.VISIBLE
            text.alpha = 1f

            icon.translationY = -4f
        }

        private fun animateSelected(
            text: TextView,
            icon: ImageView
        ) {

            text.visibility = View.VISIBLE

            text.alpha = 0f
            text.translationY = 12f

            icon.animate()
                .translationY(-4f)
                .setDuration(220)
                .start()

            text.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(250)
                .start()
        }

        private fun animateUnselected(
            text: TextView,
            icon: ImageView
        ) {

            icon.animate()
                .translationY(0f)
                .setDuration(200)
                .start()

            text.animate()
                .translationY(10f)
                .alpha(0f)
                .setDuration(180)
                .withEndAction {

                    text.visibility = View.GONE
                }
                .start()
        }

        private fun playSplash(
            text: String,
            onFinish: () -> Unit
        ) {

            txtSplash.text = text

            txtSplash.typeface =
                ResourcesCompat.getFont(
                    this,
                    R.font.plus_jakarta
                )

            txtSplash.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.accent
                )
            )

            if (text == "HOME") {

                imgSplashIcon.setImageResource(
                    R.drawable.ic_home
                )

            } else {

                imgSplashIcon.setImageResource(
                    R.drawable.ic_profile
                )
            }

            splashBg.visibility = View.VISIBLE
            viewCircle.visibility = View.VISIBLE
            imgSplashIcon.visibility = View.VISIBLE
            txtSplash.visibility = View.VISIBLE

            val centerX =
                resources.displayMetrics.widthPixels / 2f

            val centerY =
                resources.displayMetrics.heightPixels / 2f

            viewCircle.post {

                viewCircle.x =
                    centerX - (viewCircle.width / 2f)

                viewCircle.y =
                    centerY - (viewCircle.height / 2f)

                imgSplashIcon.x =
                    centerX - (imgSplashIcon.width / 2f)

                imgSplashIcon.y =
                    centerY - 180f

                txtSplash.x =
                    centerX - (txtSplash.width / 2f)

                txtSplash.y =
                    centerY - 40f
            }

            splashBg.alpha = 0f

            viewCircle.alpha = 0f
            viewCircle.scaleX = 1f
            viewCircle.scaleY = 1f

            imgSplashIcon.alpha = 0f
            imgSplashIcon.scaleX = 0.6f
            imgSplashIcon.scaleY = 0.6f

            txtSplash.alpha = 0f
            txtSplash.scaleX = 0.7f
            txtSplash.scaleY = 0.7f

            viewCircle.animate()
                .alpha(1f)
                .setDuration(120)
                .withEndAction {

                    val scaleX =
                        ObjectAnimator.ofFloat(
                            viewCircle,
                            View.SCALE_X,
                            1f,
                            45f
                        )

                    val scaleY =
                        ObjectAnimator.ofFloat(
                            viewCircle,
                            View.SCALE_Y,
                            1f,
                            45f
                        )

                    AnimatorSet().apply {

                        playTogether(
                            scaleX,
                            scaleY
                        )

                        duration = 750

                        interpolator =
                            AccelerateDecelerateInterpolator()

                        start()
                    }

                    splashBg.animate()
                        .alpha(1f)
                        .setDuration(250)
                        .start()

                    imgSplashIcon.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(350)
                        .start()

                    txtSplash.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(150)
                        .setDuration(350)
                        .start()

                    viewCircle.postDelayed({

                        onFinish()

                    }, 850)

                    viewCircle.postDelayed({

                        splashBg.animate()
                            .alpha(0f)
                            .setDuration(400)
                            .start()

                        txtSplash.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .start()

                        imgSplashIcon.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .start()

                        viewCircle.animate()
                            .alpha(0f)
                            .setDuration(350)
                            .withEndAction {

                                splashBg.visibility =
                                    View.GONE

                                viewCircle.visibility =
                                    View.GONE

                                imgSplashIcon.visibility =
                                    View.GONE

                                txtSplash.visibility =
                                    View.GONE
                            }
                            .start()

                    }, 1300)
                }
                .start()
        }

        private fun loadFragment(
            fragment: Fragment
        ) {

            supportFragmentManager
                .beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(
                    R.id.fragmentContainer,
                    fragment
                )
                .commit()
        }

        override fun onBackPressed() {

            if (!isBackPressedOnce) {

                isBackPressedOnce = true

                Toast.makeText(
                    this,
                    "Pencet Keluar Lagi",
                    Toast.LENGTH_SHORT
                ).show()

                window.decorView.postDelayed({
                    isBackPressedOnce = false
                }, 2000)

            } else {

                finishAffinity()
            }
        }
    }