package com.example.dell.newapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Random;
/*
    收到广播后根据intent内容发送notification消息通知
 */
public class notificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        System.out.println("notification!");

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(context);
        notificationBuilder.setContentText(intent.getStringExtra("Content"))
                            .setContentTitle(intent.getStringExtra("Title"))
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = notificationBuilder.build();
        //setContentText("hello world").build();
        Random rand = new Random();
        int x = Integer.parseInt(intent.getStringExtra("code"));

        notificationManager.notify(x,notification);
        Intent intent1 = new Intent(context, notificationService.class);
    }
}
