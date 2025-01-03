

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton



class FABFloatOnScroll(context: Context, attrs: AttributeSet?) :
    FloatingActionButton.Behavior(context, attrs) {
    var fabScrollListener: FABScrollListener? = null
    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            consumed
        )
        // Di chuyển Floating Action Button khi scroll
        if (dyConsumed > 0) { // Scroll xuống
            fabScrollListener?.onFABHidden()
            val layoutParams = child.layoutParams as CoordinatorLayout.LayoutParams
            val fabBottomMargin = layoutParams.bottomMargin
            child.animate()
                .translationY((child.height + fabBottomMargin).toFloat())
                .setInterpolator(LinearInterpolator())
                .start()
        } else if (dyConsumed < 0) { // Scroll lên
            child.animate()
                .translationY(0f)
                .setInterpolator(LinearInterpolator())
                .start()
        }
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }
}
interface FABScrollListener {
    fun onFABHidden()
}