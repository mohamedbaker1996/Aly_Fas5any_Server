package com.simpelexo.alyfas5anyserver.EventBus;

public class ChangeMenuClick {
    private boolean isFromFoodList;

    public ChangeMenuClick(boolean isFromFoodList) {
        this.isFromFoodList = isFromFoodList;
    }

    public boolean isFromFoodList() {
        return isFromFoodList;
    }

    public void setFromFoodList(boolean fromFoodList) {
        isFromFoodList = fromFoodList;
    }
}
