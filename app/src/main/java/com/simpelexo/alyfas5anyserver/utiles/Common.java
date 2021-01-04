package com.simpelexo.alyfas5anyserver.utiles;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.simpelexo.alyfas5anyserver.R;
import com.simpelexo.alyfas5anyserver.model.Category;
import com.simpelexo.alyfas5anyserver.model.FoodModel;
import com.simpelexo.alyfas5anyserver.model.OrderModel;
import com.simpelexo.alyfas5anyserver.model.ServerUserModel;
import com.simpelexo.alyfas5anyserver.model.TokenModel;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

public class Common {
//    public static final String POPULAR_CATEGORY_REF = "MostPopular";
//    public static final String BEST_DEAL_REF = "BestDeals";
    public static final String CATEGORY_REF = "categories";
//    public static final String FOODS_REF = "foods";
    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
//    public static final String COMMENT_REF = "Comments";
//    public static final String USER_REF = "Users";
    public static final String ORDER_REF = "Orders";
   // public static UserModel currentUser;
//    public static final String DELETE = "Delete";
//    public static final String USER_KEY = "User";
//    public static final String PWD_KEY = "Password";
    public static final int PICK_IMAGE_REQUEST = 71;
    public static final String SERVER_REF = "Server";
    public static final String NOTI_TITLE ="title" ;
    public static final String NOTI_CONTENT = "content";

    public static final String TOKEN_REF = "Tokens";
    public static ServerUserModel currentServerUser;
    public static Category categorySelected;
    public static FoodModel selectedFood;
    public static OrderModel selectedOrder;
    public static String currentToken="";
    public static String currentLanguage = "";

    public static String convertCodeToStatus(int orderStatus) {
       switch (orderStatus)
       {
           case 0:
           return "placed";
           case 1:
           return "Shipping";
           case 2:
           return "Shipped";
           case 3:
           return "Cancelled";
           default:
               return  "Error";
       }
    }

    public static String formatPrice(double price) {
        if (price != 0) {
            DecimalFormat df = new DecimalFormat("#,##0.00");
            df.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(df.format(price)).toString();
            return finalPrice.replace(".", ",");
        } else
            return "0,00";
    }

    public static void setSpanString(String welcome, String name, TextView textView) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);

    }

//    public static Double calculateSizePrice(SizeModel userSelectedSize) {
//        Double result = 0.0;
//        result = userSelectedSize.getPrice() * 1.0;
//        return result;
//    }





    public static void setSpanStringColor(String welcome, String name, TextView textView, int color) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);

    }
    public static void showNotification(Context context, int id , String title , String content, Intent intent){
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            String NOTIFICATION_CHANNEL_ID = context.getString(R.string.default_notification_channel_id);
            NotificationManager notificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                        "ALy Fasa5any",NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription("ALy Fasa5any");
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.RED);
                notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
                NotificationCompat.Builder builder =new NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID);
                builder.setContentTitle(title)
                        .setContentText(content)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher));
                if (pendingIntent != null)
                    builder.setContentIntent(pendingIntent);
                    Notification notification = builder.build();
                    notificationManager.notify(id,notification);


            }

    }
    public static void updateToken(Context context,String newToken) {
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentServerUser.getUid())
                .setValue(new TokenModel(Common.currentServerUser.getPhone(),newToken))
                .addOnFailureListener(e -> {
                    Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    public static String createTopicOrder() {
        return new StringBuilder("/topics/new_order").toString();
    }
}
