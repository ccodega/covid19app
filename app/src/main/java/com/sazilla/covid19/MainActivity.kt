package com.sazilla.covid19

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.sazilla.covid19.fragment.NationalChartFragment
import com.sazilla.covid19.fragment.ProvincialChartFragment
import com.sazilla.covid19.fragment.RegionalChartFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return if (item.itemId == R.id.menuid_info) { showAboutDialog(); true } else { false }
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_dialog_title, getString(R.string.app_name)))
            .setMessage(getString(R.string.about_dialog_message, BuildConfig.VERSION_NAME, getString(R.string.developer_contact), getString(R.string.data_source_site)))
            .setPositiveButton(getString(R.string.about_dialog_ok_button)) { dialog, _ -> dialog.cancel() }
            .setCancelable(true)
            .create().show()
    }
}

private val TAB_TITLES = arrayOf(
    R.string.navigation_title_italia,
    R.string.navigation_title_regioni,
    R.string.navigation_title_province
)

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) =
        when(position) {
            0 -> NationalChartFragment()
            1 -> RegionalChartFragment()
            2 -> ProvincialChartFragment()
            else -> throw IllegalArgumentException()
        }

    override fun getPageTitle(position: Int) = context.getString(TAB_TITLES[position])

    override fun getCount() = 3
}