package com.possaas.FiturKasir.PilihMenu

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.res.ResourcesCompat
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.database.*
import com.google.android.material.button.MaterialButton
import com.possaas.R
import com.possaas.FiturKasir.Dialogs.TablePickerDialog
import com.possaas.FiturKasir.PaymentSuccess.TransactionDataParcel
import com.possaas.FiturKasir.PaymentSuccess.ReceiptMenuModelParcel
import android.content.Intent
import android.widget.Toast
import com.google.firebase.database.Transaction
import com.possaas.databinding.ActivityPilihMenuBinding
import java.text.NumberFormat
import java.util.Locale

class PilihMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPilihMenuBinding
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var recyclerMenu: RecyclerView
    private lateinit var menuAdapter: PilihMenuAdapter
    private lateinit var rvCartSummary: RecyclerView
    private lateinit var cartSummaryAdapter: CartSummaryAdapter
    private lateinit var txtTotalMenu: TextView
    private lateinit var txtTotalHarga: TextView
    private lateinit var etJumlahUang: EditText
    private lateinit var etNama: EditText
    private var jumlahUang: Long = 0L
    private var isFormatting = false
    private val menuList = ArrayList<PilihMenuModel>()
    private val allMenuList = ArrayList<PilihMenuModel>()
    private var selectedCategories = ArrayList<String>()
    private var currentSort = "default"
    private var currentSearch = ""
    private lateinit var btnPesanSekarang: View
    private lateinit var btnTakeAway: MaterialButton
    private lateinit var btnDineIn: MaterialButton
    private val selectedTables = mutableSetOf<String>()
    private var isDineIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        window.statusBarColor = Color.TRANSPARENT
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityPilihMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
        setupListeners()
        loadMenu()
        activateTakeAway()
    }

    private fun setupViews() {

        val bottomSheetView = findViewById<View>(R.id.layoutBottomSheet)

        btnPesanSekarang = bottomSheetView.findViewById(R.id.btnPesanSekarang)

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetView)
        bottomSheetBehavior.peekHeight = 200
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        bottomSheetBehavior.isHideable = true
        bottomSheetBehavior.isFitToContents = true
        bottomSheetBehavior.isDraggable = true
        bottomSheetBehavior.skipCollapsed = false
        bottomSheetView.visibility = View.GONE

        txtTotalMenu = findViewById(R.id.txtTotalMenu)
        txtTotalHarga = findViewById(R.id.txtTotalHarga)
        etJumlahUang = findViewById(R.id.etJumlahUang)
        etNama = bottomSheetView.findViewById(R.id.etNama)

        btnTakeAway = findViewById(R.id.btnTakeAway)
        btnDineIn = findViewById(R.id.btnDineIn)

        recyclerMenu = binding.recyclerMenu

        menuAdapter = PilihMenuAdapter(menuList,
            object : PilihMenuAdapter.OnCartChangeListener {
                override fun onCartChanged(list: List<PilihMenuModel>) {

                    val totalItem = list.sumOf { it.jumlah }
                    val totalHarga = list.sumOf { it.jumlah * it.harga }

                    txtTotalMenu.text = "$totalItem Menu"

                    val rupiah = NumberFormat.getInstance(Locale("id", "ID"))
                    txtTotalHarga.text = "Rp ${rupiah.format(totalHarga)}"

                    cartSummaryAdapter.notifyDataSetChanged()

                    validateCash(totalHarga)
                    if (totalItem > 0) {
                        showBottomSheetPeek()
                    } else {
                        hideBottomSheetWithAnimation()
                    }
                }
            }
        )

        recyclerMenu.layoutManager = GridLayoutManager(this, 2)
        recyclerMenu.adapter = menuAdapter

        rvCartSummary = bottomSheetView.findViewById(R.id.rvCartSummary)
        cartSummaryAdapter = CartSummaryAdapter(menuList)
        rvCartSummary.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        rvCartSummary.adapter = cartSummaryAdapter

        setupCategory(binding.categoryMinuman, "Minuman")
        setupCategory(binding.categoryMakanan, "Makanan")
        setupCategory(binding.categoryAlacarte, "Ala carte")
        setupCategory(binding.categorySaus, "Saus")
        setupCategory(binding.categoryPaket, "Paket")
    }

    private fun setupListeners() {

        binding.btnBack.setOnClickListener {
            finish()
        }

        txtTotalMenu.setOnClickListener {
            bottomSheetBehavior.state =
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                    BottomSheetBehavior.STATE_COLLAPSED
                else
                    BottomSheetBehavior.STATE_EXPANDED
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearch = s.toString()
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        etJumlahUang.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {}

            override fun afterTextChanged(s: Editable?) {

                if (isFormatting) return

                isFormatting = true

                val cleanString = s.toString()
                    .replace("Rp", "")
                    .replace(".", "")
                    .replace(",", "")
                    .trim()

                if (cleanString.isNotEmpty()) {

                    jumlahUang = cleanString.toLong()

                    val formatted = NumberFormat
                        .getInstance(Locale("id", "ID"))
                        .format(jumlahUang)

                    val result = "Rp $formatted"

                    etJumlahUang.setText(result)
                    etJumlahUang.setSelection(result.length)

                } else {
                    jumlahUang = 0L
                }

                val totalHarga = menuList.sumOf { it.jumlah * it.harga }
                validateCash(totalHarga)

                isFormatting = false
            }
        })

        btnTakeAway.setOnClickListener {

            selectedTables.clear()

            isDineIn = false

            activateTakeAway()
        }

        btnDineIn.setOnClickListener {

            showTableDialog()
        }

        binding.btnFilter.setOnClickListener {
            showFilterPopup()
        }

        btnPesanSekarang.setOnClickListener {
            val selectedItems = menuList.filter { it.jumlah > 0 }
            if (selectedItems.isEmpty()) return@setOnClickListener

            val totalHarga = selectedItems.sumOf { it.jumlah * it.harga }

            val clean = etJumlahUang.text.toString()
                .replace("Rp", "")
                .replace(".", "")
                .replace(",", "")
                .trim()
            val jumlahUangInput = if (clean.isNotEmpty()) clean.toLong() else 0L

            if (jumlahUangInput < totalHarga) {
                return@setOnClickListener
            }

            val parcelItems = selectedItems.map {
                ReceiptMenuModelParcel(
                    id = it.id,
                    nama = it.nama,
                    foto = it.foto,
                    jumlah = it.jumlah,
                    hargaSatuan = it.harga
                )
            }

            val customerName = etNama.text.toString().trim()
            val orderType = if (isDineIn) "dine_in" else "take_away"
            val tableNumber = if (isDineIn && selectedTables.isNotEmpty()) selectedTables.sorted().joinToString(",") else ""
            val transactionsRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("transactions")
            transactionsRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val count = snapshot.childrenCount.toInt()
                    val invoiceId = String.format("POS-%03d", count + 1)

                                    val stokRootRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("stok")

                                    stokRootRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(stokSnap: DataSnapshot) {
                                            for (p in parcelItems) {
                                                val cur = stokSnap.child(p.id).child("jumlah").getValue(Int::class.java) ?: 0
                                                if (cur < p.jumlah) {
                                                    Toast.makeText(this@PilihMenuActivity, "Stok tidak cukup untuk ${p.nama}", Toast.LENGTH_SHORT).show()
                                                    return
                                                }
                                            }

                                            var processed = 0
                                            var failed = false
                                            val total = parcelItems.size

                                            parcelItems.forEach { p ->
                                                val itemStokRef = stokRootRef.child(p.id).child("jumlah")
                                                itemStokRef.runTransaction(object : Transaction.Handler {
                                                    override fun doTransaction(currentData: MutableData): Transaction.Result {
                                                        val current = currentData.getValue(Int::class.java) ?: 0
                                                        return if (current >= p.jumlah) {
                                                            currentData.value = current - p.jumlah
                                                            Transaction.success(currentData)
                                                        } else {
                                                            Transaction.abort()
                                                        }
                                                    }

                                                    override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                                                        if (error != null || !committed) {
                                                            failed = true
                                                        }
                                                        processed++
                                                        if (processed == total) {
                                                            if (failed) {
                                                                Toast.makeText(this@PilihMenuActivity, "Gagal memperbarui stok. Mohon coba lagi.", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                val transactionParcel = TransactionDataParcel(
                                                                    invoiceId = invoiceId,
                                                                    customerName = customerName,
                                                                    orderType = orderType,
                                                                    paymentMethod = "Cash",
                                                                    menuItems = parcelItems,
                                                                    totalPrice = totalHarga,
                                                                    cashAmount = jumlahUangInput,
                                                                    timestamp = System.currentTimeMillis(),
                                                                    tableNumber = tableNumber
                                                                )

                                                                if (isDineIn && selectedTables.isNotEmpty()) {
                                                                    val tableNumberRef = FirebaseDatabase.getInstance().getReference("tableNumber")
                                                                    val txTimestamp = System.currentTimeMillis()
                                                                    selectedTables.forEach { tableLabel ->
                                                                        tableNumberRef.child(tableLabel).setValue(
                                                                            mapOf(
                                                                                "status" to "occupied",
                                                                                "invoiceId" to invoiceId,
                                                                                "timestamp" to txTimestamp
                                                                            )
                                                                        )
                                                                    }
                                                                }

                                                                parcelItems.forEach { pitem ->
                                                                    val local = menuList.find { it.id == pitem.id }
                                                                    local?.let {
                                                                        it.stok = (it.stok - pitem.jumlah).coerceAtLeast(0)
                                                                        it.jumlah = 0
                                                                    }
                                                                }
                                                                menuAdapter.notifyDataSetChanged()
                                                                cartSummaryAdapter.notifyDataSetChanged()
                                                                hideBottomSheetWithAnimation()

                                                                val intent = Intent(this@PilihMenuActivity, Class.forName("com.possaas.FiturKasir.PaymentSuccess.PaymentSuccessActivity"))
                                                                intent.putExtra("transaction_data", transactionParcel)
                                                                startActivity(intent)
                                                            }
                                                        }
                                                    }
                                                })
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Toast.makeText(this@PilihMenuActivity, "Gagal cek stok", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                }

                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
            })
        }
    }

    private fun validateCash(totalHarga: Long) {
        if (jumlahUang >= totalHarga && totalHarga > 0) {
            btnPesanSekarang.isEnabled = true
            btnPesanSekarang.alpha = 1f
        } else {
            btnPesanSekarang.isEnabled = false
            btnPesanSekarang.alpha = 0.5f
        }
    }

    private fun showFilterPopup() {
        val popup = PopupMenu(this, binding.btnFilter, android.view.Gravity.RIGHT, 0, R.style.CustomPopupMenu)
        popup.menu.add(styleMenu("Default"))
        popup.menu.add(styleMenu("Harga Tertinggi"))
        popup.menu.add(styleMenu("Harga Terendah"))

        popup.setOnMenuItemClickListener {
            when (it.title.toString()) {
                "Harga Tertinggi" -> {
                    currentSort = "highest"
                    binding.txtFilter.text = "Harga Tertinggi"
                }
                "Harga Terendah" -> {
                    currentSort = "lowest"
                    binding.txtFilter.text = "Harga Terendah"
                }
                else -> {
                    currentSort = "default"
                    binding.txtFilter.text = "Default"
                }
            }
            applyFilter()
            true
        }
        popup.show()
    }

    private fun loadMenu() {
        FirebaseDatabase.getInstance().getReference("menu")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allMenuList.clear()

                    for (data in snapshot.children) {
                        val id = data.key.toString()

                        FirebaseDatabase.getInstance().getReference("stok").child(id)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(stokSnapshot: DataSnapshot) {

                                    val stok = stokSnapshot.child("jumlah")
                                        .getValue(Int::class.java) ?: 0

                                    val model = PilihMenuModel(
                                        id = id,
                                        nama = data.child("nama").value.toString(),
                                        harga = data.child("harga").getValue(Long::class.java) ?: 0L,
                                        foto = data.child("foto").value.toString(),
                                        kategori = data.child("kategori")
                                            .children.map { it.value.toString() }
                                            .toMutableList(),
                                        stok = stok
                                    )

                                    allMenuList.add(model)
                                    applyFilter()
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun applyFilter() {

        menuList.clear()

        var filtered = if (selectedCategories.isEmpty()) {
            allMenuList
        } else {
            allMenuList.filter { menu ->
                selectedCategories.all { menu.kategori.contains(it) }
            }
        }

        if (currentSearch.isNotEmpty()) {
            filtered = filtered.filter {
                it.nama.contains(currentSearch, true)
            }
        }

        filtered = when (currentSort) {
            "highest" -> filtered.sortedByDescending { it.harga }
            "lowest" -> filtered.sortedBy { it.harga }
            else -> filtered.reversed()
        }

        menuList.addAll(filtered)
        menuAdapter.notifyDataSetChanged()
    }

    private fun setupCategory(textView: TextView, value: String) {
        val customFont = ResourcesCompat.getFont(this, R.font.plus_jakarta)
        if (customFont != null) {
            textView.typeface = Typeface.create(customFont, Typeface.NORMAL)
        } else {
            textView.setTypeface(null, Typeface.NORMAL)
        }

        updateCategoryUI(textView, false)

        textView.setOnClickListener {
            if (selectedCategories.contains(value)) {
                selectedCategories.remove(value)
                updateCategoryUI(textView, false)
            } else {
                selectedCategories.add(value)
                updateCategoryUI(textView, true)
            }
            applyFilter()
        }
    }

    private fun updateCategoryUI(textView: TextView, selected: Boolean) {
        if (selected) {
            textView.setBackgroundResource(R.drawable.bg_category_selected_kasir)
            textView.setTextColor(getColor(R.color.accent))
        } else {
            textView.setBackgroundResource(R.drawable.bg_category_unselected_kasir)
            textView.setTextColor(getColor(R.color.primary))
        }
    }

    private fun activateDineIn() {

        btnDineIn.setBackgroundResource(
            R.drawable.bg_button_selected
        )

        btnDineIn.setTextColor(
            getColor(R.color.primary)
        )

        btnTakeAway.setBackgroundResource(
            R.drawable.bg_button_unselected
        )

        btnTakeAway.setTextColor(
            getColor(R.color.accent)
        )
    }

    private fun activateTakeAway() {

        btnTakeAway.setBackgroundResource(
            R.drawable.bg_button_selected
        )

        btnTakeAway.setTextColor(
            getColor(R.color.primary)
        )

        btnDineIn.setBackgroundResource(
            R.drawable.bg_button_unselected
        )

        btnDineIn.setTextColor(
            getColor(R.color.accent)
        )
    }

    private fun showTableDialog() {
        val picker = TablePickerDialog(this, TablePickerDialog.Mode.SELECT_FOR_ORDER, selectedTables) { selected ->
            selectedTables.clear()
            selectedTables.addAll(selected)

            if (selectedTables.isEmpty()) {
                isDineIn = false
                activateTakeAway()
            } else {
                isDineIn = true
                activateDineIn()
            }
        }

        picker.show()
    }

    private fun styleMenu(title: String): SpannableString {
        val span = SpannableString(title)
        span.setSpan(
            ForegroundColorSpan(getColor(R.color.primary)),
            0,
            span.length,
            0
        )
        span.setSpan(StyleSpan(Typeface.NORMAL), 0, span.length, 0)
        return span
    }

    private fun showBottomSheetPeek() {
        val bottomSheetView = findViewById<View>(R.id.layoutBottomSheet)
        bottomSheetView.post {
            val wasHidden = bottomSheetView.visibility != View.VISIBLE
            if (wasHidden) {
                bottomSheetView.visibility = View.VISIBLE
                val animation = AnimationUtils.loadAnimation(this, R.anim.slide_up_smooth)
                bottomSheetView.startAnimation(animation)
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun hideBottomSheetWithAnimation() {
        if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            val bottomSheetView = findViewById<View>(R.id.layoutBottomSheet)
            val animation = AnimationUtils.loadAnimation(this, R.anim.slide_down_smooth)
            animation.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation) {}
                override fun onAnimationEnd(animation: android.view.animation.Animation) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                    bottomSheetView.visibility = View.GONE
                }
                override fun onAnimationRepeat(animation: android.view.animation.Animation) {}
            })
            bottomSheetView.startAnimation(animation)
        }
    }
}