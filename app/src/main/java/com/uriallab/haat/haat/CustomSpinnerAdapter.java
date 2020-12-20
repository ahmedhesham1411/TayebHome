package com.uriallab.haat.haat;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.databinding.ObservableField;

import java.util.ArrayList;
import java.util.List;

public class CustomSpinnerAdapter extends ArrayAdapter<SpinnerModel> {
    private Context mContext;
    private ArrayList<SpinnerModel> listState;
    private boolean isFromView = false;

    private String selectedCat = "";

    public ObservableField<String> selectedVal;

    public CustomSpinnerAdapter(Context context, int resource, List<SpinnerModel> objects, ObservableField<String> selectedVal) {
        super(context, resource, objects);
        this.mContext = context;
        this.listState = (ArrayList<SpinnerModel>) objects;
        this.selectedVal = selectedVal;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(final int position, View convertView,
                              ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflator = LayoutInflater.from(mContext);
            convertView = layoutInflator.inflate(R.layout.spinner_item, null);
            holder = new ViewHolder();
            holder.mCheckBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            holder.catTxt = (TextView) convertView.findViewById(R.id.cat_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.catTxt.setText(listState.get(position).getTitle());

        // To check weather checked event fire from getview() or user input
        isFromView = true;
        holder.mCheckBox.setChecked(listState.get(position).isSelected());
        isFromView = false;

        if ((position == 0)) {
            holder.mCheckBox.setVisibility(View.INVISIBLE);
        } else {
            holder.mCheckBox.setVisibility(View.VISIBLE);
        }
        holder.mCheckBox.setTag(position);
        holder.mCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if (selectedCat.length() > 0)
                    selectedCat += "," + listState.get(position).getId();
                else
                    selectedCat = String.valueOf(listState.get(position).getId());

                Log.e("selectedCat", selectedCat + " if");
            } else {
                if (selectedCat.length() > 1) {
                    String temp = "," + listState.get(position).getId();
                    selectedCat = selectedCat.replace(temp, "");

                    Log.e("selectedCat", selectedCat + " " + temp);
                } else
                    selectedCat = "";

                Log.e("selectedCat", selectedCat + " else");
            }

            selectedVal.set(selectedCat);

            if (!isFromView) {
                listState.get(position).setSelected(isChecked);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        private CheckBox mCheckBox;
        private TextView catTxt;
    }
}