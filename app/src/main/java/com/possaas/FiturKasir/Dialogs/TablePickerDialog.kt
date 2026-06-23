package com.possaas.FiturKasir.Dialogs

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.database.FirebaseDatabase
import com.possaas.R

class TablePickerDialog(
    private val context: Context,
    private val mode: Mode = Mode.SELECT_FOR_ORDER,
    private val initialSelected: Set<String> = emptySet(),
    private val onComplete: (selected: Set<String>) -> Unit
) {

    enum class Mode {
        SELECT_FOR_ORDER,
        MANAGE_TABLES
    }

    private val mejaList = listOf("A", "B", "C", "D", "E", "F", "G", "H")

    fun show() {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_pilih_tempat)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnClose = dialog.findViewById<ImageView>(R.id.btnClose)
        val btnSelesai = dialog.findViewById<LinearLayout>(R.id.btnSelesai)
        val gridMeja = dialog.findViewById<GridLayout>(R.id.gridMeja)

        val selectedTables = initialSelected.toMutableSet()

        gridMeja.removeAllViews()

        val inflater = LayoutInflater.from(context)

        FirebaseDatabase.getInstance().getReference("tableNumber").get()
            .addOnSuccessListener { tableNumberSnapshot ->
                for (snap in tableNumberSnapshot.children) {
                    val status = snap.child("status").getValue(String::class.java)
                    if (status == "occupied") {
                        selectedTables.add(snap.key ?: "")
                    }
                }

                mejaList.forEachIndexed { index, meja ->
                    val item = inflater.inflate(R.layout.item_meja_kasir, gridMeja, false)
                    val layout = item.findViewById<LinearLayout>(R.id.layoutMeja)
                    val txtMeja = item.findViewById<TextView>(R.id.txtMeja)
                    val chairTop = item.findViewById<View>(R.id.chairTop)
                    val chairBottom = item.findViewById<View>(R.id.chairBottom)

                    txtMeja.text = meja

                    fun refresh() {
                        if (selectedTables.contains(meja)) {
                            txtMeja.setBackgroundColor(context.getColor(R.color.primary))
                            txtMeja.setTextColor(context.getColor(R.color.accent))
                            chairTop.setBackgroundColor(context.getColor(R.color.primary))
                            chairBottom.setBackgroundColor(context.getColor(R.color.primary))
                        } else {
                            txtMeja.setBackgroundColor(context.getColor(R.color.white))
                            txtMeja.setTextColor(context.getColor(R.color.accent))
                            chairTop.setBackgroundColor(context.getColor(R.color.white))
                            chairBottom.setBackgroundColor(context.getColor(R.color.white))
                        }
                    }

                    refresh()

                    layout.setOnClickListener {
                        if (selectedTables.contains(meja)) {
                            selectedTables.remove(meja)
                        } else {
                            selectedTables.add(meja)
                        }
                        refresh()
                        btnSelesai.visibility = when (mode) {
                            Mode.SELECT_FOR_ORDER -> if (selectedTables.isEmpty()) View.GONE else View.VISIBLE
                            Mode.MANAGE_TABLES -> View.VISIBLE // always visible di manajemen meja
                        }
                    }

                    val row = index / 2
                    val col = index % 2
                    val params = GridLayout.LayoutParams(
                        GridLayout.spec(row, GridLayout.FILL),
                        GridLayout.spec(col, 1f)
                    )
                    params.width = 0
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT
                    val density = context.resources.displayMetrics.density
                    val vertOffsetDp = 12
                    val topMarginPx = if (index % 2 == 1) (vertOffsetDp * density).toInt() else 0
                    params.setMargins(0, topMarginPx, 0, 0)
                    val horizOffsetDp = 24
                    val horizOffsetPx = horizOffsetDp * density
                    item.layoutParams = params
                    item.translationX = if (col == 0) -horizOffsetPx / 2f else horizOffsetPx / 2f

                    gridMeja.addView(item)
                }

                btnSelesai.visibility = when (mode) {
                    Mode.SELECT_FOR_ORDER -> if (selectedTables.isEmpty()) View.GONE else View.VISIBLE
                    Mode.MANAGE_TABLES -> View.VISIBLE // always visible di manajemen meja
                }
            }
            .addOnFailureListener {
                mejaList.forEachIndexed { index, meja ->
                    val item = inflater.inflate(R.layout.item_meja_kasir, gridMeja, false)
                    val layout = item.findViewById<LinearLayout>(R.id.layoutMeja)
                    val txtMeja = item.findViewById<TextView>(R.id.txtMeja)
                    val chairTop = item.findViewById<View>(R.id.chairTop)
                    val chairBottom = item.findViewById<View>(R.id.chairBottom)

                    txtMeja.text = meja

                    fun refresh() {
                        if (selectedTables.contains(meja)) {
                            txtMeja.setBackgroundColor(context.getColor(R.color.primary))
                            txtMeja.setTextColor(context.getColor(R.color.accent))
                            chairTop.setBackgroundColor(context.getColor(R.color.primary))
                            chairBottom.setBackgroundColor(context.getColor(R.color.primary))
                        } else {
                            txtMeja.setBackgroundColor(context.getColor(R.color.white))
                            txtMeja.setTextColor(context.getColor(R.color.accent))
                            chairTop.setBackgroundColor(context.getColor(R.color.white))
                            chairBottom.setBackgroundColor(context.getColor(R.color.white))
                        }
                    }

                    refresh()

                    layout.setOnClickListener {
                        if (selectedTables.contains(meja)) selectedTables.remove(meja) else selectedTables.add(meja)
                        refresh()
                        btnSelesai.visibility = when (mode) {
                            Mode.SELECT_FOR_ORDER -> if (selectedTables.isEmpty()) View.GONE else View.VISIBLE
                            Mode.MANAGE_TABLES -> View.VISIBLE // always visible di manajemen meja
                        }
                    }

                    val row = index / 2
                    val col = index % 2
                    val params = GridLayout.LayoutParams(
                        GridLayout.spec(row, GridLayout.FILL),
                        GridLayout.spec(col, 1f)
                    )
                    params.width = 0
                    params.height = GridLayout.LayoutParams.WRAP_CONTENT
                    val density = context.resources.displayMetrics.density
                    val vertOffsetDp = 12
                    val topMarginPx = if (index % 2 == 1) (vertOffsetDp * density).toInt() else 0
                    params.setMargins(0, topMarginPx, 0, 0)
                    val horizOffsetDp = 24
                    val horizOffsetPx = horizOffsetDp * density
                    item.layoutParams = params
                    item.translationX = if (col == 0) -horizOffsetPx / 2f else horizOffsetPx / 2f

                    gridMeja.addView(item)
                }

                btnSelesai.visibility = when (mode) {
                    Mode.SELECT_FOR_ORDER -> if (selectedTables.isEmpty()) View.GONE else View.VISIBLE
                    Mode.MANAGE_TABLES -> View.VISIBLE // always visible di manajemen meja
                }
            }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnSelesai.setOnClickListener {
            val tableNumberRef = FirebaseDatabase.getInstance().getReference("tableNumber")
            val now = System.currentTimeMillis()

            when (mode) {
                Mode.SELECT_FOR_ORDER -> {
                    selectedTables.forEach { m ->
                        tableNumberRef.child(m).setValue(mapOf("status" to "reserved", "timestamp" to now))
                    }
                    onComplete(selectedTables)
                }
                Mode.MANAGE_TABLES -> {
                    mejaList.forEach { m ->
                        val status = if (selectedTables.contains(m)) "occupied" else "free"
                        tableNumberRef.child(m).setValue(mapOf("status" to status, "timestamp" to now))
                    }
                    onComplete(selectedTables)
                }
            }
            dialog.dismiss()
        }

        dialog.show()
    }
}