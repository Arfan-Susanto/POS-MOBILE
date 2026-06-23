package com.possaas.Admin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.possaas.FiturAdmin.EditKasir.EditKasirActivity
import com.possaas.FiturAdmin.EditMenu.EditMenuActivity
import com.possaas.FiturAdmin.EditStok.EditStockActivity
import com.possaas.R
import com.possaas.StockWarning.StockWarningAdapter
import com.possaas.StockWarning.StockWarningModel
import java.text.SimpleDateFormat
import java.util.*

class HomeAdminFragment : Fragment() {

    private lateinit var txtTime: TextView
    private lateinit var txtDate: TextView

    private lateinit var recyclerWarning:
            RecyclerView

    private lateinit var btnEditKasir:
            LinearLayout

    private lateinit var btnEditMenu:
            LinearLayout

    private lateinit var btnEditStock:
            LinearLayout

    private lateinit var adapter:
            StockWarningAdapter

    private val warningList =
        ArrayList<StockWarningModel>()

    private lateinit var database:
            DatabaseReference

    private val handler =
        Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_home_admin,
            container,
            false
        )

        txtTime =
            view.findViewById(R.id.txtTime)

        txtDate =
            view.findViewById(R.id.txtDate)

        recyclerWarning =
            view.findViewById(R.id.recyclerWarning)

        btnEditKasir =
            view.findViewById(R.id.btnEditKasir)

        btnEditMenu =
            view.findViewById(R.id.btnEditMenu)

        btnEditStock =
            view.findViewById(R.id.btnEditStok)

        adapter =
            StockWarningAdapter(warningList)

        recyclerWarning.layoutManager =
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )

        recyclerWarning.adapter =
            adapter

        // =========================
        // BUTTON EDIT KASIR
        // =========================
        btnEditKasir.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    EditKasirActivity::class.java
                )
            )
        }

        // =========================
        // BUTTON EDIT MENU
        // =========================
        btnEditMenu.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    EditMenuActivity::class.java
                )
            )
        }

        // =========================
        // BUTTON EDIT STOCK
        // =========================

        btnEditStock.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    EditStockActivity::class.java
                )
            )
        }

        startClock()
        getStockWarning()

        return view
    }

    private fun startClock() {

        handler.post(object : Runnable {

            override fun run() {

                val current = Date()

                val timeFormat =
                    SimpleDateFormat(
                        "HH:mm:ss",
                        Locale.getDefault()
                    )

                val dateFormat =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale("id", "ID")
                    )

                txtTime.text =
                    timeFormat.format(current)

                txtDate.text =
                    dateFormat.format(current)

                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun getStockWarning() {

        val rootRef =
            FirebaseDatabase
                .getInstance()
                .reference

        rootRef.addValueEventListener(

            object : ValueEventListener {

                override fun onDataChange(
                    snapshot: DataSnapshot
                ) {

                    warningList.clear()

                    val menuSnapshot =
                        snapshot.child("menu")

                    val stokSnapshot =
                        snapshot.child("stok")

                    for (data in menuSnapshot.children) {

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

                        val stok =
                            stokSnapshot
                                .child(menuId)
                                .child("jumlah")
                                .getValue(Int::class.java)
                                ?: 0

                        if (stok <= 5) {

                            warningList.add(

                                StockWarningModel(

                                    id = menuId,

                                    name = nama,

                                    stock = stok,

                                    image = foto
                                )
                            )
                        }
                    }

                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(
                    error: DatabaseError
                ) {

                }
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        handler.removeCallbacksAndMessages(null)
    }
}