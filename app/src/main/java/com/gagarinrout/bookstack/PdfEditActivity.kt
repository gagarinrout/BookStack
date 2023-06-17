package com.gagarinrout.bookstack

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.gagarinrout.bookstack.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfEditBinding

    private companion object{
        private const val TAG = "PDF_EDIT_TAG"
    }

    //book id from intent started from AdapterPdfAdmin
    private var bookId = ""

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arrayList to category titles
    private lateinit var categoryTitleArrayList:ArrayList<String>

    //arrayList to hold category Ids
    private lateinit var categoryIdArrayList: ArrayList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please Wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //handle back button click to go to previous screen
        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //handle click, show categories pick dialog
        binding.categoryTv.setOnClickListener {
            categoryDialog()
        }

        //handle click, begin update
        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private fun loadBookInfo() {
        Log.d(TAG, "loadBookInfo: Loading Book info...")
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    selectedCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    //set to views
                    binding.titleEt.setText(title)
                    binding.descriptionEt.setText(description)

                    //load book category info using categoryId
                    Log.d(TAG, "onDataChange: Loading book category info...")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectedCategoryId)
                        .addListenerForSingleValueEvent(object: ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value
                                //set to textview
                                binding.categoryTv.text = category.toString()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private var title = ""
    private var description = ""

    private fun validateData() {
        //get data
        title = binding.titleEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()

        //validate data
        if (title.isEmpty()){
            Toast.makeText(this, "Enter Title !", Toast.LENGTH_SHORT).show()
        }
        else if (description.isEmpty()){
            Toast.makeText(this, "Enter Description !", Toast.LENGTH_SHORT).show()
        }
        else if(selectedCategoryId.isEmpty()){
            Toast.makeText(this, "Pick Category !", Toast.LENGTH_SHORT).show()
        }
        else{
            updatePdf()
        }
    }

    private fun updatePdf() {
        Log.d(TAG, "updatePdf: Pdf info update started")

        //show progress
        progressDialog.setMessage("Updating book info")
        progressDialog.show()

        //setup data to update in db
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryId"

        //start updating
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updatePdf: Successfully updated info")
                progressDialog.dismiss()
                Toast.makeText(this, "Updated Successfully :)", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "updatePdf: Failed to update due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to update due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectedCategoryId = ""
    private var selectedCategoryTitle = ""


    private fun categoryDialog() {
        /*Show dialog to pick category of pdf/book*/

        //make string array from arrayList of string
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for(i in categoryTitleArrayList.indices){
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray){dialog, position->
                //handle click, set clicked category id and title
                selectedCategoryId = categoryIdArrayList[position]
                selectedCategoryTitle = categoryTitleArrayList[position]

                //set to textview
                binding.categoryTv.text = selectedCategoryTitle
            }
            .show() //show dialog
    }

    private fun loadCategories() {
        Log.d(TAG, "loadCategories: Loading categories...")

        categoryTitleArrayList = ArrayList()
        categoryIdArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before adding data into them
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children){
                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"

                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category $category")

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}