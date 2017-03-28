package com.example.extra.socialproject.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.extra.socialproject.R;

/**
 * Classe pour définir le clique sur un item de la RecyclerView
 */
public class RecyclerItemClickListener {
    private final RecyclerView mRecyclerView;
    private OnItemClickListener mOnItemClickListener;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(v);
                mOnItemClickListener.onItemClicked(mRecyclerView, holder.getAdapterPosition(), v);
            }
        }
    };

    private RecyclerView.OnChildAttachStateChangeListener mAttachListener = new RecyclerView.OnChildAttachStateChangeListener() {
        @Override
        public void onChildViewAttachedToWindow(View view) {
            if (mOnItemClickListener != null) {
                view.setOnClickListener(mOnClickListener);
            }
        }

        @Override
        public void onChildViewDetachedFromWindow(View view) {

        }
    };

    private RecyclerItemClickListener(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mRecyclerView.setTag(R.id.item_click_support, this);
        mRecyclerView.addOnChildAttachStateChangeListener(mAttachListener);
    }

    public static RecyclerItemClickListener addTo(RecyclerView view) {
        RecyclerItemClickListener support = (RecyclerItemClickListener) view.getTag(R.id.item_click_support);
        if (support == null) {
            support = new RecyclerItemClickListener(view);
        }
        return support;
    }

    public RecyclerItemClickListener setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
        return this;
    }

    public interface OnItemClickListener {

        void onItemClicked(RecyclerView recyclerView, int position, View v);
    }
}