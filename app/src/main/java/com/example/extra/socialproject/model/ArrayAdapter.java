package com.example.extra.socialproject.model;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.extra.socialproject.R;

import java.util.ArrayList;

/**
 * Adapter pour les RecyclerView qui vont contenir une image et du texte
 */
public class ArrayAdapter extends RecyclerView.Adapter<ArrayAdapter.ArrayAdapterViewHolder> {
    private ArrayList<String> recyclerList;

    public ArrayAdapter(ArrayList<String> recyclerList) {
        this.recyclerList = recyclerList;
    }

    @Override
    public ArrayAdapter.ArrayAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_recycler,parent,false);
        return new ArrayAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ArrayAdapter.ArrayAdapterViewHolder holder, int position) {
        holder.image.setImageResource(R.drawable.user_icon);
        holder.text.setText(recyclerList.get(position));
    }

    @Override
    public int getItemCount() {
        return recyclerList.size();
    }

    static class ArrayAdapterViewHolder extends RecyclerView.ViewHolder{
        private ImageView image;
        private TextView text;
        private ArrayAdapterViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image_id);
            text = (TextView) itemView.findViewById(R.id.text_id);
        }
    }
}