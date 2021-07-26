package com.tapsdk.antiaddiction.demo.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tapsdk.antiaddiction.demo.R;

import java.util.ArrayList;
import java.util.List;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.SimpleHolder> {

    public List<String> data;

    public SimpleAdapter() {
        data = new ArrayList<>();
    }

    public SimpleAdapter(List<String> data) {
        this.data = data;
    }

    public void reset(List<String> data) {
        this.data = data;
    }

    @Override
    public SimpleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SimpleHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.itemview_simple_item_info, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleAdapter.SimpleHolder holder, int position) {
        holder.cellTextView.setText(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class SimpleHolder extends RecyclerView.ViewHolder {

        TextView cellTextView;

        public SimpleHolder(View itemView) {
            super(itemView);
            cellTextView = itemView.findViewById(R.id.cellTextView);
        }
    }
}
