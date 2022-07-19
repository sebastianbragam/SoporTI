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

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.RepliesViewHolder> {

    Context context;
    ArrayList<Reply> list;

    public RepliesAdapter(Context context, ArrayList<Reply> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public RepliesAdapter.RepliesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.replies_item, parent, false);
        return new RepliesAdapter.RepliesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RepliesAdapter.RepliesViewHolder holder, int position) {

        Reply reply = list.get(position);
        holder.tvUserName.setText(reply.user.firstName + " " + reply.user.lastName);
        holder.tvReply.setText(reply.reply);
        String date = new SimpleDateFormat("dd/MM/yy hh:mm aa ").format(new Date(reply.date));
        holder.tvDate.setText(date);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RepliesViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate, tvUserName, tvReply;

        public RepliesViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvReply = itemView.findViewById(R.id.tvReply);

        }

    }

}
