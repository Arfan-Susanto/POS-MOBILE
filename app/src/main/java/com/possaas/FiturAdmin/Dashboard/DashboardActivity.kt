package com.possaas.FiturAdmin.Dashboard

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.database.*
import com.possaas.R

class DashboardActivity : AppCompatActivity() {

    private lateinit var btnBack: View
    private lateinit var txtTitle: TextView

    private lateinit var txtTotalAdmin: TextView
    private lateinit var txtTotalKasir: TextView
    private lateinit var txtTotalMenu: TextView
    private lateinit var txtTotalBought: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_dashboard_admin)

        btnBack = findViewById(R.id.btnBack)
        txtTitle = findViewById(R.id.txtTitle)

        txtTotalAdmin = findViewById(R.id.txtTotalAdmin)
        txtTotalKasir = findViewById(R.id.txtTotalKasir)
        txtTotalMenu = findViewById(R.id.txtTotalMenu)
        txtTotalBought = findViewById(R.id.txtTotalBought)

        txtTitle.setTypeface(ResourcesCompat.getFont(this, R.font.plus_jakarta), Typeface.BOLD)
        txtTitle.setTextColor(ContextCompat.getColor(this, R.color.accent))

        btnBack.setOnClickListener {
            finish()
        }

        loadCounts()
    }

    private fun loadCounts() {
        val rootRef = FirebaseDatabase.getInstance().reference

        rootRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var adminCount = 0
                var kasirCount = 0

                val usersSnapshot = snapshot.child("users")
                for (user in usersSnapshot.children) {
                    val role = user.child("role").value?.toString() ?: ""
                    if (role.equals("ADMIN", ignoreCase = true)) adminCount++
                    if (role.equals("KASIR", ignoreCase = true)) kasirCount++
                }

                // menu
                val menuSnapshot = snapshot.child("menu")
                val menuCount = menuSnapshot.childrenCount.toInt()

                val transSnapshot = snapshot.child("transactions")
                var boughtCount = 0
                for (trx in transSnapshot.children) {
                    val customer = trx.child("customerName").value?.toString() ?: ""
                    if (customer.isNotEmpty()) boughtCount++
                }

                txtTotalAdmin.text = adminCount.toString()
                txtTotalKasir.text = kasirCount.toString()
                txtTotalMenu.text = menuCount.toString()
                txtTotalBought.text = boughtCount.toString()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}