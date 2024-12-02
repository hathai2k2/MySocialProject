package com.example.mysocialproject.ui.dialog

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.DialogChangeTextBinding


class AppChangeTextDialog(
    private val context: Context,
    private val label: String,
    private val onConfirm: (String) -> Unit
) : AlertDialog(context) {
    private lateinit var mViewBinding: DialogChangeTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = DialogChangeTextBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)
        setCancelable(false)
        mViewBinding.ivClose.setOnClickListener {
            dismiss()
        }
        mViewBinding.tvLabel.text = label
        mViewBinding.btnConfirm.setOnClickListener {
            onConfirm(mViewBinding.edtText.getText())
            dismiss()
        }
    }


}