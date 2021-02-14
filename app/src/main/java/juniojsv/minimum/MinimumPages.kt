package juniojsv.minimum

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import juniojsv.minimum.applications.ApplicationsFragment
import juniojsv.minimum.widgets.WidgetsFragment

class MinimumPages(fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val fragments: Array<Fragment> = arrayOf(ApplicationsFragment(), WidgetsFragment())

    override fun getCount(): Int = fragments.size

    override fun getItem(position: Int): Fragment = fragments[position]

    companion object {
        val DEFAULT_PAGE_TRANSFORMER = ViewPager.PageTransformer { page, position ->
            page.apply {
                val pageWidth = width
                when {
                    position < -1 -> {
                        alpha = 0f
                    }
                    position <= 0 -> {
                        alpha = 1f
                        translationX = 0f
                    }
                    position <= 1 -> {
                        alpha = 1 - position
                        translationX = pageWidth * -position
                    }
                    else -> {
                        alpha = 0f
                    }
                }
            }
        }
    }
}