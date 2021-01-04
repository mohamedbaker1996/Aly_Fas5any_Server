package com.simpelexo.alyfas5anyserver.callback;

import com.simpelexo.alyfas5anyserver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {
    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);
}
