package com.simpelexo.alyfas5anyserver.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.model.CartItem;
import com.simpelexo.alyfas5anyserver.model.OrderModel;
import com.simpelexo.alyfas5anyserver.utiles.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.ViewHolder> {


    Context context;
    List<CartItem> cartItemList;
    List<OrderModel> orderModel;

    Gson gson;
    private double totalPrice;
    private OrderAdapter orderAdapter;



    public OrderDetailAdapter(Context context, List<CartItem> cartItemList) {
        this.context = context;
        this.cartItemList = cartItemList;
        gson = new Gson();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_order_detail_item,parent,false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(cartItemList.get(position).getFoodImage()).into(holder.img_food_image);
        holder.txt_food_name.setText(new StringBuilder(" اسم المنتج :").append(cartItemList.get(position).getFoodName()));
       // holder.txt_food_price.setText(new StringBuilder(" سعر الكيلو :").append(cartItemList.get(position).getFoodPrice()));
       // holder.txt_food_price.setText(new StringBuilder(" سعر الكيلو :").append(totalPrice));
        Double food_price_n = cartItemList.get(position).getFoodPrice();
        Double food_quantity_n = cartItemList.get(position).getFoodQuantity();
        holder.txt_food_price.setText(new StringBuilder(" سعر الكيلو :").append(cartItemList.get(position).getFoodPrice()));

        Double tot = food_price_n*food_quantity_n;
        holder.txt_total_item_price.setText(new StringBuilder("اجمالي السعر :").append(tot.toString()));
        cartItemList.get(position).setTotalPriceItem(tot);
        if(position==cartItemList.size()-1) {//check if list last element
            //show your total count view here---- and add total amount
            grandTotal(cartItemList);
            //   Toast.makeText(context, ""+totalPrice, Toast.LENGTH_SHORT).show();
                        if (Common.selectedOrder.getTotalPayment() != totalPrice && totalPrice != 0) {
                            updateTotalPayment(totalPrice);

           }
        }


        if (cartItemList.get(position).getFoodSize().contains("Large")) {
            holder.txt_size.setText(new StringBuilder(" نوع الطلب :").append("بالعدد"));

        }else {
        holder.txt_size.setText(new StringBuilder(" نوع الطلب :").append(" بالوزن "));
    }
        holder.txt_food_quantity.setText(String.valueOf(cartItemList.get(position).getFoodQuantity()));
//        holder.img_btn_update_quantity.setOnClickListener(v -> {
//            Double edt_food_quantity_new = Double.parseDouble(holder.txt_food_quantity.getText().toString()) ;
//           updateQuantitynew(position,cartItemList,edt_food_quantity_new);
//           // cartItemList.remove(position);
//            grandTotal(cartItemList);
//
//            if (Common.selectedOrder.getTotalPayment() != totalPrice && totalPrice != 0) {
//                updateTotalPayment(totalPrice);
//                Toast.makeText(context, "Total payment updated to "+totalPrice, Toast.LENGTH_SHORT).show();
//            }
//           // navController.navigate(R.id.nav_order);
//
//        });
    }

    public double grandTotal(List<CartItem> items){

         totalPrice = 0;
        for(int i = 0 ; i < items.size(); i++) {
            totalPrice += items.get(i).getTotalPriceItem();
               //items.get(i).setTotalPriceItem(totalPrice);
        }

        return totalPrice;
    }
    public void updateQuantitynew(int position, List<CartItem> c, Double edt_food_quantity_new) {
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF).child(Common.selectedOrder.getKey()).child("cartItemList").child(cartItemList).child("foodQuantity");
        List<CartItem> tempList = new ArrayList<>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF).child(Common.selectedOrder.getKey()).child("cartItemList");

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mDatabase.setValue(c);
                for (DataSnapshot itemSnapShot:snapshot.getChildren())
                {
                    CartItem ci = itemSnapShot.getValue(CartItem.class);
                    c.add(ci);

//                Common.selectedOrder = itemSnapShot.getValue(OrderModel.class);
//                Common.selectedOrder.setKey(itemSnapShot.getKey());
//               tempList.add(Common.selectedOrder);

                }



             //   orderAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    public void updateQuantity(int position, Double edt_food_quantity_new) {
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF).child(Common.selectedOrder.getKey()).child("cartItemList").child(cartItemList).child("foodQuantity");
        if (!TextUtils.isEmpty(Common.selectedOrder.getKey())) {
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("foodQuantity",edt_food_quantity_new);
            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(Common.selectedOrder.getKey())
                    .child("cartItemList")
                    .child(String.valueOf(position))
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(aVoid -> {
                    //updateListData(cartItemList);
                OrderAdapter orderAdapter = new OrderAdapter(context,orderModel);
                orderAdapter.notifyDataSetChanged();
                notifyDataSetChanged();

            });
        }else {
            Toast.makeText(context, "Order Number Must not be Null or Empty!", Toast.LENGTH_SHORT).show(); }
    }
//    public void updateListData(List<CartItem> cartItems){
//        cartItemList.clear();
//        cartItemList.addAll(cartItems);
//
//        notifyDataSetChanged();
//    }
    public void updateTotalPayment( Double totalPrice) {
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference(Common.ORDER_REF).child(Common.selectedOrder.getKey()).child("cartItemList").child(cartItemList).child("foodQuantity");
        if (!TextUtils.isEmpty(Common.selectedOrder.getKey())) {
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("totalPayment",totalPrice);
            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(Common.selectedOrder.getKey())
                   // .child("cartItemList")
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(aVoid -> {
                Toast.makeText(context, ""+totalPrice, Toast.LENGTH_SHORT).show();
                Common.selectedOrder.setTotalPayment(totalPrice);

//                OrderAdapter orderAdapter = new OrderAdapter(context,orderModel);
//
//                orderAdapter.notifyDataSetChanged();
//                 notifyDataSetChanged();
            });
        }else {
            Toast.makeText(context, "Order Number Must not be Null or Empty!", Toast.LENGTH_SHORT).show(); }
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_food_name)
        TextView txt_food_name;
        @BindView(R.id.txt_food_quantity)
        EditText txt_food_quantity;
        @BindView(R.id.txt_food_price)
        TextView txt_food_price;
        @BindView(R.id.txt_size)
        TextView txt_size;
        @BindView(R.id.txt_total_item_price)
        TextView txt_total_item_price;
        @BindView(R.id.img_food_image)
        ImageView img_food_image;
        @BindView(R.id.img_btn_update_quantity)
        ImageButton img_btn_update_quantity;

        private Unbinder unbinder;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this,itemView);
            img_btn_update_quantity.setOnClickListener(v -> {
                Double edt_food_quantity_new = Double.parseDouble(txt_food_quantity.getText().toString()) ;
              //  updateQuantity(getAdapterPosition(),edt_food_quantity_new);
                cartItemList.get(getLayoutPosition()).setFoodQuantity(edt_food_quantity_new);
                //updating food price
                if (cartItemList.get(getLayoutPosition()).getFoodPrice()!=cartItemList.get(getLayoutPosition()).getFoodBasePrice()&&cartItemList.get(getLayoutPosition()).getFoodBasePrice()!= null) {

                    cartItemList.get(getLayoutPosition()).setFoodPrice(cartItemList.get(getLayoutPosition()).getFoodBasePrice());
                }
               // grandTotal(cartItemList);
                int itemClickedPosition = getLayoutPosition();
                updateQuantity(itemClickedPosition,edt_food_quantity_new);
                // grandTotal(cartItemList);
                if(itemClickedPosition==cartItemList.size()-1) {//check if list last element
                    grandTotal(cartItemList);
                    Toast.makeText(context, "" + totalPrice, Toast.LENGTH_SHORT).show();

                }
            });
        }
    }


}
