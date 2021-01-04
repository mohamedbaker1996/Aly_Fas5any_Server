package com.simpelexo.alyfas5anyserver.Service;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.simpelexo.alyfas5anyserver.utiles.Common;

import java.util.Map;
import java.util.Random;

public class MyFCMServices extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Map<String,String> dataReceived = remoteMessage.getData();
        if (dataReceived != null) {
            Common.showNotification(this,new Random().nextInt(),
                    dataReceived.get(Common.NOTI_TITLE),
                    dataReceived.get(Common.NOTI_CONTENT),null);
        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s);
    }
}
