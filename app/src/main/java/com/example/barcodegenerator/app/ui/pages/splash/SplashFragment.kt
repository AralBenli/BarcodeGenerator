package com.example.barcodegenerator.app.ui.pages.splash


import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.widget.ImageView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.barcodegenerator.R
import com.example.barcodegenerator.app.ui.base.BaseFragment
import com.example.barcodegenerator.databinding.FragmentSplashBinding
import java.io.IOException


class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    override fun getViewBinding(): FragmentSplashBinding =
        FragmentSplashBinding.inflate(layoutInflater)

    override fun initViews() {
        splash()
    }

    private fun splash() {
        val splashImage: ImageView = binding.splashImage

        Glide.with(this)
            .asGif()
            .load(R.drawable.barcode)
            .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
            .into(splashImage)

        Handler(Looper.getMainLooper()).postDelayed({
            view?.post {
                findNavController().navigate(R.id.homeFragment)
            }
        }, 4300)
    }
}

val contentValues = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, "barcode_image.png")
    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
}




