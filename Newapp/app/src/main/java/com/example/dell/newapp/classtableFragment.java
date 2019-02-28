package com.example.dell.newapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.newapp.classtable.AbsGridAdapter;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Scanner;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by 郑卓睿 on 2018/11/28.
 * 课表页面显示
 */

public class classtableFragment extends android.support.v4.app.Fragment {
    private AbsGridAdapter tableAdapter;
    private GridView detailCourse;
    private AbsGridAdapter secondAdapter;
    private TextView title;
    private SwipeRefreshLayout mRefresh;
    /*
    //private AlarmManager alarmManager;
    //private static int[] class_time={800,855,955,1050,1145,1330,1425,1525,1610,1715,1830,1925,2020,2115};
    private void setalarm(int position,String show)
    {
        Intent intent = new Intent(getActivity(), LockMainActivity.class);
        intent.putExtra("content",show);
        intent.putExtra("mode",1);
        PendingIntent pi = PendingIntent.getActivity(getActivity(), position*2, intent, 0);
        int row = position / 7;
        int column = position % 7;
        int hour=class_time[row]/100;
        int mini=class_time[row]%100;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.DAY_OF_WEEK,(column+1)%7+1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, mini);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY * 7 ,pi);
    }*/

    //private ScrollView scrollTable;
    private String[][] contents;
    public boolean exists_table=false;
    public String classtable,name,year,xq;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //开辟内存空间
        super.onCreate(savedInstanceState);
        contents = new String[14][7];
        for (int i=0;i<7;++i)
            for (int j=0;j<14;++j) contents[j][i]="";
        setTable();
        //alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //创建视图
        View view = inflater.inflate(R.layout.classtable, container, false);
        detailCourse = (GridView) view.findViewById(R.id.courceDetail);
        secondAdapter = new AbsGridAdapter(getActivity());
        /*
        scrollTable=(ScrollView) view.findViewById(R.id.scrolltable);
        scrollTable.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                mRefresh.setEnabled(scrollTable.getScrollY() == 0);
            }
        });*/

        //简易下拉刷新
        mRefresh = (SwipeRefreshLayout) view.findViewById(R.id.refreshtable);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                setTable();
                secondAdapter.setContent(contents, 14, 7);
                detailCourse.setAdapter(secondAdapter);
                if(mRefresh.isRefreshing()) {
                    //关闭刷新动画
                    mRefresh.setRefreshing(false);
                }
            }
        });

        title=(TextView) view.findViewById(R.id.table_tiltle);
        setTable();
        if (year==null || xq==null) {
            title.setText("请在右上角设置课表！");
        }
        else{
            title.setText(year + " " + xq + " " + "课表");
        }
        secondAdapter.setContent(contents, 14, 7);
        detailCourse.setAdapter(secondAdapter);
        return view;
    }

    private boolean fileIsExists(String strFile) //判断课表文件是否存在，存在则返回内容
    {
        boolean flag=true;
        try {
            FileInputStream inStream=getActivity().openFileInput(strFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(inStream));
            String line = "";
            classtable="";
            while ((line = bf.readLine()) != null) {
                classtable+=line+"\n";
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            flag=false;
        } catch (IOException e){
            e.printStackTrace();
            flag=false;
        }
        return flag;
    }

    public void setclock() //课表存在时开启锁屏服务
    {
        MainActivity.setLock=true;
        if (!classtable.equals("") && year!=null && xq!=null) {
            Intent it = new Intent(getActivity(), LockService.class);
            it.putExtra("table", classtable);
            getActivity().startService(it);
        }
    }

    public void stopclock() //关闭锁屏服务
    {
        MainActivity.setLock=false;
        Intent it = new Intent(getActivity(), LockService.class);
        getActivity().stopService(it);
    }

    public void update(String newName,String newYear, String newXq) //刷新时更新课表页面
    {
        name=newName;
        year=newYear;
        xq=newXq;
        fillContent();
        if (year==null || xq==null ) {
            title.setText("请在右上角设置课表！");
        }
        else{
            title.setText(year + " " + xq + " " + "课表");
        }
        secondAdapter.setContent(contents, 14, 7);
        detailCourse.setAdapter(secondAdapter);
    }

    private void fillContent(){ //载入课表并解析
        String classtableFileName = "table_"+name+"_Y"+year+"_X"+xq;
        if (fileIsExists(classtableFileName)==false){
            exists_table=false;
            Toast.makeText(getActivity(),"本地找不到该课表，请重新获取！",Toast.LENGTH_LONG).show();
        }
        else {
            exists_table=true;
            String[] days=classtable.split("\n");
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
            if (MainActivity.setLock){
                setclock();
            }
        }
    }

    private void setTable(){ //设置课表页面
        try {
            InputStream userin = getActivity().openFileInput("user.txt");
            Scanner scanUser = new Scanner(userin);
            if (scanUser.hasNext()){
                name = scanUser.next();
                year = scanUser.next();
                xq = scanUser.next();
                scanUser.close();
                fillContent();
            }
            else{
                Toast.makeText(getActivity(),"获取当前课表需先设置！",Toast.LENGTH_LONG).show();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
