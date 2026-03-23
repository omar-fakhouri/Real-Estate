package com.app.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.models.Property;
import com.app.realestateapp.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    private final Context context;
    private final List<Property> list;
    private final OnPropertyClickListener listener;
    private final OnFavoriteClickListener favoriteListener;

    public SearchAdapter(Context context, List<Property> list,
                         OnPropertyClickListener listener,
                         OnFavoriteClickListener favoriteListener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_search_home_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Property property = list.get(position);

        holder.tvTitle.setText(property.getTitle());
        holder.tvLocation.setText(property.getLocation());
        holder.tvPrice.setText((property.getPrice() == null ? "" : property.getPrice()) + " ₪");
        holder.tvRentSell.setText(property.getPurpose());
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

        holder.ibFavorite.setOnClickListener(v -> {
            if (favoriteListener != null && property.getId() != null) {
                favoriteListener.onFavoriteClick(property.getId());
            }

            Object tag = holder.ibFavorite.getTag();

            if (tag == null || tag.equals("normal")) {
                holder.ibFavorite.setImageResource(R.drawable.ic_fav_hover);
                holder.ibFavorite.setTag("selected");
            } else {
                holder.ibFavorite.setImageResource(R.drawable.ic_fav);
                holder.ibFavorite.setTag("normal");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvLocation, tvPrice, tvRentSell, tvView;
        ImageView ivHomeItem;
        ImageButton ibFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvRentSell = itemView.findViewById(R.id.tvRentSell);
            tvView = itemView.findViewById(R.id.tvView);
            ivHomeItem = itemView.findViewById(R.id.ivHomeItem);
            ibFavorite = itemView.findViewById(R.id.ibFav);
        }
    }

    public interface OnPropertyClickListener {
        void onPropertyClick(String propertyId);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(String propertyId);
    }
}