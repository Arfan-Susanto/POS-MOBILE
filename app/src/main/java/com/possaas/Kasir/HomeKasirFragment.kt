package com.possaas.Kasir

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.possaas.FiturKasir.PilihMenu.PilihMenuActivity
import com.possaas.FiturKasir.Dialogs.TablePickerDialog
import android.widget.Toast
import com.possaas.R
import com.possaas.StoreState
import android.content.SharedPreferences
import android.app.Dialog
import android.view.Window
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

class HomeKasirFragment : Fragment() {

    private lateinit var txtWelcome: TextView
    private lateinit var txtRole: TextView
    private lateinit var txtStatus: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtHour: TextView
    private lateinit var txtMinute: TextView
    private lateinit var txtSecond: TextView
    private var closedDialog: Dialog? = null
    private var storePrefsListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private val handler =
        Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view =
            inflater.inflate(
                R.layout.fragment_home_kasir,
                container,
                false
            )

        txtWelcome =
            view.findViewById(R.id.txtWelcome)

        txtRole =
            view.findViewById(R.id.txtRole)

        txtStatus = view.findViewById(R.id.txtStatus)

        StoreState.init(requireContext())
        txtStatus.text = if (StoreState.isClosed) "TUTUP 🔴" else "BUKA 🟢"

        fun showShopClosedDialog() {
            if (closedDialog?.isShowing == true) return
            val dlgClosed = Dialog(requireContext())
            dlgClosed.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dlgClosed.setCancelable(false)
            val layoutClosed = layoutInflater.inflate(R.layout.dialog_shop_closed_admin, null)
            val btnKeluar = layoutClosed.findViewById<LinearLayout>(R.id.btnKeluar)
            btnKeluar.setOnClickListener {
                startActivity(Intent(requireContext(), com.possaas.Role.RoleActivity::class.java))
                requireActivity().finish()
            }
            dlgClosed.setContentView(layoutClosed)
            dlgClosed.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dlgClosed.show()
            closedDialog = dlgClosed
        }

        if (StoreState.isClosed) showShopClosedDialog()

        storePrefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "is_closed") {
                val closed = StoreState.isClosed
                requireActivity().runOnUiThread {
                    txtStatus.text = if (closed) "TUTUP 🔴" else "BUKA 🟢"
                    if (closed) showShopClosedDialog() else {
                        closedDialog?.takeIf { it.isShowing }?.dismiss()
                    }
                }
            }
        }

        StoreState.registerListener(storePrefsListener!!)

        txtDate =
            view.findViewById(R.id.txtDate)

        txtHour =
            view.findViewById(R.id.txtHour)

        txtMinute =
            view.findViewById(R.id.txtMinute)

        txtSecond =
            view.findViewById(R.id.txtSecond)

        view.findViewById<LinearLayout>(R.id.btnPilihMenu).setOnClickListener {
            startActivity(Intent(requireContext(), PilihMenuActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.btnManajemenMeja).setOnClickListener {
            val picker = TablePickerDialog(requireContext(), TablePickerDialog.Mode.MANAGE_TABLES, emptySet()) { selected ->
                // selected contains tables marked as occupied; show simple feedback
                if (selected.isEmpty()) {
                    Toast.makeText(requireContext(), "Tidak ada meja dipilih", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Meja dipilih: ${selected.joinToString(", ")}", Toast.LENGTH_SHORT).show()
                }
            }
            picker.show()
        }

        view.findViewById<LinearLayout>(R.id.btnRiwayatTransaksi).setOnClickListener {
            val intent = Intent(requireContext(), Class.forName("com.possaas.FiturKasir.TransactionHistory.TransactionHistoryActivity"))
            intent.putExtra("mode", "KASIR")
            startActivity(intent)
        }


        loadUserData()

        startRealtimeClock()

        return view
    }

    private fun animateCounter(textView: TextView, newValue: String) {

        val oldValue = textView.text.toString()

        if (oldValue == newValue) return

        textView.animate()
            .translationY(-25f)
            .alpha(0f)
            .setDuration(140)
            .withEndAction {

                textView.text = newValue

                textView.translationY = 25f

                textView.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(140)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
            }
            .start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        storePrefsListener?.let { StoreState.unregisterListener(it) }
        closedDialog?.dismiss()
        handler.removeCallbacksAndMessages(null)
    }

    private fun showStatusDialog() {

        val dlg = Dialog(requireContext())
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setCancelable(true)
        val layout = layoutInflater.inflate(R.layout.dialog_shop_status, null)
        val btnClose = layout.findViewById<LinearLayout>(R.id.btnClose)
        val cardBuka = layout.findViewById<LinearLayout>(R.id.cardBuka)
        val cardTutup = layout.findViewById<LinearLayout>(R.id.cardTutup)
        val tvBuka = layout.findViewById<TextView>(R.id.tvBuka)
        val tvTutup = layout.findViewById<TextView>(R.id.tvTutup)

        btnClose.setOnClickListener { dlg.dismiss() }

        cardBuka.setOnClickListener {
            txtStatus.text = "BUKA 🟢"
            dlg.dismiss()
        }

        cardTutup.setOnClickListener {
            txtStatus.text = "TUTUP 🔴"
            dlg.dismiss()
        }

        dlg.setContentView(layout)
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dlg.show()
    }

    private fun loadUserData() {

        val uid =
            FirebaseAuth.getInstance()
                .currentUser?.uid ?: return

        FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener {

                val shortName =
                    it.child("short_name")
                        .value
                        .toString()

                val role =
                    it.child("role")
                        .value
                        .toString()

                txtWelcome.text =
                    "Selamat datang, $shortName"

                txtRole.text = role
            }
    }

    private fun startRealtimeClock() {

        handler.post(object : Runnable {

            override fun run() {

                val date =
                    SimpleDateFormat(
                        "dd MMMM yyyy",
                        Locale("id", "ID")
                    ).format(Date())

                txtDate.text = date

                val newHour = String.format("%02d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
                val newMinute = String.format("%02d", Calendar.getInstance().get(Calendar.MINUTE))
                val newSecond = String.format("%02d", Calendar.getInstance().get(Calendar.SECOND))

                animateCounter(txtHour, newHour)
                animateCounter(txtMinute, newMinute)
                animateCounter(txtSecond, newSecond)

                handler.postDelayed(
                    this,
                    1000
                )
            }
        })
    }
}