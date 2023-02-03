package com.manuni.groceryapp;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterShop extends Filter {

    private AdapterShop adapterShop;
    private ArrayList<ModelShop> modelShopArrayList;

    public FilterShop(AdapterShop adapterShop, ArrayList<ModelShop> modelShopArrayList) {
        this.adapterShop = adapterShop;
        this.modelShopArrayList = modelShopArrayList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();

        if (constraint != null && constraint.length()>0){
            constraint = constraint.toString().toUpperCase();

            ArrayList<ModelShop> filterShop = new ArrayList<>();
            for (int i=0; i<modelShopArrayList.size();i++){
                    if (modelShopArrayList.get(i).getShopName().toUpperCase().contains(constraint)|| modelShopArrayList.get(i).getEmail().toUpperCase().contains(constraint) || modelShopArrayList.get(i).getPhoneNumber().toUpperCase().contains(constraint)){
                        filterShop.add(modelShopArrayList.get(i));
                    }
            }

            filterResults.count = filterShop.size();
            filterResults.values = filterShop;


        }
        else {
            filterResults.count = modelShopArrayList.size();
            filterResults.values = modelShopArrayList;
        }

        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
            adapterShop.list = (ArrayList<ModelShop>) filterResults.values;
            adapterShop.notifyDataSetChanged();
    }
}
