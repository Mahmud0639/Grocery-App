package com.manuni.groceryapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import com.manuni.groceryapp.databinding.CalculationRowBinding;

import java.util.ArrayList;

public class AdapterCalculation extends RecyclerView.Adapter<AdapterCalculation.AdapterCalculationViewHolder> implements Filterable {
    private Context context;
    public ArrayList<ModelCalculation> list,filterList;
    private FilterCalculation filter;


    public AdapterCalculation(Context context, ArrayList<ModelCalculation> list) {
        this.context = context;
        this.list = list;
        this.filterList = list;
    }

    @Override
    public AdapterCalculationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.calculation_row,parent,false);
        return new AdapterCalculationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AdapterCalculationViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ModelCalculation data = list.get(position);

        String shopName = data.getShopName();
        String fullAddress = data.getFullAddress();
        String inProgressCount = data.getInProgress();
        String inProgressTotal = data.getTotalOfInProgress();
        String cancelledCount = data.getCancelled();
        String cancelledTotal = data.getTotalOfCancelled();
        String completedCount = data.getCompleted();
        String completedTotal = data.getTotalOfCompleted();
        String deliveryFee = data.getShopDeliveryFee();
        String deliveryFeeFullDay = data.getDeliveryFeeFullDay();
        String shopUid = data.getShopUid();
        String timestamp = data.getTimestamp();
        String totalCost = data.getTotalOfCost();

        holder.binding.shopNameTV.setText(shopName);
        holder.binding.addressTV.setText(fullAddress);
        holder.binding.inProgressTV.setText(inProgressCount);
        holder.binding.cancelledTV.setText(cancelledCount);

        double cancelPrice = Double.parseDouble(cancelledTotal);

        holder.binding.totalCancelledTV.setText(String.format("%.2f",cancelPrice)+"Tk");
        holder.binding.completedTV.setText(completedCount);

        double completePrice = Double.parseDouble(completedTotal);

        holder.binding.totalCompletedTV.setText(String.format("%.2f",completePrice)+"Tk");

        double deliveryFeeDouble = Double.parseDouble(deliveryFee);

        holder.binding.deliveryFeeTV.setText(String.format("%.2f",deliveryFeeDouble)+"Tk");

        double fullDeliveryDouble = Double.parseDouble(deliveryFeeFullDay);
        holder.binding.fullDayDeliveryTV.setText(String.format("%.2f",fullDeliveryDouble)+"Tk");

        double totalCostDouble = Double.parseDouble(totalCost);

        holder.binding.totalCostTV.setText(String.format("%.2f",totalCostDouble)+"Tk");

        if (data.getPaymentStatus().equals("true")){
            holder.binding.paid.setVisibility(View.VISIBLE);
            holder.binding.unpaid.setVisibility(View.GONE);
        }else {
            holder.binding.paid.setVisibility(View.GONE);
            holder.binding.unpaid.setVisibility(View.VISIBLE);
        }







//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(Long.parseLong(timestamp));
//        String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString();

        holder.binding.dateTimeTV.setText(data.getTimestamp());

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        if (filter == null){
            filter = new FilterCalculation(this,filterList);
        }
        return filter;
    }

    public class AdapterCalculationViewHolder extends RecyclerView.ViewHolder{

        CalculationRowBinding binding;
        public AdapterCalculationViewHolder(View itemView) {
            super(itemView);

            binding = CalculationRowBinding.bind(itemView);
        }
    }
}
