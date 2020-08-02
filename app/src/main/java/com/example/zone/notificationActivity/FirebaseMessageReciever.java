package com.example.zone.notificationActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.zone.PhotoActivity.PostListActivity;
import com.example.zone.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessageReciever extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // handle when recieve notification via data
        if (remoteMessage.getData().size() > 0) {

            shownotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("message"));
        }
        //handle when notification is recieved
        if (remoteMessage.getNotification()!=null){
            shownotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
        }

    }


    private RemoteViews getCustomDesign(String title,String message){
        //let access our layout view in our java code
    RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.firebasenotification);
    remoteViews.setTextViewText(R.id.title,title);
    remoteViews.setTextViewText(R.id.message,message);
    remoteViews.setImageViewResource(R.id.icon,R.drawable.sanyam);

    return remoteViews;
    }


    public void shownotification(String tittle,String message)
    {

        //here when the user click the notification we want him to take in postlist activity so use intents
        Intent intent = new Intent(this, PostListActivity.class);

        String channel_id =  "Zone_channel";
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//also in intent clearing the previous page

        PendingIntent pendingIntent = PendingIntent .getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); //accessing device notification sound path
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),channel_id).
                setSmallIcon(R.drawable.sanyam).//setting notification icon
                setSound(uri).   //setting notification sound
                setAutoCancel(true).
                setVibrate(new long[]{1000,1000,1000,1000,1000}). //setting vibration mode for notification
                setOnlyAlertOnce(true).setContentIntent(pendingIntent);

        //now if android version smaller than jelly bean i will show default notification else i will show custom design notification design
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN){

            builder = builder.setContent(getCustomDesign(tittle,message));
        }else {
            builder = builder.setContentTitle(tittle).setContentText(message).setSmallIcon(R.drawable.sanyam);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);//now creating notification manager for displaying notification
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)//now android version greater  and equal to oreo i will show notification via notification channel
        {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id,"zone_app",NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setSound(uri,null);
            notificationManager.createNotificationChannel(notificationChannel);

        }

        notificationManager.notify(0,builder.build());

    }
}
