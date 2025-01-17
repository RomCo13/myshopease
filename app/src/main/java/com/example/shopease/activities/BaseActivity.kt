package com.example.shopease.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.shopease.InterfaceFragmentTitle
import com.example.shopease.R
import com.example.shopease.dataClasses.User
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.example.shopease.fragments.HomeFragment
import com.example.shopease.fragments.ProfileFragment
import com.example.shopease.fragments.SavedPlaceFragment
import com.example.shopease.friends.FriendsFragment
import com.example.shopease.wishLists.WishlistsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

open class BaseActivity : AppCompatActivity(), InterfaceFragmentTitle {
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var dbHelper: UsersDatabaseHelper

    var username: String? = null
    var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = UsersDatabaseHelper(this)
        user = dbHelper.getLocallyStoredUser()
        username = user?.username
    }

    internal fun setUpUpperNavBar() {
        val inflater = LayoutInflater.from(this)
        val customUpperNavBar = inflater.inflate(R.layout.upper_nav_bar, null)

        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.customView = customUpperNavBar
    }

    internal fun setBottomNavBar() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.action_wishlist -> {
                    loadFragment(WishlistsFragment())
                    true
                }

                R.id.action_home -> {
                    loadFragment(HomeFragment())
                    true
                }

                R.id.action_friends -> {
                    loadFragment(FriendsFragment())
                    true
                }


                else -> false
            }
        }

        // Set the "Home" item as the default selected item
        bottomNavigation.selectedItemId = R.id.action_home
    }

    fun updateNavigationBarToWishlists() {
        bottomNavigation.selectedItemId = R.id.action_wishlist
    }

    override fun updateTitle(title: String) {
        // Update the title in your custom upper navigation bar
        val fragmentTitle: TextView = findViewById(R.id.title)
        fragmentTitle.text = title
    }

    fun loadFragment(fragment: Fragment, args: Bundle? = null, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()

        // Set arguments if provided
        fragment.arguments = args

        // Replace the existing fragment with the new one
        transaction.replace(R.id.fragmentContainer, fragment)

        // Add to back stack if needed
        if (addToBackStack) {
            transaction.addToBackStack(null)
                .setReorderingAllowed(true)
        }

        // Commit the transaction
        transaction.commit()
    }

    fun onProfileButtonClick(view: View) {
        val profileFragment = ProfileFragment()
        loadFragment(profileFragment)
    }

    fun onBackButtonClick(view: View) {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}
