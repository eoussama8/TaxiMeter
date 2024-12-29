package com.example.taximeter

import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.taximeter.databinding.ActivityMainBinding
import com.example.taximeter.ui.history.HistoryFragment
import com.example.taximeter.ui.maps.MapsFragment
import com.example.taximeter.ui.meter.MeterFragment
import com.example.taximeter.ui.profile.ProfileFragment
import com.example.taximeter.ui.settings.SettingsFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the navigation drawer
        setupNavigationDrawer()

        // Load the default fragment if no state is saved
        if (savedInstanceState == null) {
            replaceFragment(MapsFragment(), getString(R.string.menu_maps))
        }
    }

    private fun setupNavigationDrawer() {
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_maps -> {
                    replaceFragment(MapsFragment(), getString(R.string.menu_maps))
                }
                R.id.nav_meter -> {
                    replaceFragment(MeterFragment(), getString(R.string.menu_meter))
                }
                R.id.nav_settings -> {
                    replaceFragment(SettingsFragment(), getString(R.string.menu_settings))
                }
                R.id.nav_history -> {
                    replaceFragment(HistoryFragment() ,getString(R.string.menu_settings))
                }
                R.id.nav_profile -> {
                    replaceFragment(ProfileFragment() ,getString(R.string.menu_profile))
                }
            }
            menuItem.isChecked = true // Highlight the selected menu item
            drawerLayout.closeDrawer(Gravity.START) // Close the drawer
            true
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // Optional: Add to back stack for navigation
            .commit()

        supportActionBar?.title = title // Update the title
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.START)) {
            binding.drawerLayout.closeDrawer(Gravity.START) // Close the drawer if open
        } else {
            super.onBackPressed() // Otherwise, use default behavior
        }
    }
}
