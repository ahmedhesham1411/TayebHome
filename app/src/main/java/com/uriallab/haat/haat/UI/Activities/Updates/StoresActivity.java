package com.uriallab.haat.haat.UI.Activities.Updates;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.uriallab.haat.haat.DataModels.GoogleStoresModel;
import com.uriallab.haat.haat.R;
import com.uriallab.haat.haat.UI.Adapters.StoresAdapter;
import com.uriallab.haat.haat.Utilities.IntentClass;
import com.uriallab.haat.haat.Utilities.Utilities;
import com.uriallab.haat.haat.databinding.ActivityStoresBinding;
import com.uriallab.haat.haat.viewModels.StoresViewModel;

import java.util.ArrayList;
import java.util.List;

public class StoresActivity extends AppCompatActivity {

    public ActivityStoresBinding binding;

    private StoresViewModel viewModel;

    private List<GoogleStoresModel.ResultsBean> storesList = new ArrayList<>();
    private StoresAdapter storesAdapter;

    public boolean isLoading = false;

    private LinearLayoutManager layoutManager;

    public String pageToken = "", catType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_stores);

        Bundle bundle = getIntent().getBundleExtra("data");

        initRecycler();

        boolean isSearch = bundle.getBoolean("isSearch");
        String searchTxt = bundle.getString("searchTxt");
        String type = bundle.getString("type");
        String category = bundle.getString("category");

        viewModel = new StoresViewModel(this, type, category, isSearch, searchTxt);

        binding.setStoresVM(viewModel);

        binding.edtSearch.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                Utilities.hideKeyboard(StoresActivity.this);
                handled = true;
                viewModel.categoryName.set(binding.edtSearch.getText().toString());
               viewModel.getSearchResult(binding.edtSearch.getText().toString());
            }
            return handled;
        });
    }


    private void initRecycler() {
        storesAdapter = new StoresAdapter(this, storesList);
        layoutManager = new LinearLayoutManager(this);
        binding.storesRecycler.setLayoutManager(layoutManager);
        binding.storesRecycler.setAdapter(storesAdapter);

        binding.nestedScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (v.getChildAt(v.getChildCount() - 1) != null) {
                    if ((scrollY >= (v.getChildAt(v.getChildCount() - 1).getMeasuredHeight() - v.getMeasuredHeight())) &&
                            scrollY > oldScrollY) {
                        //code to fetch more data for endless scrolling
                        try {
                            viewModel.getStoresPaginate(catType, pageToken);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    public void updateRecycler(List<GoogleStoresModel.ResultsBean> storesModel) {
        storesList.clear();
        storesList.addAll(storesModel);
        storesAdapter.notifyDataSetChanged();
    }

    public void updateRecyclerPaginate(List<GoogleStoresModel.ResultsBean> storesModel) {
        storesList.addAll(storesModel);
        storesAdapter.notifyDataSetChanged();
    }
}