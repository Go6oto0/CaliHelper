package com.georgiyordanov.calihelper

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

@GlideModule
class CustomGlideModule : AppGlideModule() {
    override fun registerComponents(
        context: Context,
        glide: Glide,
        registry: Registry
    ) {
        // OkHttpClient that adds User-Agent and Referer headers
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val newReq: Request = original.newBuilder()
                    .header("User-Agent", "CaliHelperApp/1.0")
                    .header("Referer", "https://cdn-exercisedb.vercel.app")
                    .build()
                chain.proceed(newReq)
            }
            .build()

        // Tell Glide to use OkHttp for all GlideUrl loads
        registry.replace(
            GlideUrl::class.java,
            InputStream::class.java,
            OkHttpUrlLoader.Factory(client)
        )
    }
}
