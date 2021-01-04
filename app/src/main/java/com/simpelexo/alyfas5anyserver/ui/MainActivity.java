package com.simpelexo.alyfas5anyserver.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.model.ServerUserModel;
import com.simpelexo.alyfas5anyserver.utiles.Common;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {
 private static int APP_REQUEST_CODE = 7171;
 private FirebaseAuth firebaseAuth;
 private FirebaseAuth.AuthStateListener listener;
 private AlertDialog dialog;
 private DatabaseReference serverRef;
 private List<AuthUI.IdpConfig> providers;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        //setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
    providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
    serverRef = FirebaseDatabase.getInstance().getReference(Common.SERVER_REF);
    firebaseAuth = FirebaseAuth.getInstance();
    dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();
    listener = firebaseAuthLocal ->{
        FirebaseUser user = firebaseAuthLocal.getCurrentUser();
        if (user != null) {
        checkServerUserFromFirebase(user);
        }else {
            phoneLogin();
        }
    };

    }

    private void checkServerUserFromFirebase(FirebaseUser user) {
        dialog.show();
        serverRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            ServerUserModel userModel = snapshot.getValue(ServerUserModel.class);
                            assert userModel != null;
                            if (userModel.isActive()) {
                                goToHomeActivity(userModel);
                            }else {
                                Toast.makeText(MainActivity.this, R.string.must_be_allowed, Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            ShowRegisterDialog(user);

                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void ShowRegisterDialog(FirebaseUser user) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.register);
        builder.setMessage(R.string.fill_all_info);

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);
        EditText edt_name = (EditText)itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText)itemView.findViewById(R.id.edt_phone);

        //set data
        edt_phone.setText(user.getPhoneNumber());
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> {
        dialog.dismiss();
        });
        builder.setPositiveButton(R.string.register, (dialogInterface, which) -> {
            if (TextUtils.isEmpty(edt_name.getText().toString())) {
                Toast.makeText(MainActivity.this, R.string.your_name, Toast.LENGTH_SHORT).show();
                return;
            }
            ServerUserModel serverUserModel = new ServerUserModel();
            serverUserModel.setUid(user.getUid());
            serverUserModel.setName(edt_name.getText().toString());
            serverUserModel.setPhone(edt_phone.getText().toString());
            serverUserModel.setActive(false); // Default Value
        dialog.show();
            serverRef.child(serverUserModel.getUid())
                    .setValue(serverUserModel)
                    .addOnFailureListener(e -> {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }).addOnCompleteListener(task -> {
                dialog.dismiss();
                Toast.makeText(MainActivity.this, R.string.congratulations_regiser, Toast.LENGTH_SHORT).show();
                //  goToHomeActivity(serverUserModel);
            });

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog  registerDialog = builder.create();
        registerDialog.show();
    }

    private void goToHomeActivity(ServerUserModel serverUserModel) {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    Common.currentServerUser = serverUserModel;
                    startActivity(new Intent(this,HomeActivity.class));
                    finish();
                }).addOnCompleteListener(task -> {
            dialog.dismiss();
            Common.currentServerUser = serverUserModel;
            Common.updateToken(MainActivity.this,task.getResult().getToken());
           // Common.currentToken =task.getResult().getToken();
            startActivity(new Intent(this,HomeActivity.class));
            finish();
                });

    }

    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers).build(),APP_REQUEST_CODE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }else {
                Toast.makeText(this, R.string.fail_sign_in, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStop() {
        if (listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();
    }
}