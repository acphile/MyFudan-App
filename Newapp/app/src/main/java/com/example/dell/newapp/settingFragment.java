package com.example.dell.newapp;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by 郑卓睿 on 2018/11/28.
 * 原先的设置页面，现在已不用
 */

public class settingFragment extends android.support.v4.app.Fragment {
    //private Button btn;
    private TextView userSetting;
    private CardView userCard;
    private Button wallSettingBtn;

    private NotificationManager mNManager;
    private Notification notify1;

    private TextView yearTv,xqTv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting, container, false);
        wallSettingBtn = (Button)view.findViewById(R.id.wallSetting);
        userSetting = (TextView) view.findViewById(R.id.usernameSetting);
        userCard = (CardView) view.findViewById(R.id.userCard);
        yearTv = (TextView) view.findViewById(R.id.yearSetting);
        xqTv = (TextView) view.findViewById(R.id.xqSetting);

        System.out.println(Environment.getDataDirectory().getAbsolutePath());
        System.out.println(getActivity().getFilesDir());
        String userFile = "user.txt";
        System.out.println(getActivity().getFilesDir()+"/"+userFile);

        setUserCard();
        wallSettingBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),wallSetting.class);
                getActivity().startActivity(intent);
            }
        });

        userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),login.class);
                getActivity().startActivityForResult(intent,100);
            }
        });

        mNManager = (NotificationManager) getActivity().getSystemService(NOTIFICATION_SERVICE);
        Button btn = (Button) view.findViewById(R.id.btn_test);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Notification.Builder mBuilder = new Notification.Builder(getActivity());
                mBuilder.setContentTitle("叶良辰")                        //标题
                        .setContentText("我有一百种方法让你呆不下去~")      //内容
                        .setSubText("——记住我叫叶良辰")                    //内容下面的一小段文字
                        .setTicker("收到叶良辰发送过来的信息~")             //收到信息后状态栏显示的文字信息
                        .setWhen(System.currentTimeMillis())           //设置通知时间
                        .setSmallIcon(R.mipmap.ic_launcher_round)            //设置小图标
                        //.setLargeIcon(LargeBitmap)                     //设置大图标
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)    //设置默认的三色灯与振动器
                        //.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.biaobiao))  //设置自定义的提示音
                        .setAutoCancel(true);                           //设置点击后取消Notification
                        //.setContentIntent(pit);                        //设置PendingIntent
                notify1 = mBuilder.build();
                mNManager.notify(1, notify1);
                */
                Intent intent1 = new Intent(getActivity(), LockMainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("content","做个简单的测试");
                startActivity(intent1);
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        System.out.println("123456789");
    }

    public void setUserCard(){
        String userFile = "user.txt";
        try {
            System.out.println(getActivity());
            InputStream fin = getActivity().openFileInput(userFile);
            Scanner scan = new Scanner(fin);
            String user,year,xq;
            if (scan.hasNext()){
                user = scan.next();
                year = scan.next();
                xq = scan.next();
                userSetting.setText(user);
                yearTv.setText(year);
                xqTv.setText(xq);
            }
            else userSetting.setText("请进行账号设置");
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void test() throws IOException {
        String fileName = "test.txt";
        String str;
        OutputStream fout = getActivity().openFileOutput(fileName,MODE_PRIVATE);
        str = "hello world!";
        byte[] b = str.getBytes();
        fout.write(b);
        fout.close();

        InputStream fin = getActivity().openFileInput(fileName);
        byte[] bytes = new byte[0];
        bytes = new byte[fin.available()];
        fin.read(bytes);
        fin.close();
        str = new String(bytes);
        System.out.println(str);
    }
}
