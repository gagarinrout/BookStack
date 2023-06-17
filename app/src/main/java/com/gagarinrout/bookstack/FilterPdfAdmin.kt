package com.gagarinrout.bookstack

import android.widget.Filter

class FilterPdfAdmin : Filter {

    //array list in which we want to search
    var filterList: ArrayList<ModelPdf>

    //adapter in which filter need to be implemented
    var adapterPdfAdmin : AdapterPdfAdmin

    //constructor
    constructor(filterList: ArrayList<ModelPdf>, adapterPdfAdmin: AdapterPdfAdmin) {
        this.filterList = filterList
        this.adapterPdfAdmin = adapterPdfAdmin
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint:CharSequence? = constraint
        val result = FilterResults()
        //value to be searched should not be null and not empty
        if (!constraint.isNullOrEmpty()){
            //change to uppercase or lowercase to avoid case sensitivity
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<ModelPdf>()
            for ( i in filterList.indices){
                //validate if match
                if(filterList[i].title.lowercase().contains(constraint)){
                    //search value is similar to value in list, add to filtered list
                    filteredModels.add(filterList[i])
                }
            }
            result.count = filteredModels.size
            result.values = filteredModels
        }
        else{
            //value is either null or empty, return all data
            result.count = filterList.size
            result.values = filterList
        }
        return result
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        //apply filter changes
        adapterPdfAdmin.pdfArrayList = results.values as ArrayList<ModelPdf>

        //notify changes
        adapterPdfAdmin.notifyDataSetChanged()
    }


}