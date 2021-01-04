package com.simpelexo.alyfas5anyserver.utiles;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.model.OrderModel;
import com.simpelexo.alyfas5anyserver.ui.order.OrderFragment;


public class NotificationHelper extends ContextWrapper {

    private static final String Channel_Id = "com.simpelexo.alyfas5anyclient";
    private static final String Channel_Name = "Kayan Channel";
    private NotificationManager manager;
   // ListenOrder listenOrder;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        createChannels();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannels() {
        NotificationChannel elAmerChannel = new NotificationChannel(Channel_Id, Channel_Name, NotificationManager.IMPORTANCE_DEFAULT);
        elAmerChannel.enableLights(true);
        elAmerChannel.enableVibration(true);
        elAmerChannel.setLightColor(Color.RED);
        elAmerChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(elAmerChannel);
    }

    public NotificationManager getManager() {
        if (manager == null)
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getElAmerChannelNotification(String key, OrderModel request) {
        Intent intent = new Intent(getBaseContext(), OrderFragment.class);
        intent.putExtra("phone", request.getUserPhone());
        PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return new Notification.Builder(getApplicationContext(), Channel_Id)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setTicker("Aly fes5any")
                .setContentText("Your Order " + key + "was updated to " + Common.convertCodeToStatus(request.getOrderStatus()))
                .setContentTitle("Aly El Fas5any")
                .setContentIntent(contentIntent)
                .setSmallIcon(R.mipmap.ic_launcher);
//        .setContentText("Your Order " + key + " is " + Common.convertCodeToStatus(request.getStatus()))
    }


}
