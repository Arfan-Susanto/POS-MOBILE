package com.possaas.FiturKasir.PaymentSuccess

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.possaas.KasirActivity
import com.possaas.databinding.ActivityPaymentSuccessBinding
import java.text.NumberFormat
import java.util.Locale

data class ReceiptMenuModelParcel(
    val id: String,
    val nama: String,
    val foto: String,
    val jumlah: Int,
    val hargaSatuan: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(nama)
        parcel.writeString(foto)
        parcel.writeInt(jumlah)
        parcel.writeLong(hargaSatuan)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ReceiptMenuModelParcel> {
        override fun createFromParcel(parcel: Parcel): ReceiptMenuModelParcel {
            return ReceiptMenuModelParcel(parcel)
        }

        override fun newArray(size: Int): Array<ReceiptMenuModelParcel?> {
            return arrayOfNulls(size)
        }
    }
}

data class TransactionDataParcel(
    val invoiceId: String,
    val customerName: String,
    val orderType: String,
    val paymentMethod: String,
    val menuItems: List<ReceiptMenuModelParcel>,
    val totalPrice: Long,
    val cashAmount: Long,
    val timestamp: Long,
    val tableNumber: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        mutableListOf<ReceiptMenuModelParcel>().apply {
            parcel.readTypedList(this, ReceiptMenuModelParcel.CREATOR)
        },
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(invoiceId)
        parcel.writeString(customerName)
        parcel.writeString(orderType)
        parcel.writeString(paymentMethod)
        parcel.writeTypedList(menuItems)
        parcel.writeLong(totalPrice)
        parcel.writeLong(cashAmount)
        parcel.writeLong(timestamp)
        parcel.writeString(tableNumber)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TransactionDataParcel> {
        override fun createFromParcel(parcel: Parcel): TransactionDataParcel {
            return TransactionDataParcel(parcel)
        }

        override fun newArray(size: Int): Array<TransactionDataParcel?> {
            return arrayOfNulls(size)
        }
    }
}

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentSuccessBinding
    private var transactionData: TransactionDataParcel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityPaymentSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactionData = intent.getParcelableExtra("transaction_data")

        setupViews()
        setupListeners()
        saveToFirebase()
    }

    private fun setupViews() {
        transactionData?.let { data ->
            binding.txtInvoiceId.text = data.invoiceId
            binding.txtCustomerName.text = data.customerName
            binding.txtOrderType.text = if (data.orderType == "dine_in") {
                "Dine In - Meja ${data.tableNumber}"
            } else {
                "Take Away"
            }
            binding.txtPaymentMethod.text = data.paymentMethod
            val receiptItems = data.menuItems.map {
                ReceiptMenuModel(
                    id = it.id,
                    nama = it.nama,
                    foto = it.foto,
                    jumlah = it.jumlah,
                    hargaSatuan = it.hargaSatuan
                )
            }
            val adapter = ReceiptItemAdapter(receiptItems)
            binding.rvReceiptItems.layoutManager = LinearLayoutManager(this)
            binding.rvReceiptItems.adapter = adapter

            val rupiah = NumberFormat.getInstance(Locale("id", "ID"))
            binding.txtTotalPrice.text = "Rp ${rupiah.format(data.totalPrice)}"
            binding.txtCashAmount.text = "Rp ${rupiah.format(data.cashAmount)}"

            val change = data.cashAmount - data.totalPrice
            binding.txtChange.text = "Rp ${rupiah.format(change)}"
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            val origin = intent.getStringExtra("origin") ?: ""
            if (origin.equals("ADMIN", ignoreCase = true)) {
                val adminIntent = Intent(this, Class.forName("com.possaas.AdminActivity"))
                startActivity(adminIntent)
            } else {
                val kasirIntent = Intent(this, KasirActivity::class.java)
                startActivity(kasirIntent)
            }
            finish()
        }

        binding.btnExportPdf.setOnClickListener {
            transactionData?.let { data ->
                exportToPdf(data)
            }
        }
    }

    private fun saveToFirebase() {
        transactionData?.let { data ->
            val database = FirebaseDatabase.getInstance()
            val transactionRef = database.getReference("transactions").child(data.invoiceId)
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                val transactionMap = mapOf(
                    "invoiceId" to data.invoiceId as Any,
                    "customerName" to data.customerName as Any,
                    "orderType" to data.orderType as Any,
                    "paymentMethod" to data.paymentMethod as Any,
                    "totalPrice" to data.totalPrice as Any,
                    "cashAmount" to data.cashAmount as Any,
                    "timestamp" to data.timestamp as Any,
                    "status" to "BERHASIL" as Any,
                    "tableNumber" to (data.tableNumber ?: "") as Any
                )

                val itemsMap = mutableMapOf<String, Any>()
                data.menuItems.forEachIndexed { index, item ->
                    itemsMap["items_$index"] = mapOf(
                        "id" to item.id as Any,
                        "nama" to item.nama as Any,
                        "foto" to item.foto as Any,
                        "jumlah" to item.jumlah as Any,
                        "hargaSatuan" to item.hargaSatuan as Any
                    )
                }

                transactionRef.setValue(
                    mutableMapOf<String, Any>().apply {
                        putAll(transactionMap)
                        putAll(itemsMap)
                    }
                )
            } else {
                FirebaseDatabase.getInstance().getReference("users").child(uid).get()
                    .addOnSuccessListener { userSnap ->
                        val shortName = userSnap.child("short_name").getValue(String::class.java) ?: ""

                        val transactionMap = mutableMapOf<String, Any>(
                            "invoiceId" to data.invoiceId as Any,
                            "customerName" to data.customerName as Any,
                            "orderType" to data.orderType as Any,
                            "paymentMethod" to data.paymentMethod as Any,
                            "totalPrice" to data.totalPrice as Any,
                            "cashAmount" to data.cashAmount as Any,
                            "timestamp" to data.timestamp as Any,
                            "status" to "BERHASIL" as Any,
                            "tableNumber" to (data.tableNumber ?: "") as Any,
                            "cashierId" to uid as Any,
                            "cashier_short_name" to shortName as Any
                        )

                        data.menuItems.forEachIndexed { index, item ->
                            transactionMap["items_$index"] = mapOf(
                                "id" to item.id as Any,
                                "nama" to item.nama as Any,
                                "foto" to item.foto as Any,
                                "jumlah" to item.jumlah as Any,
                                "hargaSatuan" to item.hargaSatuan as Any
                            )
                        }

                        transactionRef.setValue(transactionMap)
                    }
            }
        }
    }

    private fun exportToPdf(data: TransactionDataParcel) {
        val pdfHelper = PDFExportHelper(this)
        pdfHelper.generatePDF(data)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}