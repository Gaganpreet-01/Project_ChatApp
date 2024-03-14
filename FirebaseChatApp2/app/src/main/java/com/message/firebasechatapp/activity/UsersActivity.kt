package com.message.firebasechatapp.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.message.firebasechatapp.R
import com.message.firebasechatapp.adapter.UserAdapter
import com.message.firebasechatapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

class UsersActivity : AppCompatActivity() {
    private var userList = ArrayList<User>()
    private lateinit var userRecyclerView: RecyclerView
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        // Initialize views
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        userRecyclerView = findViewById(R.id.userRecyclerView)

        // Initialize FirebaseService
        val sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        token = sharedPref.getString("token", "") ?: ""

        // Set up RecyclerView
        userRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set up click listeners
        imgBack.setOnClickListener {
            onBackPressed()
        }

        imgProfile.setOnClickListener {
            val intent = Intent(
                this@UsersActivity,
                ProfileActivity::class.java
            )
            startActivity(intent)
        }

        // Fetch user list
        getUsersList()
    }

    private fun getUsersList() {
        val firebase: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        firebase?.let { user ->
            val userid = user.uid
            FirebaseMessaging.getInstance().subscribeToTopic("/topics/$userid")

            val databaseReference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("Users")

            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()

                    val currentUser = snapshot.getValue(User::class.java)
                    val imgProfile=findViewById<ImageView>(R.id.imgProfile)


                    // Load profile image
                    currentUser?.let {
                        if (it.profileImage.isNullOrEmpty()) {
                            Glide.with(this@UsersActivity).load(it.profileImage)
                                .placeholder(R.drawable.profile_image).into(imgProfile)
                        } else {
                            imgProfile.setImageResource(R.drawable.profile_image)
                        }
                    }

                    // Iterate through users and add to userList
                    for (dataSnapshot: DataSnapshot in snapshot.children) {
                        val user = dataSnapshot.getValue(User::class.java)
                        user?.let {
                            if (it.userId == firebase.uid) {
                                userList.add(it)
                            }
                        }
                    }

                    // Set up UserAdapter and attach to RecyclerView
                    val userAdapter = UserAdapter(this@UsersActivity, userList)
                    userRecyclerView.adapter = userAdapter
                }
            })
        }
    }
}
