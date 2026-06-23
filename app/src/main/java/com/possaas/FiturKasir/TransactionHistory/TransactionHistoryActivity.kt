package com.possaas.FiturKasir.TransactionHistory

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.graphics.Color
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.possaas.R
import com.possaas.FiturKasir.PaymentSuccess.TransactionDataParcel
import com.possaas.FiturKasir.PaymentSuccess.ReceiptMenuModelParcel
import com.possaas.databinding.ActivityTransactionHistoryBinding

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private val transactions = ArrayList<TransactionItem>()
    private lateinit var adapter: TransactionHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mode = intent.getStringExtra("mode") ?: "KASIR"
        val isAdmin = mode.equals("ADMIN", ignoreCase = true)

        if (isAdmin) {
            binding.headerRoot.setBackgroundResource(R.drawable.bg_header_admin)
            binding.txtTitleHistory.setTextColor(getColor(R.color.accent))
            binding.btnBack.setBackgroundResource(R.drawable.bg_back_admin)
            binding.ivBackIcon.setColorFilter(getColor(R.color.primary))
        }

        setupRecyclerView()
        loadTransactions()
        setupListeners()
    }

    private fun setupRecyclerView() {
        val mode = intent.getStringExtra("mode") ?: ""
        val isAdmin = mode.equals("ADMIN", ignoreCase = true)

        adapter = TransactionHistoryAdapter(transactions, isAdmin) { invoiceId ->
            openTransactionDetail(invoiceId)
        }
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun loadTransactions() {
        val database = FirebaseDatabase.getInstance()
        val transactionRef = database.getReference("transactions")
        val tempTransactions = mutableListOf<TransactionItem>()
        var processedCount = 0

        transactionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                transactions.clear()
                tempTransactions.clear()
                processedCount = 0

                val totalTransactions = snapshot.childrenCount.toInt()
                if (totalTransactions == 0) {
                    adapter.notifyDataSetChanged()
                    return
                }

                for (data in snapshot.children) {
                    val invoiceId = data.child("invoiceId").getValue(String::class.java) ?: ""
                    val customerName = data.child("customerName").getValue(String::class.java) ?: ""
                    var cashierName = when {
                        data.child("cashier_short_name").exists() -> data.child("cashier_short_name").getValue(String::class.java) ?: ""
                        data.child("short_name").exists() -> data.child("short_name").getValue(String::class.java) ?: ""
                        else -> ""
                    }
                    val cashierId = data.child("cashierId").getValue(String::class.java) ?: ""
                    val timestamp = data.child("timestamp").getValue(Long::class.java) ?: 0L
                    val statusStr = data.child("status").getValue(String::class.java) ?: "BERHASIL"

                    val status = when (statusStr) {
                        "PROSES" -> TransactionStatus.PROSES
                        else -> TransactionStatus.BERHASIL
                    }

                    if (cashierName.isEmpty() && cashierId.isNotEmpty()) {
                        database.getReference("users").child(cashierId).get()
                            .addOnSuccessListener { userSnap ->
                                val resolvedCashierName = userSnap.child("short_name").getValue(String::class.java) ?: "-"
                                val transaction = TransactionItem(
                                    invoiceId = invoiceId,
                                    customerName = customerName,
                                    cashierName = resolvedCashierName,
                                    timestamp = timestamp,
                                    status = status
                                )
                                tempTransactions.add(transaction)
                                processedCount++

                                if (processedCount == totalTransactions) {
                                    transactions.addAll(tempTransactions)
                                    transactions.sortByDescending { it.timestamp }
                                    adapter.notifyDataSetChanged()
                                }
                            }
                            .addOnFailureListener {
                                val transaction = TransactionItem(
                                    invoiceId = invoiceId,
                                    customerName = customerName,
                                    cashierName = "-",
                                    timestamp = timestamp,
                                    status = status
                                )
                                tempTransactions.add(transaction)
                                processedCount++

                                if (processedCount == totalTransactions) {
                                    transactions.addAll(tempTransactions)
                                    transactions.sortByDescending { it.timestamp }
                                    adapter.notifyDataSetChanged()
                                }
                            }
                    } else {
                        val transaction = TransactionItem(
                            invoiceId = invoiceId,
                            customerName = customerName,
                            cashierName = if (cashierName.isNotEmpty()) cashierName else "-",
                            timestamp = timestamp,
                            status = status
                        )
                        tempTransactions.add(transaction)
                        processedCount++

                        if (processedCount == totalTransactions) {
                            transactions.addAll(tempTransactions)
                            transactions.sortByDescending { it.timestamp }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun openTransactionDetail(invoiceId: String) {
        val database = FirebaseDatabase.getInstance()
        val transactionRef = database.getReference("transactions").child(invoiceId)

        transactionRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                try {
                    val invId = snapshot.child("invoiceId").getValue(String::class.java) ?: ""
                    val customerName = snapshot.child("customerName").getValue(String::class.java) ?: ""
                    val orderType = snapshot.child("orderType").getValue(String::class.java) ?: "take_away"
                    val paymentMethod = snapshot.child("paymentMethod").getValue(String::class.java) ?: "Cash"
                    val totalPrice = snapshot.child("totalPrice").getValue(Long::class.java) ?: 0L
                    val cashAmount = snapshot.child("cashAmount").getValue(Long::class.java) ?: 0L
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val tableNumber = snapshot.child("tableNumber").getValue(String::class.java)

                    val menuItems = mutableListOf<ReceiptMenuModelParcel>()
                    var itemIndex = 0
                    while (true) {
                        val itemKey = "items_$itemIndex"
                        val itemSnap = snapshot.child(itemKey)
                        if (!itemSnap.exists()) break

                        val item = ReceiptMenuModelParcel(
                            id = itemSnap.child("id").getValue(String::class.java) ?: "",
                            nama = itemSnap.child("nama").getValue(String::class.java) ?: "",
                            foto = itemSnap.child("foto").getValue(String::class.java) ?: "",
                            jumlah = itemSnap.child("jumlah").getValue(Int::class.java) ?: 0,
                            hargaSatuan = itemSnap.child("hargaSatuan").getValue(Long::class.java) ?: 0L
                        )
                        menuItems.add(item)
                        itemIndex++
                    }

                    val parcel = TransactionDataParcel(
                        invoiceId = invId,
                        customerName = customerName,
                        orderType = orderType,
                        paymentMethod = paymentMethod,
                        menuItems = menuItems,
                        totalPrice = totalPrice,
                        cashAmount = cashAmount,
                        timestamp = timestamp,
                        tableNumber = tableNumber
                    )

                    val intent = Intent(this, Class.forName("com.possaas.FiturKasir.PaymentSuccess.PaymentSuccessActivity"))
                    intent.putExtra("transaction_data", parcel)
                    val origin = this.intent.getStringExtra("mode") ?: ""
                    if (origin.isNotEmpty()) intent.putExtra("origin", origin)
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Gagal membuka detail: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}