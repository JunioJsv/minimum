package juniojsv.minimum

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import juniojsv.minimum.databinding.MinimumPageIndicatorViewBinding

class MinimumPageIndicator(context: Context, attr: AttributeSet) : FrameLayout(context, attr) {
    private var binding = MinimumPageIndicatorViewBinding.inflate(LayoutInflater.from(context))

    init {
        addView(binding.root)
        applySelect(binding.mHomeIcon)
    }

    private fun applySelect(view: View) {
        with(view.animate()) {
            scaleX(SELECTED_SCALE).duration = SCALE_DURATION
            scaleY(SELECTED_SCALE).duration = SCALE_DURATION
            alpha(STANDARD_ALPHA).duration = ALPHA_DURATION
        }
    }

    private fun applyUnSelect(view: View) {
        with(view.animate()) {
            scaleX(STANDARD_SCALE).duration = SCALE_DURATION
            scaleY(STANDARD_SCALE).duration = SCALE_DURATION
            alpha(UNSELECTED_ALPHA).duration = ALPHA_DURATION
        }
    }

    fun setSelectedPage(index: Int) {
        when (index) {
            0 -> {
                applySelect(binding.mHomeIcon)
                applyUnSelect(binding.mWidgetsIcon)
            }
            1 -> {
                applySelect(binding.mWidgetsIcon)
                applyUnSelect(binding.mHomeIcon)
            }
        }
    }

    companion object {
        const val SELECTED_SCALE = 1.28f
        const val STANDARD_SCALE = 1f
        const val UNSELECTED_ALPHA = .6f
        const val STANDARD_ALPHA = 1f
        const val SCALE_DURATION = 350L
        const val ALPHA_DURATION = 250L
    }
}