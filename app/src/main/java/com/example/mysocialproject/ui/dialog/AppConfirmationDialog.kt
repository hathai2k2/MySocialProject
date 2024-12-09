package com.example.mysocialproject.ui.dialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.DialogAppConfirmationBinding
import com.example.mysocialproject.databinding.DialogChangeTextBinding


class AppConfirmationDialog(
    private val context: Context,
    private val label: String,
    private val message:String,
    private val onConfirm: () -> Unit,
) : AlertDialog(context) {
    private lateinit var mViewBinding : DialogAppConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = DialogAppConfirmationBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)
        mViewBinding.tvLabel.text = label
        mViewBinding.tvMessager.text = message
        mViewBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mViewBinding.btnConfirm.setButtonClickListener {
            onConfirm()
            dismiss()
        }

        mViewBinding.btnCancel.setButtonClickListener {
            dismiss()
        }
    }

}