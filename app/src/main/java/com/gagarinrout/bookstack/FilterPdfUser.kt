package com.gagarinrout.bookstack

import android.view.Display.Mode
import android.widget.Filter

class FilterPdfUser :Filter {

    //arrayList in which we want to search
    var filterList: ArrayList<ModelPdf>

    //adapter in which filter need to be implemented
    var adapterPdfUser: AdapterPdfUser

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfUser: AdapterPdfUser) : super() {
        this.filterList = filterList
        this.adapterPdfUser = adapterPdfUser
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        var results = FilterResults()
        //value to be searched should not be null or empty
        if (!constraint.isNullOrEmpty()){
            //not null or empty

            //change to uppercase or lowercase to avoid case sensitivity
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<ModelPdf>()
            for(i in filterList.indices){
                if(filterList[i].title.uppercase().contains(constraint)){
                    //searched value matched with title, add to list
                    filteredModels.add(filterList[i])
                }
            }

            //return filtered list and size
            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            //either it is null or empty
            //return original list and size
            results.count = filterList.size
            results.values = filterList
        }
        return results

    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply filter changes
        adapterPdfUser.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfUser.notifyDataSetChanged()

    }
}