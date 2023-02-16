package com.manuni.groceryapp;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.Set;

public class FilterOrderUser extends Filter {
    private AdapterOrderUser adapterOrderUser;
    private ArrayList<ModelOrderUser> modelOrderUsers;

    public FilterOrderUser(AdapterOrderUser adapterOrderUser, ArrayList<ModelOrderUser> modelOrderUsers) {
        this.adapterOrderUser = adapterOrderUser;
        this.modelOrderUsers = modelOrderUsers;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults filterResults = new FilterResults();

        if (constraint != null && constraint.length() > 0) {
            constraint = constraint.toString().toUpperCase();

            ArrayList<ModelOrderUser> orderUsers = new ArrayList<>();
            for (int i = 0; i < modelOrderUsers.size(); i++) {
                if (modelOrderUsers.get(i).getShopName().toUpperCase().contains(constraint)
                        || modelOrderUsers.get(i).getOrderId().toUpperCase().contains(constraint)
                        || modelOrderUsers.get(i).getOrderStatus().toUpperCase().contains(constraint)
                        || modelOrderUsers.get(i).getOrderCost().toUpperCase().contains(constraint)
                        || modelOrderUsers.get(i).getOrderTime().toUpperCase().contains(constraint)) {

                    orderUsers.add(modelOrderUsers.get(i));

                }
            }
            filterResults.count = orderUsers.size();
            filterResults.values = orderUsers;

        }else {
            filterResults.count = modelOrderUsers.size();
            filterResults.values = modelOrderUsers;
        }
        return filterResults;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults filterResults) {
        adapterOrderUser.list = (ArrayList<ModelOrderUser>) filterResults.values;
        adapterOrderUser.notifyDataSetChanged();

    }
}
