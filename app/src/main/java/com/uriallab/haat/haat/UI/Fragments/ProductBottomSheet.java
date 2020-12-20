package com.uriallab.haat.haat.UI.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.databinding.ObservableField;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.DataModels.ProductsModel;
import com.uriallab.haat.haat.DataModels.StoreProductsModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.Activities.Updates.PhotoViewActivity;
import com.uriallab.haat.haat.Utilities.IntentClass;

import java.util.List;

public class ProductBottomSheet extends BottomSheetDialogFragment {

    private Activity activity;

    private double tPrice = 0;

    private int productQuantity = 1;

    private ProductsModel.ResultEntity.ProductsEntity product;

    private FrameLayout deleteQuantity;
    private TextView quantityTxt;
    private TextView priceTxt;

    private List<StoreProductsModel.ProductBean> productMenuModelList;

    private ObservableField<String> totalPrice;

    public ProductBottomSheet() {
    }

    @SuppressLint("ValidFragment")
    public ProductBottomSheet(Activity activity, ProductsModel.ResultEntity.ProductsEntity product, FrameLayout deleteQuantity, TextView quantityTxt,
                              TextView priceTxt, List<StoreProductsModel.ProductBean> productMenuModelList,
                              ObservableField<String> totalPrice) {
        this.activity = activity;
        this.product = product;
        this.deleteQuantity = deleteQuantity;
        this.quantityTxt = quantityTxt;
        this.priceTxt = priceTxt;
        this.productMenuModelList = productMenuModelList;
        this.totalPrice = totalPrice;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(activity, R.layout.product_bottom_sheet, null);
        dialog.setContentView(contentView);

        dialog.setOnShowListener(dialog1 -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog1;

            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        Button addProduct = dialog.findViewById(R.id.add_product);
        FrameLayout plus = dialog.findViewById(R.id.plus_frame);
        FrameLayout minus = dialog.findViewById(R.id.minus_frame);
        final TextView productName = dialog.findViewById(R.id.product_name);
        final TextView quantity = dialog.findViewById(R.id.quantity);
        final TextView price = dialog.findViewById(R.id.price);
        RoundedImageView productImg = dialog.findViewById(R.id.product_img);

        Picasso.get().load(product.getProduct_icon()).into(productImg);

        productName.setText(product.getProduct_name());

        productImg.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("img", product.getProduct_icon());
            IntentClass.goToActivity(activity, PhotoViewActivity.class, bundle);
        });

        tPrice = Double.parseDouble(product.getProduct_price());

        price.setText(Double.parseDouble(product.getProduct_price()) + " " + activity.getString(R.string.currency));

        plus.setOnClickListener(v -> {
            String quantityTxt = quantity.getText().toString();

            productQuantity = (Integer.parseInt(quantityTxt) + 1);

            quantity.setText(productQuantity + "");

            tPrice = (Double.parseDouble(quantity.getText().toString()) * Double.parseDouble(product.getProduct_price()));

            price.setText(tPrice + " " + activity.getString(R.string.currency));
            priceTxt.setText(tPrice + "" );
        });

        minus.setOnClickListener(v -> {
            String quantityTxt = quantity.getText().toString();

            if (Integer.parseInt(quantityTxt) > 1) {
                productQuantity = (Integer.parseInt(quantityTxt) - 1);

                quantity.setText(productQuantity + "");
            }

            tPrice = (Double.parseDouble(quantity.getText().toString()) * Double.parseDouble(product.getProduct_price()));

            price.setText(tPrice + " " + activity.getString(R.string.currency));
            priceTxt.setText(tPrice + "" );
        });

        addProduct.setOnClickListener(v -> {
            try {
                Log.e("list_size", productMenuModelList.size() + " before add");
                productMenuModelList.add(new StoreProductsModel.ProductBean(product.getProduct_name(), Integer.parseInt(product.getProduct_price()), productQuantity));
                Log.e("list_size", productMenuModelList.size() + " after add");
            } catch (Exception e) {
                Log.e("list_size", "add Exception" + e.getMessage());
                e.printStackTrace();
            }

            product.setSelected(true);

            deleteQuantity.setVisibility(View.VISIBLE);
            quantityTxt.setVisibility(View.VISIBLE);
            quantityTxt.setText(productQuantity + "");


            double tPrice = 0;
            for (int i = 0; i < productMenuModelList.size(); i++) {
                double itemPrice = productMenuModelList.get(i).getPrice() * productMenuModelList.get(i).getQuantity();
                tPrice += itemPrice;
            }
            totalPrice.set(tPrice +" "+ activity.getString(R.string.currency));

            dismiss();
        });
    }
}