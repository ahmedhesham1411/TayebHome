package com.uriallab.haat.haat.UI.Adapters;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.DataModels.OrdersModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.AllOffersActivity;
import com.uriallab.haat.haat.UI.Activities.ChatActivity;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.databinding.ItemOrdersBinding;

import java.util.List;

/**
 * Created by Mahmoud on 4/19/2020.
 */

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrdersViewHolder> {

    private Activity activity;
    private List<OrdersModel.ResultBean.OrdersBean> incomingList;

    public OrdersAdapter(Activity activity, List<OrdersModel.ResultBean.OrdersBean> incomingList) {
        this.activity = activity;
        this.incomingList = incomingList;
    }

    @Override
    public OrdersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemOrdersBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_orders, parent, false);
        return new OrdersViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final OrdersViewHolder holder, final int position) {

        if (incomingList.get(position).getOrd_Client_StatusID() == 2) {
            holder.binding.mainLin.setBackgroundResource(R.drawable.shape_rounded_moov);

            holder.binding.storeNameTxt.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.orderDetilsTxt.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.orderNumber.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.orderNum.setTextColor(activity.getResources().getColor(R.color.colorWhite));
        } else
            holder.binding.orderDetilsTxt.setTextColor(activity.getResources().getColor(R.color.colorTextHint));


        try {
            Picasso.get().load(incomingList.get(position).getOrd_Shop_ImgUrl())
                    .placeholder(R.drawable.store_blue).into(holder.binding.orderImg);
        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.binding.orderStateArrow.setImageResource(R.drawable.arrow_left);

        if (ConfigurationFile.getCurrentLanguage(activity).equals("en"))
            holder.binding.orderStateArrow.setRotation(180);

        holder.binding.orderNumber.setText(String.valueOf(incomingList.get(position).getOrderUID()));
        holder.binding.storeNameTxt.setText(incomingList.get(position).getOrd_Shop_Nm());
        holder.binding.orderDetilsTxt.setText(incomingList.get(position).getOrd_Dtls());

        if (incomingList.get(position).getOrd_Client_StatusID() == 1) {
            holder.binding.orderStateImg.setImageResource(R.drawable.new_order);

            holder.binding.orderStateImg.setColorFilter(activity.getResources().getColor(R.color.colorBlue), PorterDuff.Mode.SRC_ATOP);
            holder.binding.orderStateArrow.setColorFilter(activity.getResources().getColor(R.color.colorBlue), PorterDuff.Mode.SRC_ATOP);

            holder.binding.orderStateTxt.setTextColor(activity.getResources().getColor(R.color.colorBlue));
            holder.binding.orderStateTxt.setText(activity.getString(R.string.new_order));
        } else if (incomingList.get(position).getOrd_Client_StatusID() == 2) {
            holder.binding.orderStateImg.setImageResource(R.drawable.new_offer);

            holder.binding.orderStateImg.setColorFilter(activity.getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
            holder.binding.orderStateArrow.setColorFilter(activity.getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);

            holder.binding.orderStateTxt.setTextColor(activity.getResources().getColor(R.color.colorWhite));
            holder.binding.orderStateTxt.setText(activity.getString(R.string.new_offer));
        } else {
            holder.binding.orderStateImg.setImageResource(R.drawable.delivering);

            holder.binding.orderStateImg.setColorFilter(activity.getResources().getColor(R.color.colorMoov), PorterDuff.Mode.SRC_ATOP);
            holder.binding.orderStateArrow.setColorFilter(activity.getResources().getColor(R.color.colorMoov), PorterDuff.Mode.SRC_ATOP);

            holder.binding.orderStateTxt.setTextColor(activity.getResources().getColor(R.color.colorMoov));
            holder.binding.orderStateTxt.setText(activity.getString(R.string.delivering));
        }

        holder.itemView.setOnClickListener(view -> {
            Log.e("orderStatus", incomingList.get(position).getOrd_Client_StatusID() + "");
            Bundle bundle = new Bundle();
            if (incomingList.get(position).getOrd_Client_StatusID() == 1 || incomingList.get(position).getOrd_Client_StatusID() == 2) {
                if (incomingList.get(position).getOrd_Client_StatusID() == 1)
                    bundle.putBoolean("isNew", true);
                else
                    bundle.putBoolean("isNew", false);
                bundle.putString("orderId", String.valueOf(incomingList.get(position).getOrderUID()));
                IntentClass.goToActivity(activity, AllOffersActivity.class, bundle);
            } else {
                bundle.putString("orderId", String.valueOf(incomingList.get(position).getOrderUID()));
                IntentClass.goToActivity(activity, ChatActivity.class, bundle);
            }
        });

    }

    @Override
    public int getItemCount() {
        return incomingList.size();
    }

    public class OrdersViewHolder extends RecyclerView.ViewHolder {

        private ItemOrdersBinding binding;

        private OrdersViewHolder(ItemOrdersBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}