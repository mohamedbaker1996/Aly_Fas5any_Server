package com.simpelexo.alyfas5anyserver.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.dantsu.escposprinter.connection.usb.UsbConnection;
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.simpelexo.alyfas5anyserver.EventBus.CategoryClick;
import com.simpelexo.alyfas5anyserver.EventBus.ChangeMenuClick;
import com.simpelexo.alyfas5anyserver.EventBus.ToastEvent;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.utiles.Common;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class HomeActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private int menuClick = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = this.getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
        String pine = sharedPreferences.getString("language", "ar");
        String languageToLoad = pine;
        Locale locale = new Locale(languageToLoad);//Set Selected Locale
        Locale.setDefault(locale);//set new locale as default
        //  Configuration config = new Configuration();//get Configuration
        Configuration config = new Configuration();//get Configuration
        config.locale = locale;//set config locale as selected locale
        this.getResources().updateConfiguration(config, this.getResources().getDisplayMetrics());
        invalidateOptionsMenu();
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        subscribeToTopic(Common.createTopicOrder());
      /*  FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
         drawer = findViewById(R.id.drawer_layout);
         navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_category, R.id.nav_food_list, R.id.nav_order,R.id.nav_sign_out)
                .setDrawerLayout(drawer)
                .build();
         navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString(getString(R.string.hey),Common.currentServerUser.getName(),txt_user);

        menuClick = R.id.nav_category;
    }

    private void subscribeToTopic(String topicOrder) {
        FirebaseMessaging.getInstance()
                .subscribeToTopic(topicOrder)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Failed  :"+task.isSuccessful(), Toast.LENGTH_SHORT).show();
            }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
         navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
//    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
//    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (HomeActivity.ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//                    UsbDevice usbDevice = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
//                        if (usbManager != null && usbDevice != null) {
//                            // YOUR PRINT CODE HERE
//
//                        }
//                    }
//                }
//            }
//        }
//    };
//    public void printUsb() {
//        UsbConnection usbConnection = UsbPrintersConnections.selectFirstConnected(this);
//        UsbManager usbManager = (UsbManager) this.getSystemService(Context.USB_SERVICE);
//        if (usbConnection != null && usbManager != null) {
//            PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(HomeActivity.ACTION_USB_PERMISSION), 0);
//            IntentFilter filter = new IntentFilter(HomeActivity.ACTION_USB_PERMISSION);
//            registerReceiver(this.usbReceiver, filter);
//            usbManager.requestPermission(usbConnection.getDevice(), permissionIntent);
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCategoryClick(CategoryClick event)
    {
        if (event.isSuccess()) {
            if (menuClick != R.id.nav_food_list) {
                navController.navigate(R.id.nav_food_list);
                menuClick = R.id.nav_food_list;

            }
        }
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onToastEvent(ToastEvent event)
    {
        if (event.isUpdate()) {
            Toast.makeText(this, R.string.update_success, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, R.string.delete_success, Toast.LENGTH_SHORT).show();
        }

        EventBus.getDefault().postSticky(new ChangeMenuClick(event.isFromFoodList()));
    }
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onChangeMenuClick(ChangeMenuClick event)
    {
        if (event.isFromFoodList()) {
            //clear stack
            navController.popBackStack(R.id.nav_category,true);
            navController.navigate(R.id.nav_category);
        }else {
        //clear stack
            navController.popBackStack(R.id.nav_food_list,true);
            navController.navigate(R.id.nav_food_list);
        }
        menuClick = -1;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.nav_language_arabic ) {
//            setLocale("ar");
            SharedPreferences ensharedPreferences = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
            SharedPreferences.Editor eneditor = ensharedPreferences.edit();
            eneditor.putString("language", "ar");
            eneditor.apply();


            Toast toast = Toast.makeText(HomeActivity.this, "تم تغيير اللغه الى العربيه", Toast.LENGTH_LONG);
            View view = toast.getView();

            //To change the Background of Toast
            view.setBackgroundColor(Color.parseColor("#3498db"));
            TextView text = (TextView) view.findViewById(android.R.id.message);

            //Shadow of the Of the Text Color
            text.setShadowLayer(1, 0, 0, Color.TRANSPARENT);
            text.setTextColor(Color.WHITE);
            text.setTextSize(Integer.valueOf(getResources().getString(R.string.text_size)));
            toast.show();
//             Toast.makeText(HomeCycleActivity.this, "Arabic Selected", Toast.LENGTH_SHORT).show();
            Common.currentLanguage ="ar";
            Intent refresh = new Intent(this, HomeActivity.class);
            refresh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(refresh);
            finish();

            // this.recreate();

        }else if (item.getItemId() == R.id.nav_language_english  ){
            //  setLocale("En");
            SharedPreferences npsharedPrefrences = getSharedPreferences("selectedLanguage", Context.MODE_PRIVATE);
            SharedPreferences.Editor npeditor = npsharedPrefrences.edit();
            npeditor.putString("language", "EN");
            npeditor.apply();
            Toast toast = Toast.makeText(HomeActivity.this, "English Selected", Toast.LENGTH_LONG);
            View view = toast.getView();

            //To change the Background of Toast
            view.setBackgroundColor(Color.parseColor("#3498db"));
            TextView text = (TextView) view.findViewById(android.R.id.message);

            //Shadow of the Of the Text Color
            text.setShadowLayer(1, 0, 0, Color.TRANSPARENT);
            text.setTextColor(Color.WHITE);
            text.setTextSize(Integer.valueOf(getResources().getString(R.string.text_size)));
            toast.show();
            Common.currentLanguage ="En";
//             Intent intent=getIntent();
//             overridePendingTransition(0, 0);
//             finish();
//             overridePendingTransition(0, 0);
//             startActivity(intent);
            Intent refresh = new Intent(this, HomeActivity.class);

            refresh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            startActivity(refresh);
            finish();

        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId()){
            case R.id.nav_category:
                if (item.getItemId() != menuClick)
                {
                    navController.popBackStack(); //Remove Back stack
                    navController.navigate(R.id.nav_category);
                }

                break;
            case R.id.nav_order:
                if (item.getItemId() != menuClick)
                {
                    navController.popBackStack(); //Remove Back stack
                    navController.navigate(R.id.nav_order);
                }
                break;
            case R.id.nav_sign_out:
              signOut();
                break;
            default:
                menuClick = -1;
                break;

        }
        menuClick = item.getItemId();
        return true;
    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_out)
                .setMessage(R.string.want_sign_out)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentServerUser = null;
                FirebaseAuth.getInstance().signOut();
                Intent mainActivity = new Intent(HomeActivity.this, MainActivity.class);
                mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivity);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }
}