package com.uriallab.haat.haat.UI.Adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableInt;
import androidx.recyclerview.widget.RecyclerView;

import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.CancelationReason;
import com.uriallab.haat.haat.databinding.ItemCancelReasonBinding;

import java.util.List;

public class CancelReasonAdapter extends RecyclerView.Adapter<CancelReasonAdapter.ReasonsViewHolder> {
    private int row_index=-1;


    private List<CancelationReason.ResultBean.ReasonsBean> incomingList;
    private ObservableInt reasonId;

    public CancelReasonAdapter(List<CancelationReason.ResultBean.ReasonsBean> incomingList, ObservableInt reasonId) {
        this.incomingList = incomingList;
        this.reasonId = reasonId;
    }

    @Override
    public ReasonsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCancelReasonBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_cancel_reason, parent, false);
        return new ReasonsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ReasonsViewHolder holder, final int position) {

        for (int i = 0; i < incomingList.size(); i++) {
            if (incomingList.get(position).isSelected()){
                holder.binding.checkbox.setChecked(true);
            }
            else
                holder.binding.checkbox.setChecked(false);
        }

        holder.binding.catTxt.setText(incomingList.get(position).getReason_Titl() + "");

        holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                row_index = position;
                notifyDataSetChanged();
                reasonId.set(incomingList.get(position).getOrderReasonUID());
                incomingList.get(position).setSelected(true);
            } else {
                reasonId.set(0);
                incomingList.get(position).setSelected(false);
            }

            //notifyDataSetChanged();
        });

        if (row_index == position) {
            holder.binding.checkbox.setChecked(true);
        } else {
            holder.binding.checkbox.setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        return incomingList.size();
    }

    public class ReasonsViewHolder extends RecyclerView.ViewHolder {

        private ItemCancelReasonBinding binding;

        private ReasonsViewHolder(ItemCancelReasonBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}