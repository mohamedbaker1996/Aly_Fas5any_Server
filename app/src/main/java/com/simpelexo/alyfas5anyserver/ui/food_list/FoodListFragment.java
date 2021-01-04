package com.simpelexo.alyfas5anyserver.ui.food_list;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.simpelexo.alyfas5anyserver.EventBus.ChangeMenuClick;
import com.simpelexo.alyfas5anyserver.EventBus.ToastEvent;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.adapter.FoodListAdapter;
import com.simpelexo.alyfas5anyserver.model.FoodModel;
import com.simpelexo.alyfas5anyserver.ui.BaseFragment;
import com.simpelexo.alyfas5anyserver.utiles.Common;
import com.simpelexo.alyfas5anyserver.utiles.MySwipeHelper;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;

import static android.app.Activity.RESULT_OK;

public class FoodListFragment extends BaseFragment {

    private static final int PICK_IMAGE_REQUEST = 1234;
    private ImageView img_food;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private android.app.AlertDialog dialog;

    private FoodListViewModel foodListViewModel;
    private List<FoodModel> foodModelList;
    private Unbinder unbinder;
    @BindView(R.id.recycler_food_list)
    RecyclerView recycler_food_list;

    LayoutAnimationController layoutAnimationController;
    FoodListAdapter adapter;
    private Uri imageUri = null;

    public FoodListFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View view = inflater.inflate(R.layout.fragment_food_list, container, false);
        setUpActivity();
        unbinder = ButterKnife.bind(this, view);
        initViews();
        foodListViewModel.getMutableLiveDataFoodList().observe(this, foodModels -> {
            if (foodModels != null) {
            foodModelList = foodModels;
            adapter = new FoodListAdapter(getContext(), foodModelList);
            recycler_food_list.setAdapter(adapter);
            recycler_food_list.setLayoutAnimation(layoutAnimationController);
        }
        });
        return view;
    }

    private void initViews() {
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar())
                .setTitle(Common.categorySelected.getName());
        recycler_food_list.setHasFixedSize(true);
        recycler_food_list.setLayoutManager(new LinearLayoutManager(getContext()));

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
    try {
        //get width
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

    MySwipeHelper mySwipeHelper = new MySwipeHelper(getContext(), recycler_food_list, width/4) {
        @Override
        public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
            buf.add(new MyButton(getContext(), getString(R.string.delete), 30, 0, Color.parseColor("#9b0000"),
                    pos -> {
                        if (foodModelList != null)

                            Common.selectedFood = foodModelList.get(pos);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(getString(R.string.delete))
                                .setMessage(R.string.do_u_want_delete)
                                .setNegativeButton(R.string.cancel, ((dialogInterface, which) -> dialogInterface.dismiss()))
                                .setPositiveButton(R.string.delete, ((dialog, which) -> {
                                    Common.categorySelected.getFoods().remove(pos);
                                    updateFood(Common.categorySelected.getFoods(),true);
                                }));
                        AlertDialog deleteDialog = builder.create();
                        deleteDialog.show();
                    }));
            buf.add(new MyButton(getContext(), getString(R.string.update), 30, 0, Color.parseColor("#560027"),
                    pos -> {
                showUpdateDialog(pos);
                    }));
        }
                };
            }
    catch (Exception e){
     Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

          }

    private void showUpdateDialog(int pos) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.update);
        builder.setMessage(R.string.fill_info);
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_food,null);
        EditText edt_food_name = (EditText)itemView.findViewById(R.id.edt_food_name);
        EditText edt_food_price = (EditText)itemView.findViewById(R.id.edt_food_price);
        EditText edt_food_description = (EditText)itemView.findViewById(R.id.edt_food_description);
        img_food = (ImageView)itemView.findViewById(R.id.img_food);

        //set data
        edt_food_name.setText(new StringBuilder("").append(Common.categorySelected.getFoods().get(pos).getName()));
        edt_food_price.setText(new StringBuilder("").append(Common.categorySelected.getFoods().get(pos).getPrice()));
        edt_food_description.setText(new StringBuilder("").append(Common.categorySelected.getFoods().get(pos).getDescription()));
        Glide.with(getContext()).load(Common.categorySelected.getFoods().get(pos).getImage()).into(img_food);
        img_food.setOnClickListener(v -> {
         chooseImage();
        });
        builder.setNegativeButton(R.string.cancel,((dialog1, which) -> dialog1.dismiss() ))
                .setPositiveButton(R.string.update,((dialog1, which) -> {
                    FoodModel updateFood = Common.categorySelected.getFoods().get(pos);
                    updateFood.setName(edt_food_name.getText().toString());
                    updateFood.setDescription(edt_food_description.getText().toString());
                    updateFood.setPrice(TextUtils.isEmpty(edt_food_price.getText()) ? 0:
                            Long.parseLong(edt_food_price.getText().toString()));
                    if (imageUri !=null) {
                        //Has Image
                        dialog.setMessage("uploading...");
                        dialog.show();

                        String unique_name = UUID.randomUUID().toString();
                        StorageReference imageFolder = storageReference.child("images/" + unique_name);
                        imageFolder.putFile(imageUri).addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnCompleteListener(task -> {
                            dialog.dismiss();
                            imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                updateFood.setImage(uri.toString());
                                Common.categorySelected.getFoods().set(pos,updateFood);
                                updateFood(Common.categorySelected.getFoods(),false);
                            });
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            dialog.setMessage(new StringBuilder("Uploading ").append(progress).append("%"));


                        });
                    }else {
                        Common.categorySelected.getFoods().set(pos,updateFood);
                        updateFood(Common.categorySelected.getFoods(),false);
                    }

                }));
    builder.setView(itemView);
    AlertDialog updateDialog = builder.create();
    updateDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST &&resultCode == RESULT_OK ) {
            if (data != null &&data.getData() != null) {
                imageUri = data.getData();
                img_food.setImageURI(imageUri);
            }
        }
    }

    private void updateFood(List<FoodModel> foods,boolean isDelete) {
        Map<String,Object> updateData = new HashMap<>();
        updateData.put("foods",foods);
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        foodListViewModel.getMutableLiveDataFoodList();
                        EventBus.getDefault().postSticky(new ToastEvent(!isDelete,true));

                    }
                });
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,getString(R.string.select_pic)),PICK_IMAGE_REQUEST);
    }

    @Override
    public void onBack(){
        super.onBack();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new ChangeMenuClick(true));
        super.onDestroy();
        unbinder.unbind();
    }
}