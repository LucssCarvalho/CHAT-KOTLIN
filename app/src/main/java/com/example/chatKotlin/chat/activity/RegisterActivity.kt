package com.example.chatKotlin.chat.Activity

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.chatKotlin.R
import com.example.chatKotlin.chat.FirebaseConfig.FirebaseConfig.Companion.getDatabaseReference
import com.example.chatKotlin.chat.FirebaseConfig.FirebaseConfig.Companion.getFirebaseAuthentication
import com.example.chatKotlin.chat.Model.User
import com.example.chatKotlin.chat.helper.Base64Custom
import com.example.chatKotlin.chat.helper.Preferences
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference

class RegisterActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var button: Button
    private lateinit var inputName: TextInputEditText
    private lateinit var inputEmail: TextInputEditText
    private lateinit var inputPassword: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setView()
    }

    private fun setView() {
        inputName = findViewById(R.id.inputRegisterName)
        inputName.error = "You need to enter a name"

        inputEmail = findViewById(R.id.inputRegisterEmail)
        inputEmail.error = "You need to enter a email address"

        inputPassword = findViewById(R.id.inputRegisterPassword)
        inputPassword.error = "You need to enter a password"

        database = getDatabaseReference()
        auth = getFirebaseAuthentication()
        button = findViewById(R.id.btnSignUp)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun singUp(view: View) {
        val name = inputName.text.toString()
        val email = inputEmail.text.toString()
        val password = inputPassword.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val userId: String = Base64Custom().encodeBase64(email)
                        saveUser(name, email, userId)

                        Toast.makeText(
                            baseContext, "Your account has been successfully created!",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.d(TAG, "Your account has been successfully created")

                        finish()
                    } else {
                        var exception: String = try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthWeakPasswordException) {
                            "Password not strong enough! Must be at least 6 characters"
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            "Please use a valid email address"
                        } catch (e: FirebaseAuthUserCollisionException) {
                            "This e-mail address has already registered"
                        } catch (e: Exception) {
                            "Error registering user"
                        }
                        Log.w(TAG, "Error:", task.exception)
                        Toast.makeText(
                            baseContext, "$exception",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            Toast.makeText(
                baseContext, "Please complete all required fields!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveUser(useName: String, userEmail: String, userId64: String) {
        val userData = User().apply {
            name = useName
            email = userEmail
            userId = userId64
        }
        userData.save()
        Preferences(this).saveUserData(userId64, userData.name)
    }
}
