package com.gagarinrout.bookstack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.gagarinrout.bookstack.databinding.ActivityPdfViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfViewActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfViewBinding

    //TAG
    private companion object{
        const val TAG = "PDF_VIEW_TAG"
    }

    private lateinit var webView: WebView

    //bookId
    var bookId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book id from intent , it will be used to load book from its url
        bookId = intent.getStringExtra("bookId")!!
        loadBookDetails()

        //handle click, back button click to go to previous screen
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun loadBookDetails() {
        Log.d(TAG, "loadBookDetails: Getting PDF Url from db")
        //Get book url from database using bookId
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book url
                    val pdfUrl = snapshot.child("url").value
                    Log.d(TAG, "onDataChange: PDF_URL: $pdfUrl")

                    //load pdf at webView using url obtained from db
                    loadPdfFromUrl(pdfUrl)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadPdfFromUrl(pdfUrl: Any?) {
        webView = binding.webView
        webView.webViewClient = object: WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }
        }
        webView.loadUrl("$pdfUrl")
        Toast.makeText(this, "Please wait, Loading Pdf...", Toast.LENGTH_SHORT).show()
        webView.settings.javaScriptEnabled = true
        webView.settings.builtInZoomControls = true
        binding.progressBar.visibility
    }
}