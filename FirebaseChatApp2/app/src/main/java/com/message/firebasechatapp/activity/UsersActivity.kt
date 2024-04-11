package com.message.firebasechatapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.message.firebasechatapp.R
import com.message.firebasechatapp.adapter.UserAdapter
import com.message.firebasechatapp.model.User

class UsersActivity : AppCompatActivity() {
    private var userList = ArrayList<User>()
    private lateinit var userRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        // Initialize views
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        userRecyclerView = findViewById(R.id.userRecyclerView)

        // Set up RecyclerView
        userRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up click listeners
        imgBack.setOnClickListener {
            onBackPressed()
        }

        imgProfile.setOnClickListener {
            val intent = Intent(this@UsersActivity, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Fetch user list
        getUsersList()
    }

    private fun getUsersList() {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        firebaseUser?.let { user ->
            val currentUserID = user.uid

            // Reference to the "Users" node in Firebase Realtime Database
            val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")

            // Listen for changes in the "Users" node
            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()

                    // Iterate through each user in the snapshot
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)

                        // Exclude the current user from the user list
                        if (user != null && user.userId != currentUserID) {
                            userList.add(user)
                        }
                    }

                    // Set up UserAdapter and attach to RecyclerView
                    val userAdapter = UserAdapter(this@UsersActivity, userList)
                    userRecyclerView.adapter = userAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
