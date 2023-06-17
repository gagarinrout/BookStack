package com.gagarinrout.bookstack

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.gagarinrout.bookstack.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //initialise firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog, will show while creating account | Register User
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle back button click to go to previous screen
        binding.backBtn.setOnClickListener {
           onBackPressedDispatcher.onBackPressed()
        }

        //handle click, begin register
        binding.registerBtn.setOnClickListener {
            /* 1) Input Data
               2) Validate Data
               3) Create Account - Firebase Auth
               4) Save User - Firebase Realtime Database
             */
            validateData()
        }

    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //1)Input Data
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.confirmPasswordEt.text.toString().trim()

        //2) Validate Data
        if(name.isEmpty()){
            //empty name...
            Toast.makeText(this, "Enter your name !", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            //invalid email pattern
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
        }
        else if(password.isEmpty()){
            Toast.makeText(this, "Enter Password !", Toast.LENGTH_SHORT).show()
        }
        else if(cPassword.isEmpty()){
            Toast.makeText(this, "Confirm Password !", Toast.LENGTH_SHORT).show()
        }
        else if(cPassword != password){
            Toast.makeText(this, "Password doesn't match !", Toast.LENGTH_SHORT).show()
        }
        else{
            createUserAccount()
        }
    }

    private fun createUserAccount() {
        //3) Create Account - Firebase Auth

        //show progress
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()

        //create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //account created, now user updated in database
                updateUserInfo()
            }
            .addOnFailureListener { e->
                //failed to create account
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to create account :(", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInfo() {
        //4) Update user in database
        progressDialog.setMessage("Saving your info...")

        //timestamp
        val timestamp = System.currentTimeMillis()

        //as user is registered in firebase so we can get userid
        val uid = firebaseAuth.uid

        //setup data to add in database
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["timestamp"] = timestamp
        hashMap["userType"] = "user" //can be user and admin, admin will be authorised manually from firebase db

        //adding data on db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                //user info saved, open user dashboard
                progressDialog.dismiss()
                Toast.makeText(this, "Account Created :)", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                //failed to add data in database
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving user info due to ${e.message} :(", Toast.LENGTH_SHORT).show()
            }
    }
}