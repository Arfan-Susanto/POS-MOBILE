package com.possaas.Kasir

import android.app.Activity
import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.possaas.CloudinaryManager
import com.possaas.R
import com.possaas.Role.RoleActivity
import com.possaas.SessionManager
import com.yalantis.ucrop.UCrop
import java.io.File

class ProfileKasirFragment : Fragment() {

    private lateinit var txtName: TextView
    private lateinit var txtRole: TextView
    private lateinit var imgProfile: ImageView
    private lateinit var auth: FirebaseAuth
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    companion object {
        private const val PICK_IMAGE = 100
    }

    private fun getSavedEmail(): String? {
        return try {
            val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.getString("logged_email", null)
        } catch (e: Exception) {
            android.util.Log.e("ProfileKasir", "getSavedEmail error: ${e.message}")
            null
        }
    }

    private fun getSavedUserKey(): String? {
        return try {
            val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs.getString("logged_user_key", null)
        } catch (e: Exception) {
            android.util.Log.e("ProfileKasir", "getSavedUserKey error: ${e.message}")
            null
        }
    }

    private fun populateUserData(userSnapshot: DataSnapshot) {
        try {
            val name = userSnapshot.child("name").getValue(String::class.java) ?: "Nama tidak tersedia"
            txtName.text = name

            val role = userSnapshot.child("role").getValue(String::class.java) ?: "KASIR"
            txtRole.text = role.uppercase()

            val imageUrl = userSnapshot.child("profile_image").getValue(String::class.java)
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(imageUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .circleCrop()
                    .placeholder(R.drawable.logo_bfc)
                    .error(R.drawable.logo_bfc)
                    .into(imgProfile)
            } else {
                Glide.with(requireContext())
                    .load(R.drawable.logo_bfc)
                    .circleCrop()
                    .into(imgProfile)
            }
        } catch (e: Exception) {
            android.util.Log.e("ProfileKasir", "populateUserData exception: ${e.message}")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(
            R.layout.fragment_profile_kasir,
            container,
            false
        )

        auth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener {
            loadProfileData()
        }
        auth.addAuthStateListener(authStateListener!!)
        imgProfile = view.findViewById(R.id.imgProfile)
        txtName = view.findViewById(R.id.txtName)
        txtRole = view.findViewById(R.id.txtRole)

        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)

        view.post {
            loadProfileData()
        }

        btnLogout.setOnClickListener {
            auth.signOut()

            SessionManager(requireContext()).clearSession()

            val intent = Intent(
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

    override fun onDestroyView() {
        super.onDestroyView()
        authStateListener?.let {
            auth.removeAuthStateListener(it)
        }
    }
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (
            requestCode == PICK_IMAGE &&
            resultCode == Activity.RESULT_OK &&
            data != null
        ) {
            val sourceUri = data.data ?: return
            val destinationUri = Uri.fromFile(
                File(
                    requireContext().cacheDir,
                    "cropped.jpg"
                )
            )

            UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)
                .start(requireContext(), this)
        }

        // HASIL CROP
        else if (
            requestCode == UCrop.REQUEST_CROP &&
            resultCode == Activity.RESULT_OK
        ) {
            val resultUri = UCrop.getOutput(data!!)

            if (resultUri != null) {
                Glide.with(requireContext())
                    .load(resultUri)
                    .circleCrop()
                    .into(imgProfile)

                uploadToCloudinary(resultUri)
            }
        }
    }

    private fun uploadToCloudinary(imageUri: Uri) {
        Thread {
            try {
                val result = CloudinaryManager.cloudinary
                    .uploader()
                    .upload(
                        imageUri.path,
                        hashMapOf<String, Any>()
                    )

                val imageUrl = result["secure_url"].toString()

                if (imageUrl.isNotEmpty()) {
                    saveImageToFirebase(imageUrl)
                } else {
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error: URL kosong", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Upload gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun saveImageToFirebase(imageUrl: String) {
        val currentUserEmail = auth.currentUser?.email
        if (currentUserEmail.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Email user tidak ditemukan", Toast.LENGTH_SHORT).show()
            android.util.Log.e("ProfileKasir", "saveImageToFirebase: currentUser email null")
            return
        }

        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.orderByChild("email").equalTo(currentUserEmail.trim())
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userSnapshot = snapshot.children.firstOrNull()
                    if (userSnapshot != null) {
                        val uid = userSnapshot.key
                        android.util.Log.d("ProfileKasir", "saveImageToFirebase: found user uid=$uid for email=$currentUserEmail")
                        if (uid != null) {
                            usersRef.child(uid).child("profile_image").setValue(imageUrl)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Foto profile berhasil diupdate", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(requireContext(), "Gagal simpan ke Firebase: ${error.message}", Toast.LENGTH_SHORT).show()
                                    error.printStackTrace()
                                }
                            return@addOnSuccessListener
                        }
                    }
                    Toast.makeText(requireContext(), "User dengan email $currentUserEmail tidak ditemukan", Toast.LENGTH_SHORT).show()
                    android.util.Log.w("ProfileKasir", "saveImageToFirebase: snapshot exists but no children found for email=$currentUserEmail")
                } else {
                    Toast.makeText(requireContext(), "User dengan email $currentUserEmail tidak ditemukan", Toast.LENGTH_SHORT).show()
                    android.util.Log.w("ProfileKasir", "saveImageToFirebase: snapshot does not exist for email=$currentUserEmail")
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("ProfileKasir", "saveImageToFirebase error: ${error.message}")
                error.printStackTrace()
            }
    }

    private fun loadProfileData() {
        val currentUser = auth.currentUser
        
        android.util.Log.d("ProfileKasir", "currentUser: $currentUser")
        android.util.Log.d("ProfileKasir", "currentUser.uid: ${currentUser?.uid}")
        android.util.Log.d("ProfileKasir", "currentUser.email: ${currentUser?.email}")
        android.util.Log.d("ProfileKasir", "currentUser.email is null: ${currentUser?.email == null}")
        android.util.Log.d("ProfileKasir", "currentUser.email is empty: ${currentUser?.email.isNullOrEmpty()}")

        if (currentUser == null) {
            val savedKey = getSavedUserKey()
            if (!savedKey.isNullOrEmpty()) {
                android.util.Log.d("ProfileKasir", "currentUser null, loading user by saved key: $savedKey")
                FirebaseDatabase.getInstance().getReference("users").child(savedKey).get()
                    .addOnSuccessListener { snap ->
                        if (snap != null && snap.exists()) {
                            populateUserData(snap)
                        } else {
                            Toast.makeText(requireContext(), "User tidak ditemukan di database (saved key)", Toast.LENGTH_LONG).show()
                            android.util.Log.w("ProfileKasir", "Saved key present but user node not found: $savedKey")
                        }
                    }
                    .addOnFailureListener { err ->
                        Toast.makeText(requireContext(), "Error: ${err.message}", Toast.LENGTH_SHORT).show()
                        android.util.Log.e("ProfileKasir", "Error loading saved key user: ${err.message}")
                    }
                return
            }

            android.util.Log.d("ProfileKasir", "currentUser null, querying for role KASIR as fallback")
            val usersRef = FirebaseDatabase.getInstance().getReference("users")
            usersRef.orderByChild("role").equalTo("KASIR")
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot != null && snapshot.exists() && snapshot.childrenCount > 0) {
                        val userSnapshot = snapshot.children.firstOrNull()
                        if (userSnapshot != null) {
                            android.util.Log.d("ProfileKasir", "Fallback role KASIR found key=${userSnapshot.key}")
                            populateUserData(userSnapshot)
                            return@addOnSuccessListener
                        }
                    }
                    Toast.makeText(requireContext(), "User tidak login", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { err ->
                    android.util.Log.w("ProfileKasir", "Error querying KASIR role: ${err.message}. Falling back to full scan.")
                    usersRef.get()
                        .addOnSuccessListener { fullSnap ->
                            try {
                                for (userSnapshot in fullSnap.children) {
                                    val roleDb = userSnapshot.child("role").getValue(String::class.java)
                                    if (roleDb != null && roleDb.trim().equals("KASIR", ignoreCase = true)) {
                                        android.util.Log.d("ProfileKasir", "Fallback full scan found KASIR key=${userSnapshot.key}")
                                        populateUserData(userSnapshot)
                                        return@addOnSuccessListener
                                    }
                                }
                                Toast.makeText(requireContext(), "User tidak login", Toast.LENGTH_LONG).show()
                            } catch (e: Exception) {
                                android.util.Log.e("ProfileKasir", "Fallback full scan exception: ${e.message}")
                                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { err2 ->
                            android.util.Log.e("ProfileKasir", "Fallback full scan error: ${err2.message}")
                            Toast.makeText(requireContext(), "Error: ${err2.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            return
        }

        val currentUserEmail = currentUser.email

        if (currentUserEmail.isNullOrEmpty()) {
            android.util.Log.d("ProfileKasir", "Email kosong, mencoba reload user...")
            currentUser.reload().addOnSuccessListener {
                android.util.Log.d("ProfileKasir", "Reload success, email sekarang: ${currentUser.email}")
                if (!currentUser.email.isNullOrEmpty()) {
                    searchAndLoadUserData(currentUser.email!!)
                } else {
                    Toast.makeText(requireContext(), "Email masih kosong setelah reload", Toast.LENGTH_LONG).show()
                }
            }.addOnFailureListener { error ->
                android.util.Log.e("ProfileKasir", "Reload failed: ${error.message}")
                Toast.makeText(requireContext(), "Gagal reload: ${error.message}", Toast.LENGTH_SHORT).show()
            }
            return
        }

        searchAndLoadUserData(currentUserEmail)
    }

    private fun searchAndLoadUserData(currentUserEmail: String) {
        android.util.Log.d("ProfileKasir", "Searching for user with email: $currentUserEmail")

        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        usersRef.orderByChild("email").equalTo(currentUserEmail.trim()).get()
            .addOnSuccessListener { snapshot ->
                android.util.Log.d("ProfileKasir", "Query snapshot exists: ${snapshot.exists()}, childrenCount: ${snapshot.childrenCount}")
                if (snapshot.exists() && snapshot.childrenCount > 0) {
                    val userSnapshot = snapshot.children.firstOrNull()
                    if (userSnapshot == null) {
                        Toast.makeText(requireContext(), "User dengan email: '$currentUserEmail' tidak ditemukan di database", Toast.LENGTH_LONG).show()
                        android.util.Log.w("ProfileKasir", "No child found after query for email=$currentUserEmail")
                        return@addOnSuccessListener
                    }

                    android.util.Log.d("ProfileKasir", "User found! Key: ${userSnapshot.key}")

                    val name = userSnapshot.child("name").getValue(String::class.java) ?: "Nama tidak tersedia"
                    txtName.text = name
                    android.util.Log.d("ProfileKasir", "Name: $name")

                    val role = userSnapshot.child("role").getValue(String::class.java) ?: "KASIR"
                    txtRole.text = role.uppercase()
                    android.util.Log.d("ProfileKasir", "Role: $role")

                    val imageUrl = userSnapshot.child("profile_image").getValue(String::class.java)
                    android.util.Log.d("ProfileKasir", "ImageUrl: $imageUrl")

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .circleCrop()
                            .placeholder(R.drawable.logo_bfc)
                            .error(R.drawable.logo_bfc)
                            .into(imgProfile)
                    } else {
                        Glide.with(requireContext())
                            .load(R.drawable.logo_bfc)
                            .circleCrop()
                            .into(imgProfile)
                    }
                } else {
                    val message = "User dengan email: '$currentUserEmail' tidak ditemukan di database"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    android.util.Log.w("ProfileKasir", message)
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                android.util.Log.e("ProfileKasir", "Firebase Error: ${error.message}")
                error.printStackTrace()
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snap = task.result
                    if (snap == null || !snap.exists() || snap.childrenCount == 0L) {
                        android.util.Log.w("ProfileKasir", "equalTo query returned no results, trying case-insensitive scan")
                        FirebaseDatabase.getInstance().getReference("users").get()
                            .addOnSuccessListener { fullSnap ->
                                try {
                                    for (userSnapshot in fullSnap.children) {
                                        val emailDb = userSnapshot.child("email").getValue(String::class.java)
                                        if (emailDb != null && emailDb.trim().lowercase() == currentUserEmail.trim().lowercase()) {
                                            android.util.Log.d("ProfileKasir", "Fallback found user key=${userSnapshot.key} via case-insensitive match")
                                            val name = userSnapshot.child("name").getValue(String::class.java) ?: "Nama tidak tersedia"
                                            txtName.text = name
                                            val role = userSnapshot.child("role").getValue(String::class.java) ?: "KASIR"
                                            txtRole.text = role.uppercase()
                                            val imageUrl = userSnapshot.child("profile_image").getValue(String::class.java)
                                            if (!imageUrl.isNullOrEmpty()) {
                                                Glide.with(requireContext())
                                                    .load(imageUrl)
                                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                    .circleCrop()
                                                    .placeholder(R.drawable.logo_bfc)
                                                    .error(R.drawable.logo_bfc)
                                                    .into(imgProfile)
                                            } else {
                                                Glide.with(requireContext())
                                                    .load(R.drawable.logo_bfc)
                                                    .circleCrop()
                                                    .into(imgProfile)
                                            }
                                            return@addOnSuccessListener
                                        }
                                    }
                                    android.util.Log.w("ProfileKasir", "Fallback scan: no case-insensitive match found for email=$currentUserEmail")
                                } catch (e: Exception) {
                                    android.util.Log.e("ProfileKasir", "Fallback scan exception: ${e.message}")
                                }
                            }
                            .addOnFailureListener { err ->
                                android.util.Log.e("ProfileKasir", "Fallback full scan error: ${err.message}")
                            }
                    }
                }
            }
    }
}