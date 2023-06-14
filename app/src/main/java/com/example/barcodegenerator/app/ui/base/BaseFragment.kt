package com.example.barcodegenerator.app.ui.base

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.barcodegenerator.R

/**
 * Created by AralBenli on 12.06.2023.
 */
abstract class BaseFragment<VB : ViewBinding> :
    Fragment() {
    lateinit var binding: VB

    abstract fun getViewBinding(): VB
    abstract fun initViews()
    private var savedInstanceView: View? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (savedInstanceView == null) {
            onCreateViewBase()
            binding = getViewBinding()
            savedInstanceView = binding.root
            savedInstanceView
        } else {
            savedInstanceView
        }


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observer()
    }
    fun showCustomToast(status : Status, message: String, fragment: Fragment) {

        val layout = fragment.layoutInflater.inflate(R.layout.custom_toast_layout,null)

        val textView = layout.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        val imageView = layout.findViewById<ImageView>(R.id.titleIcon)
        when(status){
            Status.Success -> imageView.setImageResource(R.drawable.icon_success)
            Status.Fail -> imageView.setImageResource(R.drawable.icon_failed)
            Status.Warn -> imageView.setImageResource(R.drawable.icon_warn)
        }

        val myToast = Toast(this.context)
        myToast.duration = Toast.LENGTH_SHORT
        myToast.setGravity(Gravity.BOTTOM, 0, 200)
        myToast.view = layout//setting the view of custom toast layout
        myToast.show()

    } enum class Status {
        Success,
        Fail ,
        Warn
    }
    open fun onCreateViewBase() {}
    open fun observer() {}
}