package com.gagarinrout.bookstack

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.gagarinrout.bookstack.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityForgotPasswordBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        //init progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //handle click, back button click to go to previous screen
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle click, submit btn to begin password recovery process
        binding.submitBtn.setOnClickListener {
            validateData()
        }
    }

    private var email = ""
    private fun validateData() {
        //get data
        email = binding.emailEt.text.toString().trim()

        //validate data
        if(email.isEmpty()){
            Toast.makeText(this, "Enter email !", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "Enter valid email !", Toast.LENGTH_SHORT).show()
        }
        else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {
        //show progress dialog
        progressDialog.setMessage("Sending password reset instructions to $email")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Instructions sent to \n$email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}