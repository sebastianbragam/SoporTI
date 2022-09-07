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

import com.saltapor.soporti.EditCategoryActivity;
import com.saltapor.soporti.R;

import java.io.Serializable;
import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder> {

    Context context;
    ArrayList<Category> list;

    public CategoriesAdapter(Context context, ArrayList<Category> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CategoriesAdapter.CategoriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.categories_item, parent, false);
        return new CategoriesAdapter.CategoriesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.CategoriesViewHolder holder, int position) {

        Category category = list.get(position);
        holder.tvCategoryName.setText(category.category);
        holder.tvSubcategoryName.setText(category.subcategory);

        // Check if it is enabled or not.
        if (category.enabled) {
            holder.tvEnabled.setText("Sí");
        } else {
            holder.tvEnabled.setText("No");
        }

        holder.btnEditCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditCategoryActivity.class);
                intent.putExtra("KEY_NAME", category);
                intent.putExtra("KEY_LIST", (Serializable) list);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoriesViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategoryName, tvSubcategoryName, tvEnabled;
        ImageButton btnEditCategory;

        public CategoriesViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSubcategoryName = itemView.findViewById(R.id.tvSubcategoryName);
            tvEnabled = itemView.findViewById(R.id.tvEnabled);
            btnEditCategory = itemView.findViewById(R.id.btnEditCategory);

        }

    }

}
