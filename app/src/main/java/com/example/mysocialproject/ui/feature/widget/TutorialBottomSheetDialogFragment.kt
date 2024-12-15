package com.example.mysocialproject.ui.feature.widget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.BotttomDialogHdThemwidgetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TutorialBottomSheetDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val DIALOG_TYPE_WIDGETCHAT = 0
        const val DIALOG_TYPE_WIDGETPOST = 1
        const val KEY_DIALOG_TYPE = "dialogTypeWidget"
    }

    private var dialogType: Int = DIALOG_TYPE_WIDGETCHAT
    private lateinit var binding: BotttomDialogHdThemwidgetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.botttom_dialog_hd_themwidget,
            container,
            false
        )
        arguments?.getInt(KEY_DIALOG_TYPE)?.let {
            dialogType = it
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
//            if(dialogType == DIALOG_TYPE_WIDGETPOST){
//                image1.setImageResource(R.drawable.stepone)
//                image11.setImageResource(R.drawable.stepdotone)
//                image2.setImageResource(R.drawable.steptwo)
//                image21.setImageResource(R.drawable.stepttwo)
//                image3.setImageResource(R.drawable.stepthree)
//                image31.setImageResource(R.drawable.steplastpost)
//            }else{
//                image1.setImageResource(R.drawable.stepone)
//                image11.setImageResource(R.drawable.stepdotone)
//                image2.setImageResource(R.drawable.steptwo)
//                image21.setImageResource(R.drawable.stepttwo)
//                image3.setImageResource(R.drawable.stepthreechat)
//                image31.setImageResource(R.drawable.steplastchat)
//            }
        }
    }
}