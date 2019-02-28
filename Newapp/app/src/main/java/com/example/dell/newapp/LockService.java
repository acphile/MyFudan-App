package com.example.dell.newapp;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;

import java.util.Calendar;

/**
 * Created by dell on 2018/12/10.
 * 上课时添加自定义锁屏服务
 */

public class LockService extends Service {
    private String cur_class="None";
    private String[][] contents;
    private static int[] class_time={800,855,955,1050,1145,1330,1425,1525,1610,1715,1830,1925,2020,2115};
    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("create lock service");
        LockService.this.registerReceiver(mScreenOffReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterComponent();
    }
    private MyBinder binder = new MyBinder();
    public class MyBinder extends Binder
    {
        public void setCourse(String con)
        {
            cur_class=con;
        }
        public String getCourse()
        {
            return cur_class;
        }
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("set course table");
        String table=intent.getStringExtra("table"); //根据传入课表设置锁屏服务
        if (table!=null) //解析课表
        {
            String[] days=table.split("\n");
            contents = new String[14][7];
            for (int i=0;i<7;++i)
                for (int j=0;j<14;++j) contents[j][i]="";
            for (int i=0; i<days.length; i++)
            {
                String[] courses=days[i].split("\t");
                for (int j=0; j<courses.length; j++)
                {
                    if (!courses[j].equals("None")){
                        contents[j][(i+6)%7]=courses[j];
                    }
                }
            }
        }
        startlock();
        return Service.START_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    private int find_class() //查找当前时间是否有课
    {
        Calendar cal = Calendar.getInstance();
        int cur_h=cal.get(Calendar.HOUR_OF_DAY);// 获取时间
        int cur_m=cal.get(Calendar.MINUTE);
        int day=cal.get(Calendar.DAY_OF_WEEK);
        int cur=cur_h*60+cur_m;
        cur_class=null;
        for (int i=0; i<14; i++)
        {
            int h=class_time[i]/100;
            int m=class_time[i]%100;
            int begin_time=h*60+m,end_time=h*60+m+45;
            //System.out.println(begin_time+" "+end_time+" "+cur);
            if (cur>=begin_time&&cur<=end_time) {
                cur_class=contents[i][(day + 5) % 7];
                //System.out.println(cur_class);
                if (!cur_class.equals("")) {
                    return end_time;
                }
                else {
                    return -1;
                }
            }
        }
        return -1;
    }

    private void startlock() //打开自定义锁屏
    {
        int en=find_class();
        if (en>-1) {
            Intent intent1 = new Intent(LockService.this, LockMainActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.putExtra("content", cur_class);
            intent1.putExtra("endtime", en);
            startActivity(intent1);
        }
    }

    //监听屏幕开关，准备开启自定义锁屏
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) || intent.getAction()
                    .equals(Intent.ACTION_SCREEN_ON)) {
                startlock();
            }
        }
    };

    public void unregisterComponent() {
        if (mScreenOffReceiver != null) {
            LockService.this.unregisterReceiver(mScreenOffReceiver);
        }
    }
}