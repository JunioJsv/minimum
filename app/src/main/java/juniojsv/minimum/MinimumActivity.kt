package juniojsv.minimum

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import juniojsv.minimum.databinding.MinimumActivityBinding
import juniojsv.minimum.preferences.PreferencesActivity
import juniojsv.minimum.preferences.PreferencesActivity.Keys.ACCENT_COLOR
import juniojsv.minimum.preferences.PreferencesActivity.Keys.DARK_MODE
import juniojsv.minimum.preferences.PreferencesActivity.Keys.GRID_VIEW
import juniojsv.minimum.preferences.PreferencesActivity.Keys.GRID_VIEW_COLUMNS
import juniojsv.minimum.preferences.PreferencesHandler

class MinimumActivity : AppCompatActivity(), PreferencesHandler.OnPreferenceChangeListener {
    private lateinit var binding: MinimumActivityBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        appearanceHandler(preferences)

        binding = MinimumActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mPages.apply {
            adapter = MinimumPages(supportFragmentManager)
            setPageTransformer(true, MinimumPages.DEFAULT_PAGE_TRANSFORMER)
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    binding.mPageIndicator.setSelectedPage(position)
                }
            })
        }

        PreferencesHandler.addListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.minimum_shortcuts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mDial ->
                startActivity(Intent(Intent.ACTION_DIAL))
            R.id.mCamera ->
                startActivity(
                        Intent.createChooser(
                                Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                                getString(R.string.take_pictures_with)
                        )
                )
            R.id.mPreferences ->
                startActivity(Intent(this, PreferencesActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferencesHandler.removeListener(this)
        binding.mPages.clearOnPageChangeListeners()
    }

    override fun onBackPressed() {
        // Nothings
    }

    override fun onPreferenceChange(key: String) {
        when (key) {
            DARK_MODE, ACCENT_COLOR, GRID_VIEW, GRID_VIEW_COLUMNS -> {
                recreate()
            }
        }
    }
}
