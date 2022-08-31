package com.saltapor.soporti.Models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saltapor.soporti.R;
import com.saltapor.soporti.ReportCategoryActivity;

import java.util.ArrayList;

public class ReportCategoryAdapter extends RecyclerView.Adapter<ReportCategoryAdapter.ReportCategoryViewHolder> {

    Context context;
    ArrayList<ReportItem> list;

    public ReportCategoryAdapter(Context context, ArrayList<ReportItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ReportCategoryAdapter.ReportCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.report_category_item, parent, false);
        return new ReportCategoryAdapter.ReportCategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportCategoryAdapter.ReportCategoryViewHolder holder, int position) {

        ReportItem reportItem = list.get(position);
        holder.tvCategory.setText(reportItem.title);
        holder.tvTicketsCount.setText(reportItem.quantity + "");

        // Preparing elapsed solving time.
        long diff = 0;
        if (reportItem.time > 0) {
            diff = reportItem.time / reportItem.quantity;
        } else {
            diff = reportItem.time ;
        }

        // Transform milliseconds (long) to seconds
        long seconds = diff / 1000;

        // Each one is the rest of the last one divided accordingly.
        long days = seconds / (24 * 60 * 60);
        long hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        long minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;

        String time = (days + " d, " + hours + " hs, " + minutes + " min.");
        holder.tvTime.setText(time);

        // Set mean rating.
        if (reportItem.quantity == 0) {
            holder.tvSatisfaction.setText("-");
        } else {
            holder.tvSatisfaction.setText((double) reportItem.rating / reportItem.quantity + "");
        }

        // Set mean response quantity.
        if (reportItem.quantity == 0) {
            holder.tvResponseQuantity.setText("-");
        } else {
            holder.tvResponseQuantity.setText((double) reportItem.responseQuantity / reportItem.quantity + "");
        }

        // Preparing elapsed response time.
        diff = 0;
        if (reportItem.responseTime > 0) {
            diff = reportItem.responseTime / reportItem.quantity;
        } else {
            diff = reportItem.responseTime ;
        }

        // Transform milliseconds (long) to seconds
        seconds = diff / 1000;

        // Each one is the rest of the last one divided accordingly.
        days = seconds / (24 * 60 * 60);
        hours = (seconds % (24 * 60 * 60)) / (60 * 60);
        minutes = ((seconds % (24 * 60 * 60)) % (60 * 60)) / 60;

        String responseTime = (days + " d, " + hours + " hs, " + minutes + " min.");
        holder.tvResponseTime.setText(responseTime);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ReportCategoryViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategory, tvTicketsCount, tvTime, tvResponseTime, tvResponseQuantity, tvSatisfaction;

        public ReportCategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTicketsCount = itemView.findViewById(R.id.tvTicketsCount);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvResponseTime = itemView.findViewById(R.id.tvResponseTime);
            tvResponseQuantity = itemView.findViewById(R.id.tvResponseQuantity);
            tvSatisfaction = itemView.findViewById(R.id.tvSatisfaction);

        }

    }

}