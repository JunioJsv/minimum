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
import juniojsv.minimum.features.preferences.PreferencesActivity
import juniojsv.minimum.features.preferences.PreferencesActivity.Keys.ACCENT_COLOR
import juniojsv.minimum.features.preferences.PreferencesActivity.Keys.DARK_MODE
import juniojsv.minimum.features.preferences.PreferencesActivity.Keys.GRID_VIEW
import juniojsv.minimum.features.preferences.PreferencesActivity.Keys.GRID_VIEW_COLUMNS

class MinimumActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: MinimumActivityBinding
    private lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        preferences.registerOnSharedPreferenceChangeListener(this)
        appearanceHandler(preferences)

        binding = MinimumActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.pages.apply {
            adapter = MinimumPages(supportFragmentManager)
            setPageTransformer(true, MinimumPages.DEFAULT_PAGE_TRANSFORMER)
            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {}
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.minimum_shortcuts, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.dial ->
                startActivity(Intent(Intent.ACTION_DIAL))

            R.id.camera ->
                startActivity(
                    Intent.createChooser(
                        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA),
                        getString(R.string.take_pictures_with)
                    )
                )

            R.id.preferences ->
                startActivity(Intent(this, PreferencesActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        preferences.unregisterOnSharedPreferenceChangeListener(this)
        binding.pages.clearOnPageChangeListeners()
    }

    override fun onBackPressed() {
        // Nothings
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DARK_MODE, ACCENT_COLOR, GRID_VIEW, GRID_VIEW_COLUMNS -> {
                recreate()
            }
        }
    }
}
