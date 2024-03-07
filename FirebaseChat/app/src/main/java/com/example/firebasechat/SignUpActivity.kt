package com.example.firebasechat

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.database.DatabaseReference

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)



        auth = FirebaseAuth.getInstance()

        btnSignUp.setOnClickListener{
            val userName = etName.text.toString()
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if(TextUtils.isEmpty(userName)){
                Toast.makeText(applicationContext,"userName is required",Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(email)){
                Toast.makeText(applicationContext,"email is required",Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(password)){
                Toast.makeText(applicationContext,"password is required",Toast.LENGTH_SHORT).show()
            }
            if(TextUtils.isEmpty(confirmPassword)){
                Toast.makeText(applicationContext,"confirmPassword is required",Toast.LENGTH_SHORT).show()
            }
            if(password !=confirmPassword){
                Toast.makeText(applicationContext,"Password did not match",Toast.LENGTH_SHORT).show()
            }
            registerUser(userName,email, password )

        }

    }
    private fun registerUser(userName:String,email:String,password:String){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(this){
                if(it.isSuccessful){
                    val user: FirebaseUser? = auth.currentUser
                    val userId: String = user!!.uid

                    databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId)

                    val hashmap:HashMap<String,String> = HashMap()
                    hashmap.put("userId",userId)
                    hashmap.put("userName",userName)
                    hashmap.put("profileImage","")

                    databaseReference.setValue(hashmap).addOnCompleteListener(this){
                        if(it.isSuccessful){
                            //open home activity
                            val intent = Intent(this@SignUpActivity,HomeActivity::class.java)
                            startActivity(intent)

                        }
                    }


                }
            }
    }
}