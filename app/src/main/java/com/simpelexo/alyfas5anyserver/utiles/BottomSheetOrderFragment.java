package com.simpelexo.alyfas5anyserver.utiles;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.simpelexo.alyfas5anyserver.EventBus.LoadOrderEvent;
import com.simpelexo.alyfas5anyserver.R;

import org.greenrobot.eventbus.EventBus;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class BottomSheetOrderFragment extends BottomSheetDialogFragment {
    private Unbinder unbinder;

    private static BottomSheetOrderFragment instance;

    public static BottomSheetOrderFragment getInstance() {
        return instance == null ? new BottomSheetOrderFragment() : instance;
    }


    public BottomSheetOrderFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_order_filter, container, false);
        unbinder = ButterKnife.bind(this, itemView);

        return itemView;
    }

    @OnClick({R.id.placed_filter, R.id.shipping_filter, R.id.shipped_filter, R.id.canceled_filter})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.placed_filter:
                EventBus.getDefault().postSticky(new LoadOrderEvent(0));
                dismiss();
                break;
            case R.id.shipping_filter:
                EventBus.getDefault().postSticky(new LoadOrderEvent(1));
                dismiss();
                break;
            case R.id.shipped_filter:
                EventBus.getDefault().postSticky(new LoadOrderEvent(2));
                dismiss();
                break;
            case R.id.canceled_filter:
                EventBus.getDefault().postSticky(new LoadOrderEvent(-1));
                dismiss();
                break;
        }
    }
}
