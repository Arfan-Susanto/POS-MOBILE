package com.possaas

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager
import com.cloudinary.utils.ObjectUtils

object CloudinaryManager {

    val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", "delpu0vif",
            "api_key", "231161664589315",
            "api_secret", "ijgMwgBHf1BvGcHXCD0wHvAHdJc"
        )
    )

    fun init(context: Context) {

        val config =
            HashMap<String, String>()

        config["cloud_name"] =
            "delpu0vif"

        config["api_key"] =
            "231161664589315"

        config["api_secret"] =
            "ijgMwgBHf1BvGcHXCD0wHvAHdJc"

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