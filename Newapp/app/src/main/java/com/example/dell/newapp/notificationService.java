package com.example.dell.newapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
/*
    后台Service，用来访问后端请求数据，发现有更新则发送广播请求弹notification
 */
public class notificationService extends Service {
    public notificationService() {
    }
    private notificationService p;
    //异步获取新闻内容
    class newsConn extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "None";
            String path="http://119.23.240.17:8000/news/";
            try{
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(3000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                String lastTime = params[0];
                String postStr = "time="+lastTime;
                conn.getOutputStream().write(postStr.getBytes());
                int code = conn.getResponseCode();
                if (code == 200){
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    result="";
                    while ((line = br.readLine()) != null) {
                        result += line + "\n";
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result){
            if (result.equals("None")) return;
            //字符串处理部分，无差错处理
            String[] lines = result.split("\n");
            OutputStream fout ;
            int x=0,up_tot=0;
            for (int i=0;i<6;++i){
                try {
                    fout = getApplication().openFileOutput("news"+String.valueOf(i)+".txt", Context.MODE_APPEND);
                    if (x>lines.length)
                    {
                        System.out.println(result);
                        fout.close();
                        break;
                    }

                    int num = Integer.parseInt(lines[x]);
                    up_tot+=num;

                    ++x;
                    for (int j=0;j<num;++j,++x){
                        if (x>lines.length)
                        {
                            System.out.println(result);
                            break;
                        }
                        String ans = lines[x] + "\n";
                        fout.write(ans.getBytes());
                    }
                    fout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String ds = df.format(new Date());
                MainActivity.newsLastTime=ds;

                fout = getApplication().openFileOutput("newsLastTime.txt",Context.MODE_PRIVATE);
                System.out.println(ds);
                fout.write(ds.getBytes());
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //如果有新的新闻则发送广播
            if (up_tot==0) {
            }
            else {
                Intent intent = new Intent();
                intent.setAction(MainActivity.ACTION_NOTIFICATION);
                intent.putExtra("Title","新的新闻");
                intent.putExtra("Content",String.format("更新了%d条消息",up_tot));
                intent.putExtra("code","1");
                sendBroadcast(intent);
                //Intent intent1 = new Intent(p, MainActivity.class);
                //PendingIntent pendingIntent = PendingIntent.getActivity(p, 0, intent1,0);
                /*Notification notification = new NotificationCompat.Builder(p)
                        .setContentTitle("新的新闻")
                        .setContentText(String.format("更新了%d条消息",up_tot))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        //.setContentIntent(pendingIntent)
                        .build();
                startForeground(1,notification);*/
            }

        }
    }
    //异步获取表白墙内容
    class wallConn extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "None";
            String path="http://119.23.240.17:8000/wall/";
            try{
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(3000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                String lastTime = params[0];
                String keyWord = params[1];
                String postStr = "time="+lastTime+"&key_word="+keyWord;
                conn.getOutputStream().write(postStr.getBytes());
                int code = conn.getResponseCode();
                if (code == 200){
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    result="";
                    while ((line = br.readLine()) != null) {
                        if (result.equals(""))
                        {
                            result+=line+"\n";
                        }
                        else {
                            if (keyWord.equals(""))
                            {
                                result+="\t"+line+"\n";
                            }
                            else {
                                result += String.format("关键字：%s\t%s\n", keyWord, line);
                            }
                        }
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }
        @Override
        protected void onPostExecute(String result){
            //字符串处理部分，无差错处理
            if (result.equals("None"))
            {
                return;
            }
            else {
                String[] lines = result.split("\n");
                OutputStream fout;
                int num = Integer.parseInt(lines[0]);
                try {
                    fout = getApplication().openFileOutput("wall.txt", Context.MODE_APPEND);
                    for (int j = 1; j <= num; ++j) {
                        if (j > lines.length) {
                            System.out.println(result);
                            return;
                        }
                        String ans = lines[j] + "\n";
                        fout.write(ans.getBytes());
                    }
                    fout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String ds = df.format(new Date());
                    MainActivity.wallLastTime = ds;

                    fout = getApplication().openFileOutput("wallLastTime.txt", Context.MODE_PRIVATE);
                    System.out.println(ds);
                    fout.write(ds.getBytes());
                    fout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //如果表白墙有更新则发送广播
                if (num == 0){
                    //intent.putExtra("Title","表白墙");
                    //intent.putExtra("Content","没有你关注的人");
                }
                else{
                    Intent intent = new Intent();
                    intent.setAction(MainActivity.ACTION_NOTIFICATION);
                    intent.putExtra("Title","表白墙");
                    intent.putExtra("Content",String.format("更新了%d条你关注的人的表白消息",num));
                    intent.putExtra("code","2");
                    sendBroadcast(intent);
                }

            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate(){

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        p = this;
        System.out.println("notification service!!");
        //请求后端更新新闻，若有更新在onPostExecute部分更新后发送广播到notificationReceiver
        String newsLastTime = "";
        try {
            InputStream fin = getApplication().openFileInput("newsLastTime.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            newsLastTime = bf.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("news last time :%s",newsLastTime));
        new newsConn().execute(newsLastTime);

        //请求后端更新表白墙，若有更新在onPostExecute部分更新后发送广播到notificationReceiver
        String wallLastTime = "";
        try{
            InputStream fin = getApplication().openFileInput("wallLastTime.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            wallLastTime = bf.readLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("wall last time :%s",wallLastTime));

        int x=0;
        try{
            InputStream fin = getApplication().openFileInput("wallSetting.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = bf.readLine())!=null){
                ++x;
                new wallConn().execute(wallLastTime,line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Intent intent1 = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1,0);

        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("新消息")
                .setContentText("hello service!!")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1,notification);*/
        return super.onStartCommand(intent, flags, startId);
    }
}
