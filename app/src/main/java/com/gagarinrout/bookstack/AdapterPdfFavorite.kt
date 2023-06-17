package com.gagarinrout.bookstack

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.gagarinrout.bookstack.databinding.RowPdfFavoriteBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterPdfFavorite : Adapter<AdapterPdfFavorite.HolderPdfFavorite>{

    //Context
    private val context: Context
    //ArrayList to hold books
    private var booksArrayList: ArrayList<ModelPdf>

    //view binding
    private lateinit var binding: RowPdfFavoriteBinding


    //constructor
    constructor(context: Context, booksArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfFavorite {
        // bind/inflate row_pdf_favorite.xml
        binding = RowPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfFavorite(binding.root)
    }

    override fun getItemCount(): Int {
        return booksArrayList.size //return number of items in list
    }

    override fun onBindViewHolder(holder: HolderPdfFavorite, position: Int) {
        //Get Data, set data, handle clicks etc
        //get data; from (User > uid > Favorites) we will only have ids of favorite books so we have to load their details from "Books" node
        val model = booksArrayList[position]
        loadBookDetails(model, holder)

        //handle click, open pdf details page, pass book id to load details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id) //pass book id
            context.startActivity(intent)
        }

        //handle click, remove from favorite
        holder.removeFavBtn.setOnClickListener {
            MyApplication.removeFromFavorite(context, model.id)
            holder.itemView.visibility = View.GONE
        }

    }

    private fun loadBookDetails(model: ModelPdf, holder: AdapterPdfFavorite.HolderPdfFavorite) {
        val booksId = model.id
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(booksId)
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewsCount = "${snapshot.child("viewsCount").value}"

                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewsCount = viewsCount.toLong()
                    model.downloadsCount = downloadsCount.toLong()

                    //format timestamp to date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    //load category from categoryId and set on category Tv
                    MyApplication.loadCategory("$categoryId", holder.categoryTv)

                    //set data on Tv
                    holder.titleTv.text = title
                    holder.descriptionTv.text = description
                    holder.dateTv.text = date

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    /*View Holder class to manage UI views of row_pdf_favorite.xml*/
    inner class HolderPdfFavorite(itemView: View) : RecyclerView.ViewHolder(itemView){
        //init UI Views
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        var categoryTv = binding.categoryTv
        var dateTv = binding.dateTv
        var removeFavBtn = binding.removeFavoriteBtn
    }


}