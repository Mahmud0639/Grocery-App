package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.widget.Filter;

import java.util.ArrayList;

public class FilterProductUser extends Filter {
    private ProductUserAdapter adapter;
    private ArrayList<ModelProduct> productArrayList;

    public FilterProductUser(ProductUserAdapter adapter, ArrayList<ModelProduct> productArrayList) {
        this.adapter = adapter;
        this.productArrayList = productArrayList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();
        //validate data for search query
        if (constraint != null && constraint.length()>0){
            //change to upper case to make it case insensitive
            constraint = constraint.toString().toUpperCase();
            //now store our filter list
            ArrayList<ModelProduct> filterProduct = new ArrayList<>();
            for (int i = 0; i<productArrayList.size();i++){
                //check, search by title and category

                if (productArrayList.get(i).getProductTitle().toUpperCase().contains(constraint)
                        ||productArrayList.get(i).getProductCategory().toUpperCase().contains(constraint)
                        || productArrayList.get(i).getProductDesc().toUpperCase().contains(constraint)){
                    filterProduct.add(productArrayList.get(i));
                }
            }
            filterResults.count = filterProduct.size();
            filterResults.values = filterProduct;

        }else {
            filterResults.count = productArrayList.size();
            filterResults.values = productArrayList;
        }
        return filterResults;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        adapter.list = (ArrayList<ModelProduct>) filterResults.values;
        //refresh adapter
        adapter.notifyDataSetChanged();
    }
}
