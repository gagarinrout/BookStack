package com.gagarinrout.bookstack

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.w3c.dom.Text
import java.util.Calendar
import java.util.Locale

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object{

        //created a static method to convert timestamp to proper date format, so we can use it everywhere in project, no need to rewrite again
        fun formatTimeStamp(timestamp: Long) : String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            //format dd/mm/yyyy
            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }


        fun loadCategory(categoryId: String, categoryTv: TextView){
            //load category using category id from firebase
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get category
                        val category = ""+snapshot.child("category").value
                        //set category
                        categoryTv.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
        }

        fun deleteBook(context: Context, bookId: String, bookTitle: String){
            //param details
            //1) context, used when required e.g. for progressDialog, toast
            //2) bookId to delete book from db
            //3) bookTitle, show in dialog etc

            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: deleting...")
            
            //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setMessage("Please wait...")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()

            Log.d(TAG, "deleteBook: deleting book from Db")
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Book deleted from db")
                    progressDialog.dismiss()
                    Toast.makeText(context, "Successfully Deleted :)", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "deleteBook: Failed to delete book from db")
                    progressDialog.dismiss()
                    Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun incrementViewsCount(bookId: String){
            //Get current book views count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get views count
                        var viewsCount = "${snapshot.child("viewsCount").value}"

                        if(viewsCount=="" || viewsCount==null){
                            viewsCount = "0";
                        }

                        //increment in views count
                        val newViewsCount = viewsCount.toLong() + 1

                        //setup data to update in db
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewsCount"] = newViewsCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }

        //remove from favorites function
        fun removeFromFavorite(context: Context, bookId: String){

            //firebase auth
            val firebaseAuth = FirebaseAuth.getInstance()

            //database ref
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Removed from favorites !", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    //unable to remove from favorites
                    Toast.makeText(context, "Unable to remove from favorites due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }



    }

}