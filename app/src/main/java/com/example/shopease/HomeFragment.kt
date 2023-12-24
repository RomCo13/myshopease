// src/main/java/com/example/myloginapp/HomeFragment.kt
package com.example.myloginapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        (activity as AppCompatActivity?)?.supportActionBar?.title = "Home"
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
}
