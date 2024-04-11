package com.message.firebasechatapp.activity

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.message.firebasechatapp.R
import com.message.firebasechatapp.RetrofitInstance
import com.message.firebasechatapp.adapter.ChatAdapter
import com.message.firebasechatapp.model.Chat
import com.message.firebasechatapp.model.NotificationData
import com.message.firebasechatapp.model.PushNotification
import com.message.firebasechatapp.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser? = null
    private var reference: DatabaseReference? = null
    private var chatList = ArrayList<Chat>()
    private lateinit var chatRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        // Initialize views
        val imgBack = findViewById<ImageView>(R.id.imgBack)
        val imgProfile = findViewById<ImageView>(R.id.imgProfile)
        val tvUserName = findViewById<TextView>(R.id.tvUserName) // Change type to TextView
        val etMessage = findViewById<EditText>(R.id.etMessage) // Change type to EditText
        val btnSendMessage = findViewById<ImageView>(R.id.btnSendMessage)

        chatRecyclerView = findViewById(R.id.chatRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        val intent = intent
        val userId = intent.getStringExtra("userId")
        val userName = intent.getStringExtra("userName")

        imgBack.setOnClickListener {
            onBackPressed()
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue<User>()
                user?.let {
                    tvUserName.text = it.userName
                    if (it.profileImage.isNotEmpty()) {
                        Glide.with(this@ChatActivity).load(it.profileImage).into(imgProfile)
                    } else {
                        imgProfile.setImageResource(R.drawable.profile_image)
                    }
                }
            }
        })

        btnSendMessage.setOnClickListener {
            val message: String = etMessage.text.toString()

            if (message.isEmpty()) {
                Toast.makeText(applicationContext, "Message is empty", Toast.LENGTH_SHORT).show()
                etMessage.setText("")
            } else {
                sendMessage(firebaseUser?.uid ?: "", userId, message)
                etMessage.setText("")
                val topic = "/topics/$userId"
                PushNotification(NotificationData(userName ?: "", message), topic).also {
                    sendNotification(it)
                }
            }
        }

        readMessage(firebaseUser?.uid ?: "", userId)
    }

    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chat")

        val hashMap: HashMap<String, String> = hashMapOf(
            "senderId" to senderId,
            "receiverId" to receiverId,
            "message" to message
        )

        reference.push().setValue(hashMap)
    }

    private fun readMessage(senderId: String, receiverId: String) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chat")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val chat = dataSnapShot.getValue<Chat>()

                    if ((chat?.senderId == senderId && chat.receiverId == receiverId) ||
                        (chat?.senderId == receiverId && chat.receiverId == senderId)
                    ) {
                        chatList.add(chat)
                    }
                }

                val chatAdapter = ChatAdapter(this@ChatActivity, chatList)
                chatRecyclerView.adapter = chatAdapter
            }
        })
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("TAG", "Response: ${response.body()}")
            } else {
                Log.e("TAG", response.errorBody()!!.string())
            }
        } catch(e: Exception) {
            Log.e("TAG", e.toString())
        }
    }
}
