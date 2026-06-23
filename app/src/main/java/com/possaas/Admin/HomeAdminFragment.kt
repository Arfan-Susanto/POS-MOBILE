package com.possaas.Admin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.auth.FirebaseAuth
import com.possaas.FiturAdmin.EditKasir.EditKasirActivity
import com.possaas.FiturAdmin.EditMenu.EditMenuActivity
import com.possaas.FiturAdmin.EditStok.EditStockActivity
import com.possaas.R
import com.possaas.StockWarning.StockWarningAdapter
import com.possaas.StockWarning.StockWarningModel
import android.app.Dialog
import android.view.Window
import java.text.SimpleDateFormat
import java.util.*
import com.possaas.StoreState

class HomeAdminFragment : Fragment() {

    private lateinit var txtHour: TextView
    private lateinit var txtMinute: TextView
    private lateinit var txtSecond: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtWelcome:
            TextView
    private lateinit var txtRole:
            TextView
    private lateinit var txtStatus: TextView
    private lateinit var recyclerWarning:
            RecyclerView
    private lateinit var btnEditKasir:
            LinearLayout
    private lateinit var btnEditMenu:
            LinearLayout
    private lateinit var btnEditStock:
            LinearLayout
    private lateinit var btnDashboard:
            LinearLayout
    private lateinit var btnRiwayatTransaksi: LinearLayout
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

        txtHour =
            view.findViewById(R.id.txtHour)

        txtMinute =
            view.findViewById(R.id.txtMinute)

        txtSecond =
            view.findViewById(R.id.txtSecond)

        txtDate =
            view.findViewById(R.id.txtDate)

        txtWelcome =
            view.findViewById(R.id.txtWelcome)

        txtRole =
            view.findViewById(R.id.txtRole)

        txtStatus = view.findViewById(R.id.txtStatus)

        recyclerWarning =
            view.findViewById(R.id.recyclerWarning)

        btnEditKasir =
            view.findViewById(R.id.btnEditKasir)

        btnEditMenu =
            view.findViewById(R.id.btnEditMenu)

        btnEditStock =
            view.findViewById(R.id.btnEditStok)

        btnDashboard =
            view.findViewById(R.id.btnDashboard)

        btnRiwayatTransaksi =
            view.findViewById(R.id.btnRiwayatTransaksi)

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

        btnEditKasir.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    EditKasirActivity::class.java
                )
            )
        }

        btnEditMenu.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    EditMenuActivity::class.java
                )
            )
        }

        btnEditStock.setOnClickListener {

            startActivity(
                Intent(
                    requireContext(),
                    EditStockActivity::class.java
                )
            )
        }

        btnRiwayatTransaksi.setOnClickListener {
            val intent = Intent(
                requireContext(),
                com.possaas.FiturKasir.TransactionHistory.TransactionHistoryActivity::class.java
            )
            intent.putExtra("mode", "ADMIN")
            startActivity(intent)
        }

        btnDashboard.setOnClickListener {

            startActivity(

                Intent(
                    requireContext(),
                    com.possaas.FiturAdmin.Dashboard.DashboardActivity::class.java
                )
            )
        }

        startClock()
        getStockWarning()
        getAdminData()

        StoreState.init(requireContext())
        txtStatus.text = if (StoreState.isClosed) "TUTUP 🔴" else "BUKA 🟢"

        // STATUS CLICK -> show dialog (admin can change)
        txtStatus.setOnClickListener {
            showStatusDialog()
        }

        return view
    }

    private fun showStatusDialog() {

        val dlg = Dialog(requireContext())
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setCancelable(true)
        val layout = layoutInflater.inflate(R.layout.dialog_shop_status, null)
        val btnCloseCircle = layout.findViewById<LinearLayout>(R.id.btnClose)
        val cardBuka = layout.findViewById<LinearLayout>(R.id.cardBuka)
        val cardTutup = layout.findViewById<LinearLayout>(R.id.cardTutup)
        val tvBuka = layout.findViewById<TextView>(R.id.tvBuka)
        val tvTutup = layout.findViewById<TextView>(R.id.tvTutup)

        btnCloseCircle.setOnClickListener {
            dlg.dismiss()
        }

        fun setSelected(isBuka: Boolean) {
            if (isBuka) {
                StoreState.isClosed = false
                tvBuka.setTextColor(requireContext().getColor(R.color.primary))
                tvTutup.setTextColor(requireContext().getColor(R.color.primary))
                txtStatus.text = "BUKA 🟢"
            } else {
                StoreState.isClosed = true
                txtStatus.text = "TUTUP 🔴"
            }
        }

        cardBuka.setOnClickListener {
            setSelected(true)
            dlg.dismiss()
        }

        cardTutup.setOnClickListener {
            setSelected(false)
            dlg.dismiss()
        }

        dlg.setContentView(layout)
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dlg.show()
    }

    private fun startClock() {

        handler.post(object : Runnable {

            override fun run() {

                val calendar =
                    Calendar.getInstance()

                val newHour =
                    String.format(
                        "%02d",
                        calendar.get(Calendar.HOUR_OF_DAY)
                    )

                val newMinute =
                    String.format(
                        "%02d",
                        calendar.get(Calendar.MINUTE)
                    )

                val newSecond =
                    String.format(
                        "%02d",
                        calendar.get(Calendar.SECOND)
                    )

                val dateFormat =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale("id", "ID")
                    )

                animateCounter(
                    txtHour,
                    newHour
                )

                animateCounter(
                    txtMinute,
                    newMinute
                )

                animateCounter(
                    txtSecond,
                    newSecond
                )

                txtDate.text =
                    dateFormat.format(Date())

                handler.postDelayed(
                    this,
                    1000
                )
            }
        })
    }

    private fun animateCounter(
        textView: TextView,
        newValue: String
    ) {

        val oldValue =
            textView.text.toString()

        if (oldValue == newValue) return

        textView.animate()
            .translationY(-25f)
            .alpha(0f)
            .setDuration(140)
            .withEndAction {

                textView.text =
                    newValue

                textView.translationY = 25f

                textView.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(140)
                    .setInterpolator(
                        AccelerateDecelerateInterpolator()
                    )
                    .start()
            }
            .start()
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

    private fun getAdminData() {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid
                ?: return

        FirebaseDatabase
            .getInstance()
            .reference
            .child("users")
            .child(uid)
            .addListenerForSingleValueEvent(

                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        val shortName =
                            snapshot.child("short_name")
                                .value
                                .toString()

                        val role =
                            snapshot.child("role")
                                .value
                                .toString()

                        txtWelcome.text =
                            "Selamat datang, $shortName"

                        txtRole.text =
                            role
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