package com.polar.polarsdkecghrdemo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<String> namesList;
    private OnItemClickListener listener;

    public MyAdapter(List<String> namesList, OnItemClickListener listener) {
        this.namesList = namesList;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = namesList.get(position);
        holder.textViewName.setText(name);
    }
    
    @Override
    public int getItemCount() {
        return namesList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(String name);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView textViewName;
        OnItemClickListener listener;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            this.listener = listener;
            textViewName = itemView.findViewById(R.id.textViewName);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                String s = textViewName.getText().toString()
                        .split("\n")[0].substring(4);
                listener.onItemClick(s);
            }
        }
    }
}