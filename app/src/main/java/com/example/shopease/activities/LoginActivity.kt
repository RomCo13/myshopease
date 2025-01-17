package com.example.shopease.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shopease.R
import com.example.shopease.dataClasses.User
import com.example.shopease.dbHelpers.UsersDatabaseHelper
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var dbHelper: UsersDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        usernameEditText = findViewById(R.id.usernameLoginEditText)
        passwordEditText = findViewById(R.id.passwordLoginEditText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
        dbHelper = UsersDatabaseHelper(applicationContext)

        // Check for locally stored user credentials
//        checkForLocallyStoredUser()
    }

    private fun checkForLocallyStoredUser() {
        dbHelper.getLocallyStoredUser()?.let { user ->
            showToast("ברוכים השבים, $${user.username}!")
            navigateToHomeActivity()
            finish()
        }
    }

    fun onLoginButtonClick(view: View) {
        val email = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Check if the login is valid
        dbHelper.login(email, password, object : UsersDatabaseHelper.LoginCallback {
            override fun onLoginResult(user: User?) {
                if (user != null) {
                    showToast("התחברת בהצלחה, ${user.username}!")
                    navigateToHomeActivity()
                    finish()
                } else {
                    // Login failed, user object is null
                    showToast("בעיה בהתחברות.")
                }
            }
        })
    }

    fun onSignUpButtonClick(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToHomeActivity() {
        // Pass user information to HomeActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}