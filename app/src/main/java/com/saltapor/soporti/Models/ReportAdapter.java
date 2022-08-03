package com.saltapor.soporti.Models;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saltapor.soporti.AssignTicketActivity;
import com.saltapor.soporti.R;
import com.saltapor.soporti.ReportActivity;
import com.saltapor.soporti.ReportCategoryActivity;
import com.saltapor.soporti.ViewTicketActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    Context context;
    ArrayList<ReportItem> list;
    String dateFrom, dateTo;

    public ReportAdapter(Context context, ArrayList<ReportItem> list, String dateFrom, String dateTo) {
        this.context = context;
        this.list = list;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
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

        // Preparing elapsed time.
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

        holder.btnViewType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reportItem.quantity > 0) {
                    Intent intent = new Intent(context, ReportCategoryActivity.class);
                    intent.putExtra("KEY_NAME", reportItem.title);
                    intent.putExtra("DATE_FROM", dateFrom);
                    intent.putExtra("DATE_TO", dateTo);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "No hay tickets para este tipo", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ReportViewHolder extends RecyclerView.ViewHolder {

        TextView tvType, tvTicketsCount, tvTime;
        ImageButton btnViewType;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);

            tvType = itemView.findViewById(R.id.tvType);
            tvTicketsCount = itemView.findViewById(R.id.tvTicketsCount);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnViewType = itemView.findViewById(R.id.btnViewType);

        }

    }

}