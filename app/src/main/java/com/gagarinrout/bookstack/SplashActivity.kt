 package com.gagarinrout.bookstack

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.gagarinrout.bookstack.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

 class SplashActivity : AppCompatActivity() {

     private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()


        Handler(Looper.getMainLooper()).postDelayed({
            checkUser()
        }, 1000) //means 1 seconds delay

    }

     private fun checkUser() {
         //get current user, if logged in or not
         val firebaseuser = firebaseAuth.currentUser
         if(firebaseuser == null){
             //user not logged in, go to main screen
             startActivity(Intent(this, MainActivity::class.java))
             finish()
         }
         else{
             //user logged in, check user type
             val ref = FirebaseDatabase.getInstance().getReference("Users")
             ref.child(firebaseuser.uid)
                 .addListenerForSingleValueEvent(object : ValueEventListener{
                     override fun onDataChange(snapshot: DataSnapshot) {

                         //get user type e.g. user or admin
                         val userType = snapshot.child("userType").value
                         if (userType == "user"){
                             //open user dashboard
                             startActivity(Intent(this@SplashActivity, DashboardUserActivity::class.java))
                             finish()
                         }
                         else if (userType == "admin"){
                             //open admin dashboard
                             startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                             finish()
                         }
                     }
                     override fun onCancelled(error: DatabaseError) {

                     }

                 })
         }
     }
 }

 /*Keep user logged in
 1)Check if user logged in
 2)Check type of User
  */