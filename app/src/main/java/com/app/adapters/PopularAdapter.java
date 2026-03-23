package com.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.models.Property;
import com.app.realestateapp.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class PopularAdapter extends RecyclerView.Adapter<PopularAdapter.ViewHolder> {

    private final Context context;
    private final List<Property> list;
    private final OnPropertyClickListener listener;
    private final OnFavClickListener favListener;

    public PopularAdapter(Context context, List<Property> list,
                          OnPropertyClickListener listener,
                          OnFavClickListener favListener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.favListener = favListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_popular_home_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = list.get(position);

        holder.tvTitle.setText(property.getTitle());
        holder.tvLocation.setText(property.getLocation());
        holder.tvPrice.setText(property.getPrice() + " ₪");

        if(property.getViews()>1000)
            holder.tvView.setText(String.valueOf(property.getViews()/1000)+"K");
        else
            holder.tvView.setText(String.valueOf(property.getViews()));

        Glide.with(context)
                .load(property.getMainImage())
                .placeholder(R.drawable.home_popular_placeholder)
                .error(R.drawable.home_popular_placeholder)
                .into(holder.ivHomeItem);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null && property.getId() != null) {
                listener.onPropertyClick(property.getId());
            }
        });

        holder.ibFav.setOnClickListener(v -> {
            if (favListener != null && property.getId() != null) {
                favListener.onFavClick(property.getId());
            }

            Object tag = holder.ibFav.getTag();

            if (tag == null || tag.equals("normal")) {
                holder.ibFav.setImageResource(R.drawable.ic_fav_hover);
                holder.ibFav.setTag("selected");
            } else {
                holder.ibFav.setImageResource(R.drawable.ic_fav);
                holder.ibFav.setTag("normal");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvLocation, tvPrice, tvView;
        ImageView ivHomeItem, ibFav;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvView = itemView.findViewById(R.id.tvView);
            ivHomeItem = itemView.findViewById(R.id.ivHomeItem);
            ibFav = itemView.findViewById(R.id.ibFav);
        }
    }

    public interface OnPropertyClickListener {
        void onPropertyClick(String propertyId);
    }

    public interface OnFavClickListener {
        void onFavClick(String propertyId);
    }
}