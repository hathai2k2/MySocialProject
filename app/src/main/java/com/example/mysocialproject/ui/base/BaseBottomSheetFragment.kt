package com.example.mysocialproject.ui.base

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheetFragment<T : ViewDataBinding> : BottomSheetDialogFragment() {
    lateinit var mViewBinding: T

    abstract fun getLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return mViewBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                it.setBackgroundColor(resources.getColor(R.color.transparent))

                val layoutParams = it.layoutParams
                if (layoutParams != null) {
                    layoutParams.height = mViewBinding.root.height
                }
                it.layoutParams = layoutParams

                // Kiểm tra trạng thái bàn phím
                if (isKeyboardVisible()) {
                    // Nếu bàn phím đang hiển thị, không cho phép BottomSheet bị đẩy lên
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                } else {
                    // Nếu bàn phím không hiển thị, để BottomSheet bình thường
                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }

                // Cài đặt callback để kiểm soát hành vi khi kéo thả
                behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                    override fun onStateChanged(bottomSheet: View, newState: Int) {
                        if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                            // Nếu đang kéo, giữ trạng thái expanded
                            behavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {}
                })
            }
        }
        return dialog
    }

    // Hàm kiểm tra bàn phím có đang hiển thị hay không
    private fun isKeyboardVisible(): Boolean {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val inProp = requireActivity().currentFocus
        return inProp != null && imm.isAcceptingText
    }
}