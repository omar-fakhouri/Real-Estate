package com.app.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.app.realestateapp.R;

import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private final Context context;
    private final List<String> items;
    private final boolean isHomeStyle;

    public CustomSpinnerAdapter(Context context, List<String> items, boolean isHomeStyle) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
        this.isHomeStyle = isHomeStyle;
    }

    @Override
    public int getCount() {
        return items == null ? 0 : items.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return items == null ? null : items.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spinner_selected, parent, false);

        TextView tv = view.findViewById(R.id.tvSpinnerText);
        tv.setText(items.get(position));
        tv.setTextSize(15);
        tv.setTypeface(ResourcesCompat.getFont(context, R.font.urbanistblack));

        if (isHomeStyle) {
            tv.setTextColor(Color.parseColor("#CCFFFFFF"));
        } else {
            if (position == 0) {
                tv.setTextColor(Color.parseColor("#777777"));
            } else {
                tv.setTextColor(Color.BLACK);
            }
        }

        return view;
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_spinner_dropdown, parent, false);

        TextView tv = view.findViewById(R.id.tvSpinnerDropText);
        tv.setText(items.get(position));

        if (position == 0) {
            tv.setTextColor(Color.parseColor("#777777"));
        } else {
            tv.setTextColor(Color.BLACK);
        }

        return view;
    }
}