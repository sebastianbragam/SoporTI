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

import com.saltapor.soporti.EditUserActivity;
import com.saltapor.soporti.NewTicketActivity;
import com.saltapor.soporti.R;
import com.saltapor.soporti.ViewFinishedTicketActivity;

import java.util.ArrayList;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    Context context;
    ArrayList<User> list;

    public UsersAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public UsersAdapter.UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.users_item, parent, false);
        return new UsersAdapter.UsersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.UsersViewHolder holder, int position) {

        User user = list.get(position);
        holder.tvUserName.setText(user.firstName + " " + user.lastName);
        holder.tvEmail.setText(user.email);
        holder.tvType.setText(user.type);

        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditUserActivity.class);
                intent.putExtra("KEY_NAME", user);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        TextView tvUserName, tvEmail, tvType;
        ImageButton btnEdit;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvType = itemView.findViewById(R.id.tvType);
            btnEdit = itemView.findViewById(R.id.btnEdit);

        }

    }

}