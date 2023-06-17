package com.gagarinrout.bookstack

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gagarinrout.bookstack.databinding.ActivityPdfDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PdfDetailActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding:ActivityPdfDetailBinding


    private companion object{
        //TAG
        const val TAG = "BOOK_DETAILS_TAG"
    }

    //book id, get from intent
    private var bookId = ""

    //book title and url, get from firebase
    private var bookTitle = ""
    private var bookUrl = ""

    //will hold a boolean value to indicate if book is in user's favorite or not
    private var isInMyFavorite = false

    //firebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent
        bookId = intent.getStringExtra("bookId")!!

        //init Progress bar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null){
            //user is logged in, check if book is in fav or not
            checkIsFavorite()
        }


        //increment book view count, whenever this page starts
        MyApplication.incrementViewsCount(bookId)

        loadBookDetails()

        //handle click, back button click to go to previous screen
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle click, open read pdf activity
        binding.readBookBtn.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }


        //handle click, download pdf
        binding.downloadBookBtn.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get book url
                        val pdfUrl = snapshot.child("url").value
                        val bookTitle = snapshot.child("title").value
                        var bookUrl = "$pdfUrl"
                        val fileId = extractFileId(bookUrl)
                        if (fileId != null) {
                            downloadPdfFile(fileId, bookTitle)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        //handle click, add/remove favorite
        binding.favoriteBtn.setOnClickListener {
            //this can be only added if user is logged in
            //1)check if user is logged in or not
            if (firebaseAuth.currentUser == null){
                //user not logged in, cant do favorite functionality
                Toast.makeText(this, "You are not logged in :(", Toast.LENGTH_SHORT).show()
            }
            else{
                //user is logged in, we can do favorite functionality
                if(isInMyFavorite){
                    //already in fav, remove from fav on click
                    removeFromFavorite()
                }
                else{
                    //not in fav, add to fav on click
                    addToFavorite()
                }
            }
        }

    }

    // Function to extract the file ID from a Google Drive URL
    private fun extractFileId(url: String): String? {
        val regex = "(?<=/d/|id=|rs/).+?(?=/|\\?|&|#|\$)".toRegex()
        val matchResult = regex.find(url)
        return matchResult?.value
    }

    // Function to download PDF file
    private fun downloadPdfFile(fileId: String, bookTitle: Any?) {
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val fileUri = Uri.parse("https://drive.google.com/uc?export=download&id=$fileId")
        val request = DownloadManager.Request(fileUri)

        // Set destination directory and file name
        val fileName = "$bookTitle.pdf"
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        // Set notification visibility
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        // Enqueue the download
        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(this, "Download Started...", Toast.LENGTH_SHORT).show()

        // Register BroadcastReceiver to listen for download completion
        val downloadCompleteReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    // Download completed
                    Toast.makeText(context, "Download Complete :)", Toast.LENGTH_SHORT).show()
                    incrementDownloadCount()
                }
            }
        }
        registerReceiver(downloadCompleteReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun incrementDownloadCount() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"

                    if(downloadsCount == "" || downloadsCount == null){
                        downloadsCount = "0"
                    }

                    //convert to long and increment
                    val newDownloadCount: Long = downloadsCount.toLong()+1

                    //setup data to update in db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //update incremented downloads count to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }


    private fun loadBookDetails() {
        // Books > bookId > Book Details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //format timestamp to date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    //load pdf category
                    MyApplication.loadCategory(categoryId, binding.categoryTv)

                    //set data
                    binding.titleTv.text = bookTitle
                    binding.descriptionTv.text = description
                    binding.viewsTv.text = viewsCount
                    binding.downloadsTv.text = downloadsCount
                    binding.dateTv.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun checkIsFavorite(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if(isInMyFavorite){
                        //available in favorites
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_filled_white, 0 , 0)
                        binding.favoriteBtn.text = "Remove Favorite"
                    }
                    else{
                        //not available in favorites
                        binding.favoriteBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_favorite_white, 0 , 0)
                        binding.favoriteBtn.text = "Add to Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun addToFavorite(){
        val timestamp = System.currentTimeMillis()

        //setup daa to add in db
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                //added to favorites
                Toast.makeText(this, "Added to favorites :)", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //failed to add to favorites
                Toast.makeText(this, "Failed to add to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
        checkIsFavorite()
    }
    private fun removeFromFavorite(){
        //database ref
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Removed from favorites !", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                //unable to remove from favorites
                Toast.makeText(this, "Unable to remove from favorites due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
        checkIsFavorite()
    }

}