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
import com.saltapor.soporti.ViewTicketActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    Context context;
    ArrayList<ReportItem> list;

    public ReportAdapter(Context context, ArrayList<ReportItem> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ReportAdapter.ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.report_item, parent, false);
        return new ReportAdapter.ReportViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ReportViewHolder holder, int position) {

        ReportItem reportItem = list.get(position);
        holder.tvType.setText(reportItem.title);
        holder.tvTicketsCount.setText(reportItem.quantity + "");
        String time = new SimpleDateFormat("DD:HH:mm:ss").format(new Date(reportItem.time));
        holder.tvTime.setText(time);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvTicketsCount, tvTime;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvType);
            tvTicketsCount = itemView.findViewById(R.id.tvTicketsCount);
            tvTime = itemView.findViewById(R.id.tvTime);

        }

    }

}