package com.simpelexo.alyfas5anyserver.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;

public class CartItem {
    @NonNull
    private String foodId;

    private String foodName;

    private String foodImage;

    private Double foodPrice;
    private Double foodBasePrice;

    private Double foodQuantity;

    private String userPhone;

    private String foodSize;

    private String uid;
    private Double totalPriceItem;

    @NonNull
    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(@NonNull String foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodImage() {
        return foodImage;
    }

    public void setFoodImage(String foodImage) {
        this.foodImage = foodImage;
    }

    public Double getFoodPrice() {
        return foodPrice;
    }

    public Double getFoodBasePrice() {
        return foodBasePrice;
    }

    public void setFoodBasePrice(Double foodBasePrice) {
        this.foodBasePrice = foodBasePrice;
    }

    public void setFoodPrice(Double foodPrice) {
        this.foodPrice = foodPrice;
    }

    public Double getFoodQuantity() {
        return foodQuantity;
    }

    public void setFoodQuantity(Double foodQuantity) {
        this.foodQuantity = foodQuantity;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodSize() {
        return foodSize;
    }

    public void setFoodSize(String foodSize) {
        this.foodSize = foodSize;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Double getTotalPriceItem() {
        return totalPriceItem;
    }

    public void setTotalPriceItem(Double totalPriceItem) {
        this.totalPriceItem = totalPriceItem;
    }
}
