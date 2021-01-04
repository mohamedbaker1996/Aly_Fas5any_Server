package com.simpelexo.alyfas5anyserver.ui.order;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.simpelexo.alyfas5anyserver.EventBus.ChangeMenuClick;
import com.simpelexo.alyfas5anyserver.EventBus.LoadOrderEvent;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.Service.IFCMService;
import com.simpelexo.alyfas5anyserver.Service.RetrofitFCMClient;
import com.simpelexo.alyfas5anyserver.adapter.OrderAdapter;
import com.simpelexo.alyfas5anyserver.model.FCMSendData;
import com.simpelexo.alyfas5anyserver.model.OrderModel;
import com.simpelexo.alyfas5anyserver.model.TokenModel;
import com.simpelexo.alyfas5anyserver.ui.BaseFragment;
import com.simpelexo.alyfas5anyserver.utiles.BottomSheetOrderFragment;
import com.simpelexo.alyfas5anyserver.utiles.Common;
import com.simpelexo.alyfas5anyserver.utiles.MySwipeHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class OrderFragment extends BaseFragment {
    @BindView(R.id.recycler_order)
    RecyclerView recycler_order;
    @BindView(R.id.txt_order_filter)
    TextView txt_order_filter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IFCMService ifcmService;

    private orderViewModel orderViewModel;
    private Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    OrderAdapter adapter;
   // private SwipeRefreshLayout swipeRefreshLayout;
//    Handler mHandler = new Handler();//In UI Thread



    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @SuppressLint("ResourceAsColor")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        orderViewModel =
                ViewModelProviders.of(this).get(orderViewModel.class);
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
          setUpActivity();
        unbinder = ButterKnife.bind(this, view);
        initView();
        orderViewModel.getMessageError().observe(this,s -> {
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });
        orderViewModel.getOrderModelMutableLiveData().observe(this,orderModelList -> {
            if (orderModelList != null) {
                adapter = new OrderAdapter(getContext(),orderModelList);
                recycler_order.setAdapter(adapter);
                recycler_order.setLayoutAnimation(layoutAnimationController);
                adapter.notifyDataSetChanged();
                updateTextCounter();

            }
        });

//        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_layout);
//        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary, android.R.color.holo_blue_dark, android.R.color.holo_orange_dark, android.R.color.holo_green_dark );
//        swipeRefreshLayout.setOnRefreshListener(() -> {
//             orderViewModel.loadOrderByStatus(event.getStatus());
//             Reload current fragment
//            EventBus.getDefault().postSticky(new LoadOrderEvent(0));
//            orderViewModel.getOrderModelMutableLiveData().observe(this,orderModelList -> {
//                if (orderModelList != null) {
//                    adapter = new OrderAdapter(getContext(),orderModelList);
//                    recycler_order.setAdapter(adapter);
//                    recycler_order.setLayoutAnimation(layoutAnimationController);
//
//                    updateTextCounter();
//
//                }
//            });
//        });
//        swipeRefreshLayout.post(() -> {
//            orderViewModel.getOrderModelMutableLiveData().observe(this,orderModelList -> {
//                if (orderModelList != null) {
//                    adapter = new OrderAdapter(getContext(),orderModelList);
//                    recycler_order.setAdapter(adapter);
//                    recycler_order.setLayoutAnimation(layoutAnimationController);
//
//                    updateTextCounter();
//
//                }
//            });
//
      // });
//
//            mHandler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 3000);

        return view;
    }

    private void initView() {
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        setHasOptionsMenu(true);
    recycler_order.setHasFixedSize(true);
    recycler_order.setLayoutManager(new LinearLayoutManager(getContext()));

    layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        //get width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_order, width/4) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), getString(R.string.call), 30, 0, Color.parseColor("#9b0000"),
                        pos -> {
                            Dexter.withActivity(getActivity())
                                    .withPermission(Manifest.permission.CALL_PHONE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {
                                            OrderModel orderModel = adapter.getItemPosition(pos);
                                            Intent intent = new Intent();
                                            intent.setAction(Intent.ACTION_DIAL);
                                            intent.setData(Uri.parse(new StringBuilder("tel:  ")
                                            .append(orderModel.getUserPhone()).toString()));
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {
                                            Toast.makeText(getContext(), getString(R.string.you_must_accept)+response.getPermissionName(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                        }
                                    }).check();
                        }));
                buf.add(new MyButton(getContext(), getString(R.string.remove), 30, 0, Color.parseColor("#12005e"),
                        pos -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                            .setTitle(R.string.delete)
                            .setMessage(R.string.do_you_want_delete_order)
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {
                                dialogInterface.dismiss();
                            }).setPositiveButton(getString(R.string.delete), (dialogInterface, which) -> {
                               OrderModel orderModel = adapter.getItemPosition(pos);
                                FirebaseDatabase.getInstance()
                                        .getReference(Common.ORDER_REF)
                                        .child(orderModel.getKey())
                                        .removeValue()
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }).addOnSuccessListener(aVoid -> {
                                    adapter.removeItem(pos);
                                    adapter.notifyItemRemoved(pos);
                                   updateTextCounter();
                                    dialogInterface.dismiss();
                                    Toast.makeText(getContext(), R.string.order_deleted, Toast.LENGTH_SHORT).show();
                                        });
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                            Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                            negativeButton.setTextColor(Color.GRAY);
                            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                            negativeButton.setTextColor(Color.RED);
                        }));
                buf.add(new MyButton(getContext(), getString(R.string.edit), 30, 0, Color.parseColor("#336699"),
                        pos -> {
                    showEditDialog(adapter.getItemPosition(pos),pos);
                        }));
            }
        };
    }

    private void showEditDialog(OrderModel orderModel, int pos) {
        View layout_dialog;
        AlertDialog.Builder builder;
        if (orderModel.getOrderStatus() == 0) {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipping,null);
            builder = new AlertDialog.Builder(getContext(),android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
            .setView(layout_dialog);
        }else  if (orderModel.getOrderStatus() == -1){
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_cancel,null);
            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);

        }else {
            layout_dialog = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_dialog_shipped,null);
            builder = new AlertDialog.Builder(getContext()).setView(layout_dialog);
        }
        //view
        Button btn_ok = (Button)layout_dialog.findViewById(R.id.btn_ok);
        Button btn_cancel = (Button)layout_dialog.findViewById(R.id.btn_cancel);

        RadioButton rdi_shipping =(RadioButton)layout_dialog.findViewById(R.id.rdi_shipping);
        RadioButton rdi_shipped =(RadioButton)layout_dialog.findViewById(R.id.rdi_shipped);
        RadioButton rdi_cancel =(RadioButton)layout_dialog.findViewById(R.id.rdi_canceled);
        RadioButton rdi_delete =(RadioButton)layout_dialog.findViewById(R.id.rdi_delete);
        RadioButton rdi_restore_places =(RadioButton)layout_dialog.findViewById(R.id.rdi_restore_placed);

        TextView txt_status = (TextView)layout_dialog.findViewById(R.id.txt_status);
        //set data
        txt_status.setText(new StringBuilder(getString(R.string.order_status_cos))
        .append(Common.convertCodeToStatus(orderModel.getOrderStatus())));

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
        btn_cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
        btn_ok.setOnClickListener(v -> {
            if (rdi_cancel != null && rdi_cancel.isChecked()) {
                updateOrder(pos,orderModel,-1);
                dialog.dismiss();
            }else if (rdi_shipping != null && rdi_shipping.isChecked()) {
                updateOrder(pos,orderModel,1);
                dialog.dismiss();
            }else if (rdi_shipped != null && rdi_shipped.isChecked()) {
                updateOrder(pos,orderModel,2);
                dialog.dismiss();
            }else if (rdi_restore_places != null && rdi_restore_places.isChecked()) {
                updateOrder(pos,orderModel,0);
                dialog.dismiss();
            }else if (rdi_delete != null && rdi_delete.isChecked()) {
                deleteOrder(pos,orderModel);
                dialog.dismiss();
            }
        });
    }

    private void deleteOrder(int pos, OrderModel orderModel) {
        if (!TextUtils.isEmpty(orderModel.getKey())) {
            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .removeValue()
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(aVoid -> {
                adapter.removeItem(pos);
                adapter.notifyItemRemoved(pos);
                updateTextCounter();
                Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
            });
        }else {
            Toast.makeText(getContext(), "Order Number Must not be Null or Empty!", Toast.LENGTH_SHORT).show(); }
    }

    private void updateOrder(int pos , OrderModel orderModel,int status){
        if (!TextUtils.isEmpty(orderModel.getKey())) {
            Map<String,Object> updateData = new HashMap<>();
            updateData.put("orderStatus",status);
            FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.getKey())
                    .updateChildren(updateData)
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnSuccessListener(aVoid -> {
                        //dialog
                android.app.AlertDialog dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
                        dialog.show();

                        //get user token
                FirebaseDatabase.getInstance()
                        .getReference(Common.TOKEN_REF)
                        .child(orderModel.getUserId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {

                                    TokenModel tokenModel = snapshot.getValue(TokenModel.class);
                                    Map<String,String> notifiData = new HashMap<>();
                                    notifiData.put(Common.NOTI_TITLE,getString(R.string.your_order_updated));
                                    notifiData.put(Common.NOTI_CONTENT,getString(R.string.ur_order)+orderModel.getKey()+getString(R.string.was_updated));

                                    FCMSendData sendData = new FCMSendData(tokenModel.getToken(),notifiData);

                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                     .subscribe(fcmResponse -> {
                                         dialog.dismiss();
                                         if (fcmResponse.getSuccess() == 1) {
                                             Toast.makeText(getContext(), R.string.update_order_success, Toast.LENGTH_SHORT).show();

                                         }else {
                                             Toast.makeText(getContext(), R.string.update_order_success_notfication_fail, Toast.LENGTH_SHORT).show();

                                         }
                                     }, throwable -> {
                                    dialog.dismiss();
                                         Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                     })
                                    );


                                }else {
                                    dialog.dismiss();
                                    Toast.makeText(getContext(), "Token Not Found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                                Toast.makeText(getContext(), ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    adapter.removeItem(pos);
                    adapter.notifyItemRemoved(pos);
                    updateTextCounter();
                    });
        }else {
            Toast.makeText(getContext(), "Order Number Must not be Null or Empty!", Toast.LENGTH_SHORT).show(); }
    }

    private void updateTextCounter() {
        txt_order_filter.setText(new StringBuilder(getString(R.string.orders_num_cos))
                .append(adapter.getItemCount())
                .append(getString(R.string.orders_number_str)));

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.order_filter_menu,menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            BottomSheetOrderFragment bottomSheetOrderFragment = BottomSheetOrderFragment.getInstance();
            bottomSheetOrderFragment.show(getActivity().getSupportFragmentManager(),"OrderFilter");
            return true;
        }else
            return super.onOptionsItemSelected(item);
        }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }


    @Override
    public void onStop() {
        if (EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent.class))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent.class);
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onBack() {
        super.onBack();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
        unbinder.unbind();
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onLoadOrderEvent(LoadOrderEvent event){
        orderViewModel.loadOrderByStatus(event.getStatus());

    }
}