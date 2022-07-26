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

import com.saltapor.soporti.AssignTicketActivity;
import com.saltapor.soporti.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdminTicketsAdapter extends RecyclerView.Adapter<AdminTicketsAdapter.AdminTicketsViewHolder> {

    Context context;
    ArrayList<Ticket> list;

    public AdminTicketsAdapter(Context context, ArrayList<Ticket> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AdminTicketsAdapter.AdminTicketsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.admin_tickets_item, parent, false);
        return new AdminTicketsAdapter.AdminTicketsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminTicketsAdapter.AdminTicketsViewHolder holder, int position) {

        Ticket ticket = list.get(position);
        holder.tvTitle.setText(ticket.title);
        holder.categoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        holder.stateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(ticket.date));
        holder.tvDate.setText(date);
        holder.btnViewTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AssignTicketActivity.class);
                intent.putExtra("KEY_NAME", ticket);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class AdminTicketsViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, categoryName, stateName, tvDate;
        ImageButton btnViewTicket;

        public AdminTicketsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            categoryName = itemView.findViewById(R.id.tvCategoryName);
            stateName = itemView.findViewById(R.id.tvStateName);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnViewTicket = itemView.findViewById(R.id.btnViewTicket);

        }

    }

}