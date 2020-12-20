package com.uriallab.haat.haat.UI.Fragments;


import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;
import com.uriallab.haat.haat.API.APIModel;
import com.uriallab.haat.haat.LocalNotification.TrackingDelegate;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.SharedPreferences.LoginSession;
import com.uriallab.haat.haat.databinding.FragmentMoreBinding;
import com.uriallab.haat.haat.viewModels.MoreViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MoreFragment extends Fragment {

    private FragmentMoreBinding binding;

    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_more, container, false);

        arrowColors();
        startService();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.setMoreVM(new MoreViewModel(getActivity()));

        if (LoginSession.isLoggedIn(getActivity())) {
            Picasso.get().load(APIModel.BASE_URL + LoginSession.getUserData(getActivity()).getResult().getUserData().getUser_ImgUrl()).
                    placeholder(R.drawable.user_img).into(binding.profileImg);
        }else {
            binding.profileImg.setBackgroundResource(R.drawable.user_img);
        }
    }

    private void arrowColors() {
        binding.profileArrow.setImageResource(R.drawable.arrow_left);
        binding.editProfileArrow.setImageResource(R.drawable.arrow_left);
        binding.commentsArrow.setImageResource(R.drawable.arrow_left);
        binding.settingArrow.setImageResource(R.drawable.arrow_left);
        binding.cancelArrow.setImageResource(R.drawable.arrow_left);

        binding.profileArrow.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.editProfileArrow.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.commentsArrow.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.settingArrow.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);
        binding.cancelArrow.setColorFilter(getResources().getColor(R.color.colorTextHint), PorterDuff.Mode.SRC_ATOP);

    }

    public void startService() {
        // TODO: 6/10/2020
        Intent myService = new Intent(getContext(), TrackingDelegate.class);
        getActivity().startService(myService);
    }
}