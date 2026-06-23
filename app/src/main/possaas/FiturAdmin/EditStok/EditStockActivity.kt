package com.possaas.FiturAdmin.EditStok

import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.possaas.R

class EditStockActivity : AppCompatActivity() {

    private lateinit var recyclerStock:
            RecyclerView

    private lateinit var btnSelesai:
            LinearLayout

    private lateinit var stockAdapter:
            StockAdapter

    private val stockList =
        ArrayList<StockModel>()

    private var isButtonVisible =
        false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_edit_stock
        )

        recyclerStock =
            findViewById(
                R.id.recyclerStock
            )

        btnSelesai =
            findViewById(
                R.id.btnSelesai
            )

        val btnBack =
            findViewById<LinearLayout>(
                R.id.btnBack
            )

        recyclerStock.layoutManager =
            LinearLayoutManager(this)

        stockAdapter =
            StockAdapter(

                stockList,

                onStockChanged = {

                    showFinishButton()
                }
            )

        recyclerStock.adapter =
            stockAdapter

        btnBack.setOnClickListener {

            finish()
        }

        btnSelesai.setOnClickListener {

            saveAllStock()
        }

        loadMenuAndStock()
    }

    // =========================
    // LOAD MENU + STOCK
    // =========================

    private fun loadMenuAndStock() {

        FirebaseDatabase
            .getInstance()
            .getReference("menu")
            .addListenerForSingleValueEvent(

                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        stockList.clear()

                        val tempList =
                            ArrayList<StockModel>()

                        for (data in snapshot.children) {

                            val menuId =
                                data.key.toString()

                            val nama =
                                data.child("nama")
                                    .value
                                    .toString()

                            val foto =
                                data.child("foto")
                                    .value
                                    .toString()

                            tempList.add(

                                StockModel(
                                    menuId,
                                    nama,
                                    foto,
                                    0
                                )
                            )
                        }

                        // MENU TERBARU DI ATAS
                        tempList.reverse()

                        FirebaseDatabase
                            .getInstance()
                            .getReference("stok")
                            .addListenerForSingleValueEvent(

                                object : ValueEventListener {

                                    override fun onDataChange(
                                        stokSnapshot: DataSnapshot
                                    ) {

                                        for (item in tempList) {

                                            val jumlah =
                                                stokSnapshot
                                                    .child(item.menuId)
                                                    .child("jumlah")
                                                    .getValue(Int::class.java)
                                                    ?: 0

                                            item.stok =
                                                jumlah
                                        }

                                        stockList.addAll(
                                            tempList
                                        )

                                        stockAdapter.notifyDataSetChanged()
                                    }

                                    override fun onCancelled(
                                        error: DatabaseError
                                    ) {

                                    }
                                }
                            )
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {

                    }
                }
            )
    }

    // =========================
    // SAVE STOCK
    // =========================

    private fun saveAllStock() {

        val ref =
            FirebaseDatabase
                .getInstance()
                .getReference("stok")

        var successCount = 0

        for (item in stockList) {

            ref.child(item.menuId)
                .child("jumlah")
                .setValue(item.stok)
                .addOnSuccessListener {

                    successCount++

                    // JIKA SEMUA SUDAH TERSIMPAN
                    if (
                        successCount ==
                        stockList.size
                    ) {

                        android.widget.Toast.makeText(
                            this,
                            "Stok Berhasil Diupdate",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()

                        hideFinishButton()
                    }
                }
        }
    }

    // =========================
    // SHOW BUTTON
    // =========================

    private fun showFinishButton() {

        if (isButtonVisible)
            return

        isButtonVisible = true

        btnSelesai.visibility =
            View.VISIBLE

        btnSelesai.translationY =
            300f

        btnSelesai.alpha =
            0f

        btnSelesai.animate()
            .translationY(0f)
            .alpha(1f)
            .setInterpolator(
                OvershootInterpolator()
            )
            .setDuration(400)
            .start()
    }

    // =========================
    // HIDE BUTTON
    // =========================

    private fun hideFinishButton() {

        isButtonVisible = false

        btnSelesai.animate()
            .translationY(300f)
            .alpha(0f)
            .setDuration(250)
            .withEndAction {

                btnSelesai.visibility =
                    View.GONE
            }
            .start()
    }
}