package com.simpelexo.alyfas5anyserver.callback;


import com.simpelexo.alyfas5anyserver.model.Category;

import java.util.List;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<Category> categoriesList);
    void onCategoryLoadFailed(String message);
}
