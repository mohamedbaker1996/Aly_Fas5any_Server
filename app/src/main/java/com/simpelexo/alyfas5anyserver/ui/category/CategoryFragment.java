package com.simpelexo.alyfas5anyserver.ui.category;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.simpelexo.alyfas5anyserver.EventBus.ToastEvent;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.adapter.CategoriesAdapter;
import com.simpelexo.alyfas5anyserver.model.Category;
import com.simpelexo.alyfas5anyserver.ui.BaseFragment;
import com.simpelexo.alyfas5anyserver.utiles.Common;
import com.simpelexo.alyfas5anyserver.utiles.HelperMethod;
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

public class CategoryFragment extends BaseFragment {
    private static final int PICK_IMAGE_REQUEST = 74;
    private Unbinder unbinder;

    private CategoryViewModel categoryViewModel;


    @BindView(R.id.recycler_menu_fragment)
    RecyclerView recycle_menu_fragment;
    android.app.AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    CategoriesAdapter adapter;
    List<Category> categoryModel;
    ImageView img_category;
    private Uri imageUri = null;

    FirebaseStorage storage;
    StorageReference storageReference;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      //  setUpActivity();
        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);
          setUpActivity();
        unbinder = ButterKnife.bind(this, view);

        initView();
        categoryViewModel.getMessageError().observe(this, s -> {
            Toast.makeText(getContext(), ""+s, Toast.LENGTH_SHORT).show();
            //HelperMethod.dismissProgressDialog();
            dialog.dismiss();
        });

        categoryViewModel.getCategoryListMutable().observe(this, categories -> {
         //   HelperMethod.dismissProgressDialog();
            dialog.dismiss();
            categoryModel = categories;
            adapter = new CategoriesAdapter(getContext(),categoryModel);

            recycle_menu_fragment.setAdapter(adapter);
            recycle_menu_fragment.setLayoutAnimation(layoutAnimationController);
        });
        return view;
    }

    private void initView() {
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        //dialog.show();
        //HelperMethod.showProgressDialog(getActivity(),"Loading",false);
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
//         recycle_menu_fragment.setLayoutManager(layoutManager);
//       recycle_menu_fragment.addItemDecoration(new SpacesItemDecoration(8));

        recycle_menu_fragment.setLayoutManager(layoutManager);
        recycle_menu_fragment.addItemDecoration(new DividerItemDecoration(getContext(),layoutManager.getOrientation()));

        MySwipeHelper mySwipeHelper =new MySwipeHelper(getContext(),recycle_menu_fragment,200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
            buf.add(new MyButton(getContext(),getString(R.string.update),30,0, Color.parseColor("#560027"),
                    pos -> {
                Common.categorySelected = categoryModel.get(pos);

                showUpdateDialog();
                    }));
            }
        };
    }

    private void showUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(Objects.requireNonNull(getContext()));
        builder.setTitle(R.string.update);
        builder.setMessage(R.string.fill_info);

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_update_category,null);
        EditText edt_category_name = (EditText) itemView.findViewById(R.id.edt_category_name);
        img_category = (ImageView)itemView.findViewById(R.id.img_category);

        //set data
        edt_category_name.setText(new StringBuilder("").append(Common.categorySelected.getName()));
        Glide.with(getContext()).load(Common.categorySelected.getImage()).into(img_category);

        //Event
        img_category.setOnClickListener(v -> {
        chooseImage();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, which) -> {
           dialogInterface.dismiss();
        });
        builder.setPositiveButton(getString(R.string.update), (dialogInterface, which) -> {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", edt_category_name.getText().toString());
            if (imageUri != null) {
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
                        updateData.put("image", uri.toString());
                        updateCategory(updateData);
                    });
                }).addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    dialog.setMessage(new StringBuilder("Uploading ").append(progress).append("%"));


                });
            } else {
                updateCategory(updateData);
            }
        }).setView(itemView);
        androidx.appcompat.app.AlertDialog dialog =builder.create();
        dialog.show();
    }

    private void updateCategory(Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenu_id())
                .updateChildren(updateData)
                .addOnFailureListener(e -> { Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show(); })
                .addOnCompleteListener(task -> {
                    categoryViewModel.loadCategories();
                    EventBus.getDefault().postSticky(new ToastEvent(true,false));
                });
    }
    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,getString(R.string.select_pic)),PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST &&resultCode == RESULT_OK ) {
            if (data != null &&data.getData() != null) {
                imageUri = data.getData();
                img_category.setImageURI(imageUri);
            }
        }
    }

    @Override
    public void onBack(){

        super.onBack();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
