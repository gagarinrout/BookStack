package com.gagarinrout.bookstack

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.gagarinrout.bookstack.databinding.RowPdfUserBinding

class AdapterPdfUser : Adapter<AdapterPdfUser.HolderPdfUser>, Filterable{

    //context, get using constructor
    private var context: Context

    //arrayList to hold pdfs, get using constructor
    public var pdfArrayList: ArrayList<ModelPdf> //to access in filter class this is made public

    //arrayList to hold filtered pdfs
    public var filterList: ArrayList<ModelPdf>

    //viewBinding row_pdf_user.xml => RowPdfUserBinding
    private lateinit var binding: RowPdfUserBinding

    private var filter: FilterPdfUser? = null

    //filter class to enable search

    constructor(context: Context, pdfArrayList: ArrayList<ModelPdf>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfUser {
        // inflate/bind layout row_pdf_user.xml
        binding = RowPdfUserBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderPdfUser(binding.root)
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size //return list size/ number of records
    }


    override fun onBindViewHolder(holder: HolderPdfUser, position: Int) {
        /*get data, set data, handle click etc*/

        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val description = model.description
        val title = model.title
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //format timestamp to date
        val date = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.titleTv.text = title
        holder.descriptionTv.text = description
        holder.dateTv.text = date

        //load category from category id and set in categoryTv
        MyApplication.loadCategory(categoryId = categoryId, holder.categoryTv)


        //handle click, open pdf details
        holder.itemView.setOnClickListener {
            //pass bookId in intent, that will be used to get pdf info from db
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }

    }

    override fun getFilter(): Filter {
        if (filter == null){
            filter = FilterPdfUser(filterList, this)
        }
        return  filter as FilterPdfUser

    }


    /*ViewHolder class row_pdf_user.xml*/
    inner class HolderPdfUser(itemView: View): RecyclerView.ViewHolder(itemView){
        //init UI components of row_pdf_user.xml
        var titleTv = binding.titleTv
        var descriptionTv = binding.descriptionTv
        val categoryTv = binding.categoryTv
        var dateTv = binding.dateTv
    }




}