package com.example.mysocialproject.ui.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.text.method.TransformationMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ViewEditTextBinding

const val DEFAULT_CHARACTER_PASSWORD = '●'

class AppEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mViewBinding = ViewEditTextBinding.inflate(layoutInflater, this, true)
    // Biến cờ để theo dõi trạng thái mật khẩu (ẩn hay hiện)
    private var isPasswordVisible = false
    init {
        val o = context.obtainStyledAttributes(attrs, R.styleable.AppEditText, 0, 0)
        try {
            val drawableRight = o.getDrawable(R.styleable.AppEditText_edtDrawableRight)
            val label = o.getString(R.styleable.AppEditText_edtLabel)
            val labelTextSize = o.getDimensionPixelSize(R.styleable.AppEditText_edtLabelTextSize, 0)
            val labelTextColor = o.getColor(R.styleable.AppEditText_edtLabelTextColor, 0)
            val textSize = o.getDimensionPixelSize(R.styleable.AppEditText_edtTextSize, 0)
            val text = o.getString(R.styleable.AppEditText_edtText)
            val textColor = o.getColor(R.styleable.AppEditText_edtTextColor, 0)

            val hint = o.getString(R.styleable.AppEditText_edtHint)
            val errorTextSize = o.getDimensionPixelSize(R.styleable.AppEditText_edtErrorSize, 0)
            val errorTextColor = o.getColor(R.styleable.AppEditText_edtErrorColor, 0)
            val errorText = o.getString(R.styleable.AppEditText_edtError)
            val inputType =
                o.getInt(R.styleable.AppEditText_android_inputType, InputType.TYPE_CLASS_TEXT)
            val gravity = o.getInt(R.styleable.AppEditText_android_gravity, Gravity.START)
            val isEnable = o.getBoolean(R.styleable.AppEditText_android_enabled, true)

            if (label != null) {
                setLabel(label)
                if (labelTextSize != 0) {
                    setLabelTextSize(labelTextSize)
                }
            } else {
                isShowLabel(false)
            }

            if (text != null) {
                setText(text)
            }

            if (textSize != 0) {
                setTextSize(textSize.toFloat())
            }

            if (textColor != 0) {
                setTextColor(textColor)
            }

            if (drawableRight != null) {
                setRightDrawable(drawableRight)
            }

            if (hint != null) {
                setHint(hint)
            }

            if (labelTextColor != 0) {
                setLabelTextColor(labelTextColor)
            }

            setInputType(inputType)

            setGravity(gravity)

        } finally {
            o.recycle()
        }

    }

    // Hàm để chuyển đổi giữa mật khẩu ẩn và hiện
    fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ẩn mật khẩu
            setTransformationMethod(PasswordTransformationMethod.getInstance())
            setRightDrawable(ContextCompat.getDrawable(context, R.drawable.ic_eye_close)!!)
            isPasswordVisible = false
        } else {
            // Hiện mật khẩu
            setTransformationMethod(HideReturnsTransformationMethod.getInstance())
            setRightDrawable(ContextCompat.getDrawable(context, R.drawable.ic_eye)!!)
            isPasswordVisible = true
        }
    }

    fun setLabel(label: String) {
        mViewBinding.tvLabel.text = label
    }

    fun isShowLabel(isShow: Boolean) {
        mViewBinding.tvLabel.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    fun setLabelTextSize(labelTextSize: Int) {
        mViewBinding.tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelTextSize.toFloat())
    }

    fun setText(text: String) {
        mViewBinding.editText.setText(text)
    }

    fun setTextSize(textSize: Float) {
        mViewBinding.editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }


    fun setTextColor(textColor: Int) {
        mViewBinding.editText.setTextColor(textColor)
    }

    fun setLabelTextColor(labelTextColor: Int) {
        mViewBinding.tvLabel.setTextColor(labelTextColor)
    }

    fun addTextChangeListener(
        onTextChange: TextWatcher
    ) {
        mViewBinding.editText.addTextChangedListener(onTextChange)
    }

    fun setSelection(pos: Int) {
        mViewBinding.editText.setSelection(pos)
    }

    fun setRightDrawable(icon: Drawable) {
        mViewBinding.editText.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setRightDrawableClick(onClick: (DrawableClickListener.DrawablePosition) -> Unit) {
        val padding = 20 // Khoảng padding 20 pixels
        mViewBinding.editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val actionX = event.x.toInt()
                val actionY = event.y.toInt()
                val drawableLeft = mViewBinding.editText.compoundDrawables[0]
                val drawableTop = mViewBinding.editText.compoundDrawables[1]
                val drawableRight = mViewBinding.editText.compoundDrawables[2]
                val drawableBottom = mViewBinding.editText.compoundDrawables[3]

                // Kiểm tra click vào drawable bên trái
                if (drawableLeft?.bounds?.contains(actionX, actionY) == true) {
                    onClick(DrawableClickListener.DrawablePosition.LEFT)
                    return@setOnTouchListener true
                }

                // Kiểm tra click vào drawable phía trên
                if (drawableTop?.bounds?.contains(actionX, actionY) == true) {
                    onClick(DrawableClickListener.DrawablePosition.TOP)
                    return@setOnTouchListener true
                }

                // Kiểm tra click vào drawable bên phải
                if (drawableRight != null) {
                    val width = mViewBinding.editText.width
                    val height = mViewBinding.editText.height
                    val bounds = Rect(
                        width - drawableRight.intrinsicWidth - padding,
                        0 - padding,
                        width + padding,
                        height + padding
                    )
                    if (bounds.contains(actionX, actionY)) {
                        onClick(DrawableClickListener.DrawablePosition.RIGHT)
                        return@setOnTouchListener true
                    }
                }

                // Kiểm tra click vào drawable phía dưới
                if (drawableBottom?.bounds?.contains(actionX, actionY) == true) {
                    onClick(DrawableClickListener.DrawablePosition.BOTTOM)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }


    fun setTransformationMethod(transaction: TransformationMethod?) {
        mViewBinding.editText.transformationMethod = transaction
    }

    fun setHint(hint: String) {
        mViewBinding.editText.hint = hint
    }

    fun setMaxLine(maxLine: Int) {
        mViewBinding.editText.maxLines = maxLine
    }

    fun setInputType(ipType: Int) {
        mViewBinding.editText.inputType = ipType
    }

    fun setGravity(gravity: Int) {
        mViewBinding.editText.gravity = gravity
    }


    fun getText() = mViewBinding.editText.text.toString()
    fun clearText() = mViewBinding.editText.text.clear()


    fun addChangeTextListener(onTextChange: (s: String) -> Unit) {

        mViewBinding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                onTextChange(p0.toString())
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                onTextChange(p0.toString())
            }

        })

    }

    fun addAfterChangeTextListener(onTextChange: (s: String) -> Unit) {
        mViewBinding.editText.doAfterTextChanged { edt ->
            onTextChange(edt.toString())
        }
    }

}


interface DrawableClickListener {
    enum class DrawablePosition {
        TOP, BOTTOM, LEFT, RIGHT
    }

    fun onClick(target: DrawablePosition)
}