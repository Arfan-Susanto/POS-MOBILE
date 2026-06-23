package com.possaas.Admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.possaas.CloudinaryManager
import com.possaas.R
import com.possaas.RoleActivity
import com.possaas.SessionManager
import com.yalantis.ucrop.UCrop
import java.io.File

class ProfileAdminFragment : Fragment() {

    private lateinit var imgProfile: ImageView

    private lateinit var auth: FirebaseAuth

    companion object {

        private const val PICK_IMAGE = 100
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_profile_admin,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        imgProfile =
            view.findViewById(R.id.imgProfile)

        val btnChangePhoto =
            view.findViewById<LinearLayout>(
                R.id.btnChangePhoto
            )

        val btnLogout =
            view.findViewById<LinearLayout>(
                R.id.btnLogout
            )

        // LOAD FOTO
        loadProfileImage()

        // =========================
        // CHANGE PHOTO
        // =========================
        btnChangePhoto.setOnClickListener {

            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            startActivityForResult(
                intent,
                PICK_IMAGE
            )
        }

        // =========================
        // LOGOUT
        // =========================
        btnLogout.setOnClickListener {

            auth.signOut()

            // CLEAR SESSION
            SessionManager(requireContext()).clearSession()

            val intent =
                Intent(
                    requireContext(),
                    RoleActivity::class.java
                )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
        }

        return view
    }

    // =========================
    // RESULT
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

        // PILIH FOTO
        if (
            requestCode == PICK_IMAGE &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {

            val sourceUri =
                data.data ?: return

            val destinationUri =
                Uri.fromFile(
                    File(
                        requireContext().cacheDir,
                        "cropped.jpg"
                    )
                )

            UCrop.of(
                sourceUri,
                destinationUri
            )
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)
                .start(requireContext(), this)
        }

        // HASIL CROP
        else if (
            requestCode == UCrop.REQUEST_CROP &&
            resultCode == Activity.RESULT_OK
        ) {

            val resultUri =
                UCrop.getOutput(data!!)

            if (resultUri != null) {

                // tampil langsung
                Glide.with(requireContext())
                    .load(resultUri)
                    .circleCrop()
                    .into(imgProfile)

                // upload
                uploadToCloudinary(resultUri)
            }
        }
    }

    // =========================
    // UPLOAD CLOUDINARY
    // =========================
    private fun uploadToCloudinary(
        imageUri: Uri
    ) {

        Thread {

            try {

                val result =
                    CloudinaryManager.cloudinary
                        .uploader()
                        .upload(
                            imageUri.path,
                            hashMapOf<String, Any>()
                        )

                val imageUrl =
                    result["secure_url"].toString()

                saveImageToFirebase(imageUrl)

            } catch (e: Exception) {

                e.printStackTrace()
            }

        }.start()
    }

    // =========================
    // SAVE URL FIREBASE
    // =========================
    private fun saveImageToFirebase(
        imageUrl: String
    ) {

        val uid =
            auth.currentUser?.uid ?: return

        FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .child("profile_image")
            .setValue(imageUrl)
    }

    // =========================
    // LOAD FOTO
    // =========================
    private fun loadProfileImage() {

        val uid =
            auth.currentUser?.uid ?: return

        FirebaseDatabase
            .getInstance()
            .getReference("users")
            .child(uid)
            .child("profile_image")
            .get()
            .addOnSuccessListener {

                if (it.exists()) {

                    val imageUrl =
                        it.value.toString()

                    Glide.with(requireContext())
                        .load(imageUrl)
                        .circleCrop()
                        .into(imgProfile)
                }
            }
    }
}