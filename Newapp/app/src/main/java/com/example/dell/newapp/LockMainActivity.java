package com.example.dell.newapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by dell on 2018/12/10.
 * 自定义锁屏界面
 */

public class LockMainActivity extends Activity {

    private TextView lock_date;
    private TextView remain_time;
    private SwipeBackLayout swipeback;
    private String cont="None";
    private int endtime=0;
    private CountDownTimer mTimer=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        setContentView(R.layout.activity_lock_main);
        System.out.println("TEST");
        Intent intent = getIntent();
        cont=intent.getStringExtra("content");
        endtime=intent.getIntExtra("endtime",0);

        remain_time = (TextView) findViewById(R.id.remain);
        Calendar cal = Calendar.getInstance(); //获取当前时间
        int cur_h=cal.get(Calendar.HOUR_OF_DAY);
        int cur_m=cal.get(Calendar.MINUTE);
        int cur_s=cal.get(Calendar.SECOND);
        int remaining=endtime*60-cur_h*60*60-cur_m*60-cur_s;
        remain_time.setText(String.format("还有%d分钟下课",remaining));

        if (mTimer == null) {
            //计时器维护页面下课倒计时
            mTimer = new CountDownTimer((long) (remaining * 1000), 60 * 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if (!LockMainActivity.this.isFinishing()) {
                        int remain = (int) (millisUntilFinished / 60000L);
                        if (remain>0) {
                            remain_time.setText(String.format("还有%d分钟下课", remain));
                        }
                        else {
                            remain_time.setText(String.format("即将下课", remain));
                        }
                        //Log.e("zpan","======remainTime=====" + remainTime);
                    }
                }
                @Override
                public void onFinish() {
                    finish();
                }
            };

            mTimer.start();
        }

        lock_date = (TextView) findViewById(R.id.lock_date);
        swipeback = (SwipeBackLayout) findViewById(R.id.swipeback); //右拉解锁
        swipeback.setSwipeBackListener(new SwipeBackLayout.SwipeBackFinishActivityListener(this));

        String show=cont; //显示当前课程
        try {
            show=cont.split(" ")[0];
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(cont);
        }
        lock_date.setText(show);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //无效返回键
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode==event. KEYCODE_HOME){
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTimer != null) {
            //关闭计时器
            mTimer.cancel();
            mTimer = null;
        }
    }
}