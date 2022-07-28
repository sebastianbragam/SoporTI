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

import com.saltapor.soporti.FinishedTicketsActivity;
import com.saltapor.soporti.R;
import com.saltapor.soporti.ViewFinishedTicketActivity;
import com.saltapor.soporti.ViewTicketActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TicketsAdapter extends RecyclerView.Adapter<TicketsAdapter.TicketsViewHolder> {

    Context context;
    ArrayList<Ticket> list;
    String className;

    public TicketsAdapter(Context context, ArrayList<Ticket> list, String className) {
        this.context = context;
        this.list = list;
        this.className = className;
    }

    @NonNull
    @Override
    public TicketsAdapter.TicketsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.tickets_item, parent, false);
        return new TicketsAdapter.TicketsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketsAdapter.TicketsViewHolder holder, int position) {

        Ticket ticket = list.get(position);
        holder.tvTitle.setText("Nº" + ticket.number + ": " + ticket.title);
        holder.tvTypeName.setText(ticket.type);
        holder.tvCategoryName.setText(ticket.category.category + ": " + ticket.category.subcategory);
        holder.tvStateName.setText(ticket.state);
        String date = new SimpleDateFormat("dd/MM/yyyy").format(new Date(ticket.date));
        holder.tvDate.setText(date);
        holder.btnViewTicket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (className == FinishedTicketsActivity.class.getName()) {
                    Intent intent = new Intent(context, ViewFinishedTicketActivity.class);
                    intent.putExtra("KEY_NAME", ticket);
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ViewTicketActivity.class);
                    intent.putExtra("KEY_NAME", ticket);
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class TicketsViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvTypeName, tvCategoryName, tvStateName, tvDate;
        ImageButton btnViewTicket;

        public TicketsViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTypeName = itemView.findViewById(R.id.tvTypeName);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvStateName = itemView.findViewById(R.id.tvStateName);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnViewTicket = itemView.findViewById(R.id.btnViewTicket);

        }

    }

}

