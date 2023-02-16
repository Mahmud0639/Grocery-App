package com.manuni.groceryapp;

import android.widget.Filter;

import java.util.ArrayList;

public class FilterCalculation extends Filter{
    private AdapterCalculation adapter;
    private ArrayList<ModelCalculation> modelCalculations;


    public FilterCalculation(AdapterCalculation adapter, ArrayList<ModelCalculation> modelCalculations) {
        this.adapter = adapter;
        this.modelCalculations = modelCalculations;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraints) {
        FilterResults filterResults = new FilterResults();

        if (constraints != null && constraints.length()>0){
            constraints = constraints.toString().toUpperCase();

            ArrayList<ModelCalculation>  filterCalculation = new ArrayList<>();
            for (int i=0; i<modelCalculations.size(); i++){
                if (modelCalculations.get(i).getCompleted().toUpperCase().contains(constraints)|| modelCalculations.get(i).getTimestamp().toUpperCase().contains(constraints)){
                    filterCalculation.add(modelCalculations.get(i));
                }

            }
            filterResults.count = filterCalculation.size();
            filterResults.values = filterCalculation;
        }else {
            filterResults.count = modelCalculations.size();
            filterResults.values = modelCalculations;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        adapter.list = (ArrayList<ModelCalculation>) filterResults.values;
        adapter.notifyDataSetChanged();
    }
}
