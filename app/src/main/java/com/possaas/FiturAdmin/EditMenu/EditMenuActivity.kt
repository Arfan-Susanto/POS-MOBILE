package com.possaas.FiturAdmin.EditMenu

import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.View
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.possaas.R
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

class EditMenuActivity : AppCompatActivity() {

    private lateinit var recyclerMenu:
            RecyclerView

    private lateinit var menuAdapter:
            MenuAdapter

    private val menuList =
        ArrayList<MenuModel>()

    private val allMenuList =
        ArrayList<MenuModel>()

    private var selectedImageUri:
            Uri? = null

    private var currentPreview:
            ShapeableImageView? = null

    private var selectedCategories =
        ArrayList<String>()

    private var selectedFilterCategories =
        ArrayList<String>()

    private var currentSort =
        "default"

    private var currentSearch =
        ""

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(
            savedInstanceState
        )

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        window.statusBarColor = Color.TRANSPARENT

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setContentView(
            R.layout.activity_edit_menu
        )

        recyclerMenu =
            findViewById(
                R.id.recyclerMenu
            )

        val btnTambahMenu =
            findViewById<LinearLayout>(
                R.id.btnTambahMenu
            )

        val btnBack =
            findViewById<LinearLayout>(
                R.id.btnBack
            )

        val btnFilter =
            findViewById<LinearLayout>(
                R.id.btnFilter
            )

        val txtFilter =
            findViewById<TextView>(
                R.id.txtFilter
            )

        val edtSearch =
            findViewById<EditText>(
                R.id.edtSearch
            )

        val categoryMinuman =
            findViewById<TextView>(
                R.id.categoryMinuman
            )

        val categoryMakanan =
            findViewById<TextView>(
                R.id.categoryMakanan
            )

        val categoryAlacarte =
            findViewById<TextView>(
                R.id.categoryAlacarte
            )

        val categorySaus =
            findViewById<TextView>(
                R.id.categorySaus
            )

        val categoryPaket =
            findViewById<TextView>(
                R.id.categoryPaket
            )

        setupFilterCategory(
            categoryMinuman,
            "Minuman"
        )

        setupFilterCategory(
            categoryMakanan,
            "Makanan"
        )

        setupFilterCategory(
            categoryAlacarte,
            "Ala carte"
        )

        setupFilterCategory(
            categorySaus,
            "Saus"
        )

        setupFilterCategory(
            categoryPaket,
            "Paket"
        )

        menuAdapter =
            MenuAdapter(

                menuList,

                onEditClick = { menu ->

                    showMenuDialog(
                        true,
                        menu
                    )
                },

                onDeleteClick = { menu ->

                    showDeleteDialog(menu)
                }
            )

        recyclerMenu.layoutManager =
            GridLayoutManager(
                this,
                2
            )

        recyclerMenu.adapter =
            menuAdapter


        edtSearch.addTextChangedListener(

            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    currentSearch =
                        s.toString()
                            .trim()

                    applyFilterAndSort()
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {

                }
            }
        )

        btnFilter.setOnClickListener {

            val popupMenu =
                PopupMenu(
                    this,
                    btnFilter,
                    android.view.Gravity.RIGHT,
                    0,
                    R.style.CustomPopupMenuAdmin
                )

            popupMenu.menu.add(
                styleMenuItem("Default")
            )

            popupMenu.menu.add(
                styleMenuItem(
                    "Harga Tertinggi"
                )
            )

            popupMenu.menu.add(
                styleMenuItem(
                    "Harga Terendah"
                )
            )

            popupMenu.setOnMenuItemClickListener {

                when (it.title.toString()) {

                    "Harga Tertinggi" -> {

                        currentSort =
                            "highest"

                        txtFilter.text =
                            "Harga Tertinggi"
                    }

                    "Harga Terendah" -> {

                        currentSort =
                            "lowest"

                        txtFilter.text =
                            "Harga Terendah"
                    }

                    else -> {

                        currentSort =
                            "default"

                        txtFilter.text =
                            "Default"
                    }
                }

                applyFilterAndSort()

                true
            }

            popupMenu.show()
        }

        loadMenu()

        btnTambahMenu.setOnClickListener {

            showMenuDialog(false)
        }

        btnBack.setOnClickListener {

            finish()
        }
    }

    private fun styleMenuItem(
        title: String
    ): SpannableString {

        val span =
            SpannableString(title)

        span.setSpan(

            ForegroundColorSpan(
                getColor(R.color.accent)
            ),

            0,

            span.length,

            0
        )

        span.setSpan(

            StyleSpan(Typeface.NORMAL),

            0,

            span.length,

            0
        )

        return span
    }

    private fun loadMenu() {

        FirebaseDatabase
            .getInstance()
            .getReference("menu")
            .addValueEventListener(

                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        allMenuList.clear()

                        for (data in snapshot.children) {

                            val model =
                                MenuModel(

                                    id =
                                        data.key.toString(),

                                    nama =
                                        data.child("nama")
                                            .value
                                            .toString(),

                                    harga =
                                        data.child("harga")
                                            .getValue(Long::class.java)
                                            ?: 0L,

                                    foto =
                                        data.child("foto")
                                            .value
                                            .toString(),

                                    kategori =
                                        data.child("kategori")
                                            .children
                                            .map {
                                                it.value.toString()
                                            }
                                            .toMutableList()
                                )

                            allMenuList.add(model)
                        }

                        applyFilterAndSort()
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {

                    }
                }
            )
    }


    private fun applyFilterAndSort() {

        menuList.clear()

        var filteredList =

            if (
                selectedFilterCategories.isEmpty()
            ) {

                allMenuList

            } else {

                allMenuList.filter { menu ->

                    selectedFilterCategories.all { selectedCategory ->

                        menu.kategori.contains(
                            selectedCategory
                        )
                    }
                }
            }


        if (
            currentSearch.isNotEmpty()
        ) {

            filteredList =
                filteredList.filter { menu ->

                    menu.nama.contains(
                        currentSearch,
                        ignoreCase = true
                    )
                }
        }


        val sortedList = when (currentSort) {

            "highest" -> {

                filteredList.sortedByDescending {
                    it.harga
                }
            }

            "lowest" -> {

                filteredList.sortedBy {
                    it.harga
                }
            }

            else -> {

                filteredList.reversed()
            }
        }

        menuList.addAll(
            sortedList
        )

        menuAdapter.notifyDataSetChanged()
    }


    private val pickImage =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {

            if (it.resultCode == RESULT_OK) {

                val uri =
                    it.data?.data
                        ?: return@registerForActivityResult

                val destination =
                    Uri.fromFile(

                        File(
                            cacheDir,
                            "${UUID.randomUUID()}.jpg"
                        )
                    )

                UCrop.of(
                    uri,
                    destination
                )
                    .withAspectRatio(
                        1f,
                        1f
                    )
                    .start(this)
            }
        }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == UCrop.REQUEST_CROP &&
            resultCode == RESULT_OK
        ) {

            selectedImageUri =
                UCrop.getOutput(data!!)

            currentPreview?.let {

                Glide.with(this)
                    .load(selectedImageUri)
                    .into(it)
            }
        }
    }

    private fun showMenuDialog(
        isEdit: Boolean,
        menu: MenuModel? = null
    ) {

        selectedImageUri = null

        selectedCategories.clear()

        val dialog = Dialog(this)

        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE
        )

        dialog.setContentView(
            R.layout.dialog_tambah_menu
        )

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        val edtNamaMenu =
            dialog.findViewById<EditText>(
                R.id.edtNamaMenu
            )

        val edtHarga =
            dialog.findViewById<EditText>(
                R.id.edtHarga
            )

        val imgPreviewMenu =
            dialog.findViewById<ShapeableImageView>(
                R.id.imgPreviewMenu
            )

        val btnUploadMenu =
            dialog.findViewById<LinearLayout>(
                R.id.btnUploadMenu
            )

        val btnSelesai =
            dialog.findViewById<LinearLayout>(
                R.id.btnSelesai
            )

        val btnClose =
            dialog.findViewById<LinearLayout>(
                R.id.btnClose
            )

        val categoryMinuman =
            dialog.findViewById<TextView>(
                R.id.categoryMinumanDialog
            )

        val categoryMakanan =
            dialog.findViewById<TextView>(
                R.id.categoryMakananDialog
            )

        val categoryAlacarte =
            dialog.findViewById<TextView>(
                R.id.categoryAlacarteDialog
            )

        val categorySaus =
            dialog.findViewById<TextView>(
                R.id.categorySausDialog
            )

        val categoryPaket =
            dialog.findViewById<TextView>(
                R.id.categoryPaketDialog
            )

        currentPreview =
            imgPreviewMenu

        setupCategory(
            categoryMinuman,
            "Minuman"
        )

        setupCategory(
            categoryMakanan,
            "Makanan"
        )

        setupCategory(
            categoryAlacarte,
            "Ala carte"
        )

        setupCategory(
            categorySaus,
            "Saus"
        )

        setupCategory(
            categoryPaket,
            "Paket"
        )

        if (
            isEdit &&
            menu != null
        ) {

            edtNamaMenu.setText(
                menu.nama
            )

            edtHarga.setText(
                menu.harga.toString()
            )

            Glide.with(this)
                .load(menu.foto)
                .placeholder(
                    R.drawable.logo_bfc
                )
                .into(imgPreviewMenu)

            selectedCategories.addAll(
                menu.kategori
            )

            restoreCategory(
                categoryMinuman,
                "Minuman"
            )

            restoreCategory(
                categoryMakanan,
                "Makanan"
            )

            restoreCategory(
                categoryAlacarte,
                "Ala carte"
            )

            restoreCategory(
                categorySaus,
                "Saus"
            )

            restoreCategory(
                categoryPaket,
                "Paket"
            )
        }

        btnUploadMenu.setOnClickListener {

            val intent =
                Intent(

                    Intent.ACTION_PICK,

                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

            pickImage.launch(intent)
        }

        btnClose.setOnClickListener {

            dialog.dismiss()
        }

        btnSelesai.setOnClickListener {

            val nama =
                edtNamaMenu.text.toString().trim()

            val hargaText =
                edtHarga.text.toString().trim()

            if (
                nama.isEmpty() ||
                hargaText.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Lengkapi semua data",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (
                selectedCategories.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Pilih kategori",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val harga =
                hargaText.toLongOrNull()

            if (harga == null) {

                Toast.makeText(
                    this,
                    "Harga tidak valid",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val id =
                if (isEdit)
                    menu?.id ?: ""
                else
                    UUID.randomUUID().toString()

            if (selectedImageUri != null) {

                MediaManager.get()
                    .upload(selectedImageUri)
                    .option(
                        "folder",
                        "menu_possaas"
                    )
                    .callback(

                        object : UploadCallback {

                            override fun onStart(
                                requestId: String?
                            ) {

                            }

                            override fun onProgress(
                                requestId: String?,
                                bytes: Long,
                                totalBytes: Long
                            ) {

                            }

                            override fun onSuccess(
                                requestId: String?,
                                resultData: MutableMap<Any?, Any?>?
                            ) {

                                val imageUrl =
                                    resultData?.get(
                                        "secure_url"
                                    )
                                        ?.toString()
                                        ?: ""

                                saveMenu(

                                    id,

                                    nama,

                                    harga,

                                    imageUrl,

                                    dialog
                                )
                            }

                            override fun onError(
                                requestId: String?,
                                error: ErrorInfo?
                            ) {

                                Toast.makeText(
                                    this@EditMenuActivity,
                                    "Upload gagal",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            override fun onReschedule(
                                requestId: String?,
                                error: ErrorInfo?
                            ) {

                            }
                        }
                    )
                    .dispatch()

            } else {

                saveMenu(

                    id,

                    nama,

                    harga,

                    menu?.foto ?: "",

                    dialog
                )
            }
        }

        dialog.show()
    }

    private fun saveMenu(

        id: String,

        nama: String,

        harga: Long,

        foto: String,

        dialog: Dialog

    ) {

        val data =
            HashMap<String, Any>()

        data["nama"] =
            nama

        data["harga"] =
            harga

        data["foto"] =
            foto

        data["kategori"] =
            selectedCategories

        FirebaseDatabase
            .getInstance()
            .getReference("menu")
            .child(id)
            .setValue(data)
            .addOnSuccessListener {

                val stockData =
                    HashMap<String, Any>()

                stockData["jumlah"] = 0

                FirebaseDatabase
                    .getInstance()
                    .getReference("stok")
                    .child(id)
                    .setValue(stockData)

                Toast.makeText(
                    this,
                    "Menu berhasil disimpan",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Gagal simpan menu",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun deleteMenu(
        id: String
    ) {

        FirebaseDatabase
            .getInstance()
            .getReference("menu")
            .child(id)
            .removeValue()
            .addOnSuccessListener {

                Toast.makeText(
                    this,
                    "Menu berhasil dihapus",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showDeleteDialog(
        menu: MenuModel
    ) {

        val dialog =
            Dialog(this)

        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE
        )

        dialog.setContentView(
            R.layout.dialog_delete_menu
        )

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        val btnClose =
            dialog.findViewById<LinearLayout>(
                R.id.btnCloseDeleteMenu
            )

        val btnHapus =
            dialog.findViewById<LinearLayout>(
                R.id.btnHapusMenu
            )

        btnClose.setOnClickListener {

            dialog.dismiss()
        }

        btnHapus.setOnClickListener {

            deleteMenu(menu.id)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupCategory(
        textView: TextView,
        value: String
    ) {

        val customFont = ResourcesCompat.getFont(this, R.font.plus_jakarta)
        if (customFont != null) {
            textView.typeface = Typeface.create(customFont, Typeface.NORMAL)
        } else {
            textView.setTypeface(null, Typeface.NORMAL)
        }

        updateDialogCategoryUI(
            textView,
            selectedCategories.contains(value)
        )

        textView.setOnClickListener {

            if (
                selectedCategories.contains(value)
            ) {

                selectedCategories.remove(value)

                updateDialogCategoryUI(
                    textView,
                    false
                )

            } else {

                selectedCategories.add(value)

                updateDialogCategoryUI(
                    textView,
                    true
                )
            }
        }
    }

    private fun setupFilterCategory(
        textView: TextView,
        value: String
    ) {

        val customFont = ResourcesCompat.getFont(this, R.font.plus_jakarta)
        if (customFont != null) {
            textView.typeface = Typeface.create(customFont, Typeface.NORMAL)
        } else {
            textView.setTypeface(null, Typeface.NORMAL)
        }

        updateMainCategoryUI(
            textView,
            selectedFilterCategories.contains(value)
        )

        textView.setOnClickListener {

            if (
                selectedFilterCategories.contains(value)
            ) {

                selectedFilterCategories.remove(value)

                updateMainCategoryUI(
                    textView,
                    false
                )

            } else {

                selectedFilterCategories.add(value)

                updateMainCategoryUI(
                    textView,
                    true
                )
            }

            applyFilterAndSort()
        }
    }

    private fun restoreCategory(
        textView: TextView,
        value: String
    ) {

        updateDialogCategoryUI(
            textView,
            selectedCategories.contains(value)
        )
    }

    private fun updateMainCategoryUI(
        textView: TextView,
        isSelected: Boolean
    ) {

        if (isSelected) {

            textView.setBackgroundResource(
                R.drawable.bg_category_selected
            )

            textView.setTextColor(
                getColor(R.color.primary)
            )

        } else {

            textView.setBackgroundResource(
                R.drawable.bg_category_unselected
            )

            textView.setTextColor(
                getColor(R.color.accent)
            )
        }
    }

    private fun updateDialogCategoryUI(
        textView: TextView,
        isSelected: Boolean
    ) {

        if (isSelected) {

            textView.setBackgroundResource(
                R.drawable.bg_category_dialog_selected
            )

            textView.setTextColor(
                getColor(R.color.accent)
            )

        } else {

            textView.setBackgroundResource(
                R.drawable.bg_category_dialog_unselected
            )

            textView.setTextColor(
                getColor(R.color.primary)
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}