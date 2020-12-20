package com.uriallab.haat.haat.UI.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.uriallab.haat.haat.DataModels.ProductMenuModel;
import com.uriallab.haat.haat.Interfaces.MenuClick;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.databinding.ItemProductMenuBinding;

import java.util.List;

public class ProductMenuAdapter extends RecyclerView.Adapter<ProductMenuAdapter.ProductMenuViewHolder> {

    private Activity activity;
    private List<ProductMenuModel.ResultBean.CategoryBean> incomingList;
    private MenuClick menuClick;

    private int MenuId;

    public ProductMenuAdapter(Activity activity, List<ProductMenuModel.ResultBean.CategoryBean> incomingList, int MenuId, MenuClick menuClick) {
        this.activity = activity;
        this.incomingList = incomingList;
        this.menuClick = menuClick;
        this.MenuId = MenuId;
    }

    @Override
    public ProductMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemProductMenuBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_product_menu, parent, false);
        return new ProductMenuViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final ProductMenuViewHolder holder, final int position) {

        holder.binding.menuName.setText(incomingList.get(position).getTitle());


        for (int i = 0; i < incomingList.size(); i++) {
            if (MenuId != incomingList.get(i).getId())
                incomingList.get(i).setSelected(false);
            else
                incomingList.get(i).setSelected(true);
        }

        if (incomingList.get(position).isSelected()) {
            holder.binding.menuLin.setBackgroundResource(R.drawable.shape_rounded_blue_corner_4);
        } else {
            holder.binding.menuLin.setBackgroundResource(R.drawable.shape_rounded_moov);
        }

        holder.itemView.setOnClickListener(v -> {
            try {
                MenuId = incomingList.get(position).getId();

                menuClick.menuClick(incomingList.get(position).getId());

                notifyDataSetChanged();
            }catch (Exception e){}

        });

    }

    @Override

    public int getItemCount() {
        return incomingList.size();
    }

    public class ProductMenuViewHolder extends RecyclerView.ViewHolder {

        private ItemProductMenuBinding binding;

        private ProductMenuViewHolder(ItemProductMenuBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}