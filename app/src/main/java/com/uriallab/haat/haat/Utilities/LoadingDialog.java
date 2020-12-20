package com.uriallab.haat.haat.Utilities;


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.uriallab.haat.haat.R;

import androidx.fragment.app.DialogFragment;


public class LoadingDialog extends DialogFragment {

    View view;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.loading_dialog, container, false);

     /*   ImageView imageView = view.findViewById(R.id.loader_img);

        Glide.with(this).load(R.drawable.loader).into(imageView);*/

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(false);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);


        return view;
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }
}