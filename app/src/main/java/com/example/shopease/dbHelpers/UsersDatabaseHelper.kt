package com.example.shopease.dbHelpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.shopease.dataClasses.User
import com.example.shopease.utils.Utils.byteArrayToBase64
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class UsersDatabaseHelper(context: Context) : BaseDatabaseHelper() {

    interface RegistrationCallback {
        fun onRegistrationResult(success: Boolean, user: User?)
    }

    interface LoginCallback {
        fun onLoginResult(user: User?)
    }

    interface GetUserCallback {
        fun onUserResult(user: User?)
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE)
    }

    fun registerUser(
        username: String,
        email: String,
        password: String,
        imageProfile: ByteArray?,
        callback: RegistrationCallback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val base64ImageProfile = byteArrayToBase64(imageProfile)
                        val newUser = User(user.uid, username, email, base64ImageProfile)

                        val userId = user.uid
                        val userRef = databaseReference.child("users").child(userId)
                        userRef.setValue(newUser)
                            .addOnCompleteListener { innerTask ->
                                if (innerTask.isSuccessful) {
                                    // Save user data locally after successful registration
                                    saveUserLocally(newUser)
                                    callback.onRegistrationResult(true, newUser)
                                } else {
                                    Log.e(
                                        "FirebaseHelper",
                                        "Error adding user to Realtime Database",
                                        innerTask.exception
                                    )
                                    callback.onRegistrationResult(false, null)
                                }
                            }
                    } else {
                        Log.e("FirebaseHelper", "Error getting current user after registration")
                        callback.onRegistrationResult(false, null)
                    }
                } else {
                    Log.e(
                        "FirebaseHelper",
                        "Error registering user with Firebase Authentication",
                        task.exception
                    )
                    callback.onRegistrationResult(false, null)
                }
            }
    }

    fun login(username: String, password: String, callback: LoginCallback) {
        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        val userId = user.uid
                        val userRef = databaseReference.child("users").child(userId)
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val userData = snapshot.getValue(User::class.java)
                                // Save user data locally after successful login
                                saveUserLocally(userData)
                                callback.onLoginResult(userData)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(
                                    "FirebaseHelper",
                                    "Error fetching user data from Realtime Database",
                                    error.toException()
                                )
                                callback.onLoginResult(null)
                            }
                        })
                    } else {
                        Log.e("FirebaseHelper", "Error getting current user after login")
                        callback.onLoginResult(null)
                    }
                } else {
                    Log.e(
                        "FirebaseHelper",
                        "Error logging in with Firebase Authentication",
                        task.exception
                    )
                    callback.onLoginResult(null)
                }
            }
    }

    fun isEmailExists(email: String, callback: (Boolean) -> Unit) {
        databaseReference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "FirebaseHelper",
                        "Error checking email existence",
                        error.toException()
                    )
                    callback(false)
                }
            })
    }

    fun isUsernameExists(username: String, callback: (Boolean) -> Unit) {
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback(snapshot.exists())
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "FirebaseHelper",
                        "Error checking username existence",
                        error.toException()
                    )
                    callback(false)
                }
            })
    }

    fun updateImage(username: String, newImageUrl: ByteArray?, callback: (Boolean) -> Unit) {
        val base64ImageProfile = byteArrayToBase64(newImageUrl)
        updateImageLocal(username, base64ImageProfile)
        val usersRef = databaseReference.child("users")
        usersRef.orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userId = snapshot.children.first().key
                        val userRef = usersRef.child(userId ?: "")
                        userRef.child("profileImage").setValue(base64ImageProfile)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    callback(true)
                                } else {
                                    Log.e(
                                        "FirebaseHelper",
                                        "Error updating profile image URL",
                                        task.exception
                                    )
                                    callback(false)
                                }
                            }
                    } else {
                        callback(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(
                        "FirebaseHelper",
                        "Error updating profile image URL",
                        error.toException()
                    )
                    callback(false)
                }
            })
    }

    fun getUserByUid(uid: String, callback: GetUserCallback) {
        val userRef = databaseReference.child("users").child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(User::class.java)
                if (userData != null) {
                    callback.onUserResult(userData)
                } else {
                    callback.onUserResult(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback.onUserResult(null)
            }
        })
    }

    fun updatePassword(newPassword: String, onCompleteListener: OnCompleteListener<Void>) {
        val user: FirebaseUser? = auth.currentUser

        user?.updatePassword(newPassword)
            ?.addOnCompleteListener(onCompleteListener)
    }

    fun logoutUser() {
        clearUserLocally()
        FirebaseManager.auth.signOut()
        Log.d("UserDatabaseHelper", "User logged out")
    }

    private fun saveUserLocally(user: User?) {
        val editor = sharedPreferences.edit()
        if (user != null) {
            editor.putString("userId", user.uid)
            editor.putString("username", user.username)
            editor.putString("email", user.email)
            editor.putString("profileImage", user.profileImage)
        } else {
            clearUserLocally()
        }
        editor.apply()
    }

    private fun updateImageLocal(username: String, newImageUrl: String) {
        val editor = sharedPreferences.edit()
        if (username == sharedPreferences.getString("username", "")) {
            editor.putString("profileImage", newImageUrl)
            editor.apply()
        }
    }

    private fun clearUserLocally() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }

    fun getLocallyStoredUser(): User? {
        val userId = sharedPreferences.getString("userId", null)
        val username = sharedPreferences.getString("username", null)
        val email = sharedPreferences.getString("email", null)
        val profileImage = sharedPreferences.getString("profileImage", null)

        return if (userId != null && username != null && email != null && profileImage != null) {
            User(userId, username, email, profileImage)
        } else {
            null
        }
    }
}