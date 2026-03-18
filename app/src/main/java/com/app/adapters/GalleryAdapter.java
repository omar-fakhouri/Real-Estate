package com.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.realestateapp.R;
import com.bumptech.glide.Glide;

import java.util.List;
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {

    public interface OnImageClick {
        void onClick(String imageUrl);
    }

    private final Context context;
    private final List<String> imageList;
    private final OnImageClick listener;

    public GalleryAdapter(Context context, List<String> imageList, OnImageClick listener) {
        this.context = context;
        this.imageList = imageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_gallery_image, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {

        String imageUrl = imageList.get(position);

        Glide.with(context)
                .load(imageUrl)
                .into(holder.ivGalleryImage);

        holder.ivGalleryImage.setOnClickListener(v -> listener.onClick(imageUrl));
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {

        ImageView ivGalleryImage;

        public GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ivGalleryImage = itemView.findViewById(R.id.ivGalleryImage);
        }
    }
}