package com.uriallab.haat.haat.UI.Adapters;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.DataModels.CategoryModel;
import com.uriallab.haat.haat.Interfaces.CategoryClick;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.ConfigurationFile;
import com.uriallab.haat.haat.UI.Activities.makeOrder.MakeOrderFirstStepActivity;
import com.uriallab.haat.haat.Utilities.GlobalVariables;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.databinding.ItemCategoryBinding;

import java.util.List;

/**
 * Created by Mahmoud on 4/19/2020.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private Activity activity;
    private List<CategoryModel.ResultBean.CategoryBean> incomingList;

    private CategoryClick categoryClick;


    public CategoryAdapter(Activity activity, List<CategoryModel.ResultBean.CategoryBean> incomingList, CategoryClick categoryClick) {
        this.activity = activity;
        this.incomingList = incomingList;
        this.categoryClick = categoryClick;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.item_category, parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder, final int position) {

        if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
            holder.binding.catName.setText(incomingList.get(position).getCategory_Title_AR());
        else
            holder.binding.catName.setText(incomingList.get(position).getCategory_Title_EN());

        Picasso.get().load(incomingList.get(position).getCategory_Icon_Urls()).into(holder.binding.catImg);

        holder.itemView.setOnClickListener(v -> {
            String category;
            if (ConfigurationFile.getCurrentLanguage(activity).equals("ar"))
                category = incomingList.get(position).getCategory_Title_AR();
            else
                category = incomingList.get(position).getCategory_Title_EN();

            GlobalVariables.makeOrderModel.setCategory_Id(incomingList.get(position).getCategory_Id());
            GlobalVariables.makeOrderModel.setCategory_AuthorityId(incomingList.get(position).getCategory_AuthorityId());

            categoryClick.categoryClick(incomingList.get(position).getCategory_Type(), incomingList.get(position).getCategoryUID());

          /*  Bundle bundle = new Bundle();
            bundle.putString("storeName", incomingList.get(position).getCategory_Type());
            //bundle.putString("shopImg", img);
            bundle.putInt("categoryId",  incomingList.get(position).getCategoryUID());
            bundle.putBoolean("isService", false);
            IntentClass.goToActivity(activity, MakeOrderFirstStepActivity.class, bundle);
            String aa ;*/

            //categoryClick.categoryClick(incomingList.get(position).getCategory_Type(), category, String.valueOf(incomingList.get(position).getCategoryUID()));
        });
    }

    @Override
    public int getItemCount() {
        return incomingList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        private ItemCategoryBinding binding;

        private CategoryViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}