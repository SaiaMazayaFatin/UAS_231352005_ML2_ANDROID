package com.example.studentscorepredictor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.nav_simulation -> selectedFragment = SimulationFragment()
                R.id.nav_dataset -> selectedFragment = DatasetFragment()
                R.id.nav_features -> selectedFragment = FeaturesFragment()
                R.id.nav_architecture -> selectedFragment = ArchitectureFragment()
                R.id.nav_about -> selectedFragment = AboutFragment()
            }
            if (selectedFragment != null) {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_container, selectedFragment).commit()
            }
            true
        }

        // Atur halaman default saat aplikasi pertama kali dibuka
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_simulation
        }
    }
}