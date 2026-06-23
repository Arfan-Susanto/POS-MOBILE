package com.possaas

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.utils.ObjectUtils

object CloudinaryManager {

    val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", "YOUR_CLOUD_NAME",
            "api_key", "YOUR_API_KEY",
            "api_secret", "YOUR_API_KEY_SECRET"
        )
    )

    fun init(context: Context) {

        val config =
            HashMap<String, String>()

        config["cloud_name"] =
            "YOUR_CLOUD_NAME"

        config["api_key"] =
            "YOUR_API_KEY"

        config["api_secret"] =
            "YOUR_API_KEY_SECRET"

        try {

            MediaManager.init(
                context,
                config
            )

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}
