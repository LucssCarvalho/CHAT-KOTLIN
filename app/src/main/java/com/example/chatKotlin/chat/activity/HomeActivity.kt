package com.example.chatKotlin.chat.Activity

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.example.chatKotlin.R
import com.example.chatKotlin.chat.Adapter.TabAdapter
import com.example.chatKotlin.chat.FirebaseConfig.FirebaseConfig
import com.example.chatKotlin.chat.FirebaseConfig.FirebaseUtils.FIREBASE_TAG_CONTACTS
import com.example.chatKotlin.chat.FirebaseConfig.FirebaseUtils.FIREBASE_TAG_USERS
import com.example.chatKotlin.chat.Model.Contact
import com.example.chatKotlin.chat.Model.User
import com.example.chatKotlin.chat.activity.SettingsActivity
import com.example.chatKotlin.chat.helper.Base64Custom
import com.example.chatKotlin.chat.helper.Preferences
import com.example.chatKotlin.chat.helper.SlidingTabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity() {
    private lateinit var userAuth: FirebaseAuth
    private lateinit var slidingTabLayout: SlidingTabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var contactId: String
    private lateinit var firebaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        userAuth = FirebaseConfig.getFirebaseAuthentication()

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = "MEU CHAT"

        slidingTabLayout = findViewById(R.id.stl_tabs)
        viewPager = findViewById(R.id.vp_page)

        slidingTabLayout.setDistributeEvenly(true)
        slidingTabLayout.setSelectedIndicatorColors(
            ContextCompat.getColor(this, R.color.button_green)
        )

        viewPager.adapter = TabAdapter(supportFragmentManager)

        slidingTabLayout.setViewPager(viewPager)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.item_settings -> {
            openSettings()
            true
        }
        R.id.item_newContact -> {
            openNewContact()
            true
        }
        R.id.item_logout -> {
            signOut()
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun openSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun openNewContact() {
        val editTextNewUser = EditText(this)
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this).apply {
            setTitle("New Contact")
            setMessage("User e-mail")
            setCancelable(false)
            setView(editTextNewUser)
        }

        alertDialog.setPositiveButton("Add contact") { _, _ ->
            run {
                val emailContact = editTextNewUser.text.toString()
                val context = this

                if (emailContact.isEmpty()) {
                    Toast.makeText(this, "fill in the contact email", Toast.LENGTH_LONG)
                } else {
                    contactId = Base64Custom().encodeBase64(emailContact)
                    firebaseReference =
                        FirebaseConfig.getDatabaseReference()
                            .child(FIREBASE_TAG_USERS)
                            .child(contactId)

                    firebaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.value != null) {
                                val preferences = Preferences(context)
                                val userIdCurrentUser: String =
                                    preferences.getIdentification().toString()

                                val userContact: User = dataSnapshot.getValue(User::class.java) as User

                                firebaseReference = FirebaseConfig
                                    .getDatabaseReference()
                                    .child(FIREBASE_TAG_CONTACTS)
                                    .child(userIdCurrentUser)
                                    .child(contactId)

                                val contact = Contact().apply {
                                    name = userContact.name
                                    email = userContact.email
                                    contactId = userContact.userId
                                    status = userContact.status
                                }
                                firebaseReference.setValue(contact)
                            } else {
                                Toast.makeText(context, "user not found", Toast.LENGTH_LONG)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })
                }
            }
        }
        alertDialog.setNegativeButton("Cancel") { _, _ -> run {} }
        alertDialog.create()
        alertDialog.show()
    }

    private fun signOut() {
        userAuth.signOut()
        returnLoginActivity()
    }

    private fun returnLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}