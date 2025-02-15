package com.example.mysocialproject.ui.custom_view

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.mysocialproject.R
import com.example.mysocialproject.databinding.ViewButtonBinding
import com.example.mysocialproject.extension.OnSingleClickListener
import com.example.mysocialproject.extension.onSingleClick


class AppButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val layoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val mViewBinding = ViewButtonBinding.inflate(layoutInflater, this, true)
    private val defaultBackground = ContextCompat.getDrawable(context, R.drawable.bg_button)
    private val defautTextColor = ContextCompat.getColor(context, R.color.white)
    private val defautBackgroundTintableBackgroundView =
        ContextCompat.getColor(context, R.color.colorSecondary)

    init {
        val o = context.obtainStyledAttributes(attrs, R.styleable.AppButton, 0, 0)
        try {
            val text = o.getString(R.styleable.AppButton_btnText)
            val enable = o.getBoolean(R.styleable.AppButton_btnEnable, true)
            val background = o.getDrawable(R.styleable.AppButton_btnBackground)
            val textColor = o.getColor(R.styleable.AppButton_btnTextColor, defautTextColor)
            val paddingVertical =
                o.getDimensionPixelSize(R.styleable.AppButton_btnPaddingVertical, 0)
            val paddingHorizontal =
                o.getDimensionPixelSize(R.styleable.AppButton_btnPaddingHorizontal, 0)
            val textSize = o.getDimensionPixelSize(R.styleable.AppButton_btnTextSize, 0)
            val btnFontFamilyId = o.getResourceId(R.styleable.AppButton_btnFontFamily, 0)
            val btnIcon = o.getDrawable(R.styleable.AppButton_btnIconLeft)
            val btnIconRight = o.getDrawable(R.styleable.AppButton_btnIconRight)
            val btnBgTin =
                o.getColor(R.styleable.AppButton_btnBgTint, defautBackgroundTintableBackgroundView)


            if (text != null) {
                setText(text)
            }
            setEnableButton(enable)

            if (background != null) {
                setButtonBackground(background)
            }

            setTextColor(textColor)

            if (paddingVertical != 0) {
                setPaddingVertical(paddingVertical)
            }

            if (paddingHorizontal != 0) {
                setPaddingHorizontal(paddingHorizontal)
            }

            if (textSize != 0) {
                setTextSize(textSize.toFloat())
            }

            setBtnFontFamily(btnFontFamilyId)
            if (btnIcon != null) {
                mViewBinding.ivIcon.visibility = View.VISIBLE
                setButtonIcon(btnIcon)
            } else {
                mViewBinding.ivIcon.visibility = View.GONE
            }
            if (btnIconRight != null) {
                mViewBinding.ivIconRight.visibility = View.VISIBLE
                setButtonIconRight(btnIconRight)
            } else {
                mViewBinding.ivIconRight.visibility = View.GONE
            }

            setBgTin(btnBgTin)
        } finally {
            o.recycle()
        }
    }

    fun showProgressBar(isProgressBar: Boolean) {
        mViewBinding.progressBar.visibility = if (isProgressBar) View.VISIBLE else View.GONE
    }

    private fun setBgTin(btnBgTin: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mViewBinding.btn.background.colorFilter =
                BlendModeColorFilter(btnBgTin, BlendMode.SRC_ATOP)
        } else {
            mViewBinding.btn.background.setColorFilter(
                btnBgTin,
                PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    private fun setButtonIconRight(btnIconRight: Drawable) {
        mViewBinding.ivIconRight.setImageDrawable(btnIconRight)
    }

    private fun setButtonIcon(btnIcon: Drawable) {
        mViewBinding.ivIcon.setImageDrawable(btnIcon)
    }

    fun setTextColor(textColor: Int) {
        mViewBinding.tv.setTextColor(textColor)
    }

    private fun setBtnFontFamily(btnFontFamilyId: Int) {
        if (btnFontFamilyId == 0) return
        mViewBinding.tv.typeface = ResourcesCompat.getFont(context, btnFontFamilyId)

    }

    fun setEnableButton(enable: Boolean) {
        mViewBinding.btn.isEnabled = enable
        mViewBinding.btn.isClickable = enable
    }

    fun setText(button: String) {
        mViewBinding.tv.text = button
    }

    fun setPaddingVertical(vertical: Int) {
        val layoutParams = mViewBinding.tv.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.bottomMargin = vertical
        layoutParams.topMargin = vertical
    }

    fun setPaddingHorizontal(padding: Int) {
        mViewBinding.btn.setPadding(
            padding,
            mViewBinding.btn.paddingTop,
            padding,
            mViewBinding.btn.paddingBottom
        )
    }

    fun setButtonClickListener(onClick: () -> Unit) {
        mViewBinding.btn.onSingleClick(object : OnSingleClickListener(10) {
            override fun onSingleClick(v: View?) {
                onClick()
            }
        })
    }

    fun setFastClickListener(onClick: () -> Unit) {
        mViewBinding.btn.setOnClickListener {
            onClick()
        }
    }

    fun setTextSize(textSize: Float) {
        mViewBinding.tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }


    fun setButtonBackground(background: Drawable) {
        mViewBinding.btn.background = background
    }
}