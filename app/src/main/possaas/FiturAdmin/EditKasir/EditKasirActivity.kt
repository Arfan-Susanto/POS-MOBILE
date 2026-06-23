package com.possaas.FiturAdmin.EditKasir

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.possaas.R
import com.yalantis.ucrop.UCrop
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.util.UUID

class EditKasirActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    private var currentPreview:
            CircleImageView? = null

    private lateinit var recyclerKasir:
            RecyclerView

    private lateinit var kasirAdapter:
            KasirAdapter

    private val kasirList =
        ArrayList<KasirModel>()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_kasir_edit
        )

        val btnBack =
            findViewById<LinearLayout>(
                R.id.btnBack
            )

        val btnTambahKasir =
            findViewById<LinearLayout>(
                R.id.btnTambahKasir
            )

        recyclerKasir =
            findViewById(
                R.id.recyclerKasir
            )

        // =========================
        // SETUP ADAPTER
        // =========================
        kasirAdapter =
            KasirAdapter(

                kasirList,

                onEditClick = { kasir ->

                    showKasirDialog(
                        true,
                        kasir
                    )
                },

                onDeleteClick = { kasir ->

                    showDeleteDialog(
                        kasir.uid
                    )
                }
            )

        recyclerKasir.layoutManager =
            LinearLayoutManager(this)

        recyclerKasir.adapter =
            kasirAdapter

        // =========================
        // LOAD DATA
        // =========================
        loadKasir()

        btnBack.setOnClickListener {

            finish()
        }

        btnTambahKasir.setOnClickListener {

            showKasirDialog(false)
        }
    }

    // =========================
    // LOAD KASIR
    // =========================
    private fun loadKasir() {

        FirebaseDatabase
            .getInstance()
            .getReference("users")
            .addValueEventListener(
                object : ValueEventListener {

                    override fun onDataChange(
                        snapshot: DataSnapshot
                    ) {

                        kasirList.clear()

                        for (data in snapshot.children) {

                            val role =
                                data.child("role")
                                    .value
                                    .toString()

                            if (role == "KASIR") {

                                val kasir =
                                    KasirModel(

                                        uid =
                                            data.key.toString(),

                                        email =
                                            data.child("email")
                                                .value
                                                .toString(),

                                        name =
                                            data.child("name")
                                                .value
                                                .toString(),

                                        short_name =
                                            data.child("short_name")
                                                .value
                                                .toString(),

                                        password =
                                            data.child("password")
                                                .value
                                                .toString(),

                                        role =
                                            role,

                                        profile_image =
                                            data.child(
                                                "profile_image"
                                            ).value.toString()
                                    )

                                kasirList.add(kasir)
                            }
                        }

                        kasirAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(
                        error: DatabaseError
                    ) {

                    }
                }
            )
    }

    // =========================
    // PICK IMAGE
    // =========================
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

                UCrop.of(uri, destination)
                    .withAspectRatio(1f, 1f)
                    .start(this)
            }
        }

    // =========================
    // UCROP RESULT
    // =========================
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

    // =========================
    // DIALOG TAMBAH / EDIT
    // =========================
    private fun showKasirDialog(
        isEdit: Boolean,
        kasir: KasirModel? = null
    ) {

        selectedImageUri = null

        val dialog = Dialog(this)

        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE
        )

        dialog.setContentView(
            R.layout.dialog_tambah_kasir
        )

        dialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        dialog.window?.setBackgroundDrawableResource(
            android.R.color.transparent
        )

        val txtTitleDialog =
            dialog.findViewById<TextView>(
                R.id.txtTitleDialog
            )

        val btnClose =
            dialog.findViewById<LinearLayout>(
                R.id.btnClose
            )

        val btnUpload =
            dialog.findViewById<LinearLayout>(
                R.id.btnUpload
            )

        val btnSelesai =
            dialog.findViewById<LinearLayout>(
                R.id.btnSelesai
            )

        val edtEmail =
            dialog.findViewById<EditText>(
                R.id.edtEmail
            )

        val edtNama =
            dialog.findViewById<EditText>(
                R.id.edtNama
            )

        val edtShort =
            dialog.findViewById<EditText>(
                R.id.edtShort
            )

        val edtPassword =
            dialog.findViewById<EditText>(
                R.id.edtPassword
            )

        val imgPreview =
            dialog.findViewById<CircleImageView>(
                R.id.imgPreview
            )

        currentPreview = imgPreview

        // =========================
        // MODE EDIT
        // =========================
        if (isEdit && kasir != null) {

            txtTitleDialog.text =
                "Silahkan Edit Dibawah Ini"

            edtEmail.setText(
                kasir.email
            )

            edtNama.setText(
                kasir.name
            )

            edtShort.setText(
                kasir.short_name
            )

            edtPassword.setText(
                kasir.password
            )

            Glide.with(this)
                .load(kasir.profile_image)
                .placeholder(R.drawable.logo_bfc)
                .into(imgPreview)

        } else {

            txtTitleDialog.text =
                "Silahkan Isi Dibawah Ini"

            Glide.with(this)
                .load(R.drawable.logo_bfc)
                .into(imgPreview)
        }

        btnClose.setOnClickListener {

            dialog.dismiss()
        }

        // =========================
        // PICK FOTO
        // =========================
        btnUpload.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            pickImage.launch(intent)
        }

        // =========================
        // SAVE
        // =========================
        btnSelesai.setOnClickListener {

            val email =
                edtEmail.text.toString().trim()

            val nama =
                edtNama.text.toString().trim()

            val shortName =
                edtShort.text.toString().trim()

            val password =
                edtPassword.text.toString().trim()

            if (
                email.isEmpty() ||
                nama.isEmpty() ||
                shortName.isEmpty() ||
                password.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Semua input wajib diisi",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // tambah wajib foto
            if (!isEdit && selectedImageUri == null) {

                Toast.makeText(
                    this,
                    "Foto wajib dipilih",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val uid =
                if (isEdit)
                    kasir?.uid ?: ""
                else
                    UUID.randomUUID().toString()

            // =========================
            // ADA FOTO BARU
            // =========================
            if (selectedImageUri != null) {

                MediaManager.get()
                    .upload(selectedImageUri)
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
                                    ).toString()

                                saveKasirData(

                                    uid,

                                    email,

                                    nama,

                                    shortName,

                                    password,

                                    imageUrl,

                                    isEdit,

                                    dialog
                                )
                            }

                            override fun onError(
                                requestId: String?,
                                error: ErrorInfo?
                            ) {

                                Toast.makeText(
                                    this@EditKasirActivity,
                                    "Upload foto gagal",
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

            }

            // =========================
            // TANPA GANTI FOTO
            // =========================
            else {

                saveKasirData(

                    uid,

                    email,

                    nama,

                    shortName,

                    password,

                    kasir?.profile_image ?: "",

                    isEdit,

                    dialog
                )
            }
        }

        dialog.show()
    }

    // =========================
    // SAVE FIREBASE
    // =========================
    private fun saveKasirData(

        uid: String,

        email: String,

        nama: String,

        shortName: String,

        password: String,

        imageUrl: String,

        isEdit: Boolean,

        dialog: Dialog

    ) {

        val data =
            hashMapOf<String, Any>(

                "email" to email,

                "name" to nama,

                "short_name" to shortName,

                "password" to password,

                "role" to "KASIR",

                "profile_image" to imageUrl
            )

        FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .setValue(data)
            .addOnSuccessListener {

                Toast.makeText(
                    this,

                    if (isEdit)
                        "Kasir berhasil diupdate"
                    else
                        "Kasir berhasil ditambahkan",

                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
    }

    // =========================
    // DELETE DIALOG
    // =========================
    private fun showDeleteDialog(
        uid: String
    ) {

        val dialog = Dialog(this)

        dialog.requestWindowFeature(
            Window.FEATURE_NO_TITLE
        )

        dialog.setContentView(
            R.layout.dialog_delete_kasir
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
                R.id.btnClose
            )

        val btnHapus =
            dialog.findViewById<LinearLayout>(
                R.id.btnHapus
            )

        btnClose.setOnClickListener {

            dialog.dismiss()
        }

        btnHapus.setOnClickListener {

            FirebaseDatabase
                .getInstance()
                .getReference("users")
                .child(uid)
                .removeValue()
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Kasir berhasil dihapus",
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Gagal menghapus kasir",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        dialog.show()
    }
}