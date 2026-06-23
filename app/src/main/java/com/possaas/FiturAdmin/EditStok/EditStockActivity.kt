package com.possaas.FiturAdmin.EditStok

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.graphics.Color
import android.view.animation.OvershootInterpolator
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import java.util.concurrent.atomic.AtomicInteger
import android.util.Log
import com.possaas.R

class EditStockActivity : AppCompatActivity() {

    private lateinit var recyclerStock: RecyclerView
    private lateinit var btnSelesai: LinearLayout
    private lateinit var stockAdapter: StockAdapter
    private val stockList = ArrayList<StockModel>()
    private var isButtonVisible = false
    private val changedMenuIds = mutableSetOf<String>()

    private var menuListener: ValueEventListener? = null
    private var stockListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        window.statusBarColor = Color.TRANSPARENT

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(R.layout.activity_edit_stock)

        recyclerStock = findViewById(R.id.recyclerStock)
        btnSelesai = findViewById(R.id.btnSelesai)
        val btnBack = findViewById<LinearLayout>(R.id.btnBack)

        recyclerStock.layoutManager = LinearLayoutManager(this)
        stockAdapter = StockAdapter(stockList, onStockChanged = { menuId ->
            changedMenuIds.add(menuId)
            showFinishButton()
        })
        recyclerStock.adapter = stockAdapter

        btnBack.setOnClickListener { finish() }
        btnSelesai.setOnClickListener { saveAllStock() }

        loadMenuAndStockRealtime()
    }

    private fun loadMenuAndStockRealtime() {
        val menuRef = FirebaseDatabase.getInstance().getReference("menu")
        val stockRef = FirebaseDatabase.getInstance().getReference("stok")

        menuListener = menuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(menuSnapshot: DataSnapshot) {
                val tempList = ArrayList<StockModel>()
                for (data in menuSnapshot.children) {
                    val menuId = data.key.toString()
                    val nama = data.child("nama").value.toString()
                    val foto = data.child("foto").value.toString()
                    tempList.add(StockModel(menuId, nama, foto, 0))
                }
                tempList.reverse()

                stockListener = stockRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(stokSnapshot: DataSnapshot) {
                        for (item in tempList) {
                            val node = stokSnapshot.child(item.menuId)
                            val jumlah = node.child("jumlah").getValue(Int::class.java) ?: 0
                            val reserved = node.child("reserved").getValue(Int::class.java) ?: 0

                            if (!changedMenuIds.contains(item.menuId)) {
                                item.stok = jumlah - reserved
                            }
                        }
                        stockList.clear()
                        stockList.addAll(tempList)
                        stockAdapter.notifyDataSetChanged()
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun saveAllStock() {
        val itemsToSave = stockList.filter { changedMenuIds.contains(it.menuId) }
        if (itemsToSave.isEmpty()) {
            Toast.makeText(this, "Tidak ada perubahan stok", Toast.LENGTH_SHORT).show()
            return
        }

        val stockRef = FirebaseDatabase.getInstance().getReference("stok")
        val total = itemsToSave.size
        val successCount = AtomicInteger(0)
        val doneCount = AtomicInteger(0)

        // disable button while saving
        btnSelesai.isEnabled = false

        Log.d("EditStock", "Saving ${total} items: ${itemsToSave.map { it.menuId }}")

        for (item in itemsToSave) {
            val ref = stockRef.child(item.menuId)
            // read reserved once then set jumlah
            ref.child("reserved").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(resSnapshot: DataSnapshot) {
                    val reserved = resSnapshot.getValue(Int::class.java) ?: 0
                    val jumlah = item.stok + reserved
                    Log.d("EditStock", "Updating ${item.menuId}: reserved=$reserved, set jumlah=$jumlah")
                    ref.child("jumlah").setValue(jumlah).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            successCount.incrementAndGet()
                        } else {
                            Log.w("EditStock", "Failed update ${item.menuId}", task.exception)
                        }
                        val done = doneCount.incrementAndGet()
                        if (done == total) {
                            runOnUiThread {
                                btnSelesai.isEnabled = true
                                if (successCount.get() == total) {
                                    Toast.makeText(this@EditStockActivity, "Stok Berhasil Diupdate", Toast.LENGTH_SHORT).show()
                                    changedMenuIds.clear()
                                    hideFinishButton()
                                } else {
                                    Toast.makeText(this@EditStockActivity, "Beberapa stok gagal diupdate", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w("EditStock", "Failed read reserved for ${item.menuId}", error.toException())
                    val done = doneCount.incrementAndGet()
                    if (done == total) {
                        runOnUiThread {
                            btnSelesai.isEnabled = true
                            if (successCount.get() == total) {
                                Toast.makeText(this@EditStockActivity, "Stok Berhasil Diupdate", Toast.LENGTH_SHORT).show()
                                changedMenuIds.clear()
                                hideFinishButton()
                            } else {
                                Toast.makeText(this@EditStockActivity, "Beberapa stok gagal diupdate", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            })
        }
    }

    private fun showFinishButton() {
        if (isButtonVisible) return
        isButtonVisible = true
        btnSelesai.visibility = View.VISIBLE
        btnSelesai.translationY = 300f
        btnSelesai.alpha = 0f
        btnSelesai.animate().translationY(0f).alpha(1f).setInterpolator(OvershootInterpolator()).setDuration(400).start()
    }

    private fun hideFinishButton() {
        isButtonVisible = false
        btnSelesai.animate().translationY(300f).alpha(0f).setDuration(250).withEndAction { btnSelesai.visibility = View.GONE }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        menuListener?.let { FirebaseDatabase.getInstance().getReference("menu").removeEventListener(it) }
        stockListener?.let { FirebaseDatabase.getInstance().getReference("stok").removeEventListener(it) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
