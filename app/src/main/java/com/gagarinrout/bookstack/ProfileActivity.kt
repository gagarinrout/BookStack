package com.gagarinrout.bookstack

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.gagarinrout.bookstack.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //gFirebase current user
    private lateinit var firebaseUser: FirebaseUser

    //arrayList to hold books
    private lateinit var booksArrayList: ArrayList<ModelPdf>
    private lateinit var adapterPdfFavorite: AdapterPdfFavorite

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //reset to default values
        binding.accountTypeTv.text = "N/A"
        binding.memberDateTv.text = "N/A"
        binding.favoriteBooksCountTv.text = "N/A"
        binding.accountstatusTv.text = "N/A"


        //firebase auth init
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        // init/setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        loadUserInfo()
        loadFavoriteBooks()

        //handle click, back button click to go to previous screen
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle click, verify user if not
        binding.accountstatusTv.setOnClickListener {
            if(firebaseUser.isEmailVerified){
                //user is verified
                Toast.makeText(this, "Already Verified :)", Toast.LENGTH_SHORT).show()
            }
            else{
                //user isn't verified, show confirmation dialog before verification
                emailVerificationDialog()
            }
        }


    }

    private fun emailVerificationDialog() {
        //show confirmation dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Verify Email")
            .setMessage("Are you sure you want to send email verification instructions to your email ${firebaseUser.email}")
            .setPositiveButton("SEND"){d,e->
                sendEmailVerification()
            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
            .show()
    }

    private fun sendEmailVerification() {
        //show progress dialog
        progressDialog.setMessage("Sending email verification instructions to email ${firebaseUser.email}")
        progressDialog.show()

        //send instructions
        firebaseUser.sendEmailVerification()
            .addOnSuccessListener {
                //successfully sent
                progressDialog.dismiss()
                Toast.makeText(this, "Account verification instructions sent! check your email ${firebaseUser.email}:)", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                //failed to send
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to send due to ${e.message} :(", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserInfo() {
        //check if user is verified or not, changes will effect after re login when you verify email
        if(firebaseUser.isEmailVerified){
            binding.accountstatusTv.text = "Verified"
        }
        else{
            binding.accountstatusTv.text = "Not Verified"
        }

        //db reference to load user info
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert timestamp to formatted Date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.nameTv.text = name
                    binding.emailTv.text = email
                    binding.accountTypeTv.text = userType
                    binding.memberDateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadFavoriteBooks(){

        //init arrayList
        booksArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear arrayList before starting to add data
                    booksArrayList.clear()
                    for(ds in snapshot.children){
                        //get only id of books, rest of the info we have loaded in adapter class
                        val  bookId = "${ds.child("bookId").value}"

                        //set to model
                        val modelPdf = ModelPdf()
                        modelPdf.id = bookId

                        //add model to list
                        booksArrayList.add(modelPdf)
                    }
                    //set number of favorite books
                    binding.favoriteBooksCountTv.text = "${booksArrayList.size}"

                    //setup adapter
                    adapterPdfFavorite = AdapterPdfFavorite(this@ProfileActivity, booksArrayList)
                    //set adapter to Recycler view
                    binding.favoriteRv.adapter = adapterPdfFavorite
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


}