package com.saltapor.soporti.Models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.saltapor.soporti.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    Context context;
    ArrayList<Category> list;

    public CategoryAdapter(Context context, ArrayList<Category> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        Category category = list.get(position);
        holder.categoryName.setText(category.category);
        holder.subcategoryName.setText(category.subcategory);

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName, subcategoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            categoryName = itemView.findViewById(R.id.tvCategoryName);
            subcategoryName = itemView.findViewById(R.id.tvSubcategoryName);

        }

    }

}
