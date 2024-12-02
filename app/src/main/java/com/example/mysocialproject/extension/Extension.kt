package com.example.mysocialproject.extension

import android.content.Context
import android.content.res.Resources
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.mysocialproject.R


fun View.onSingleClick(onSingleClickListener: View.OnClickListener) {
    this.setOnClickListener(object : OnSingleClickListener() {
        override fun onSingleClick(v: View?) {
            onSingleClickListener.onClick(v)
        }
    })
}

fun Context.showSoftKeyboard(view: View) {
    if (view.requestFocus()) {
        val imm = getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun Context.hideKeyboard(view: View) {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    imm?.hideSoftInputFromWindow(view.windowToken, 0)
}


fun Resources.dpToPx(dip: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dip,
        this.displayMetrics
    )
}

fun transformationMethodPassword(ivEye: ImageView, edtPass: EditText) {
    ivEye.setOnClickListener {
        val currentTrans = edtPass.transformationMethod
        if (currentTrans is PasswordTransformationMethod) {
            ivEye.setImageResource(R.drawable.ic_eye)
            edtPass.transformationMethod = null
            edtPass.setSelection(edtPass.length())
        } else {
            ivEye.setImageResource(R.drawable.ic_eye_close)
            edtPass.transformationMethod = PasswordTransformationMethod()
            edtPass.setSelection(edtPass.length())
        }
    }
}
fun ImageView.loadImage(url: String?, placeholder: Int = R.drawable.ic_user, error: Int = R.drawable.ic_user) {
    Glide.with(this)
        .load(url)
        .placeholder(placeholder) // Ảnh hiển thị trong khi tải
        .error(error) // Ảnh hiển thị khi tải thất bại
        .into(this)
}