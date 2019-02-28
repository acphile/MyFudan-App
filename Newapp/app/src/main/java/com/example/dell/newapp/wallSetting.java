package com.example.dell.newapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
/*
* 表白墙关键字设置界面
* */
public class wallSetting extends AppCompatActivity {
    private ListView ltv;
    private Button addBtn;
    private EditText edt;
    private Context mContext;
    List<String> mData;
    ListAdapter listAdapter;
    //一个存储关键字界面的Adapter
    class ListAdapter extends BaseAdapter {
        private List<String> mData;

        public ListAdapter(List<String> data){
            mData = data;
        }
        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wallsettingitem,viewGroup,false);
            TextView tv = (TextView) view.findViewById(R.id.wallSettingItm);
            String s = mData.get(position);

            tv.setText("关键字："+s);
            //长按删除关键字
            tv.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(wallSetting.this);
                    builder.setTitle("删除关键字");
                    builder.setMessage(String.format("是否删除表白墙关键字：%s",((TextView)view).getText()));
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            System.out.println("wallsetting0");
                            remove(position);
                            update();
                            Toast.makeText(wallSetting.this, "已删除", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return true;
                }
            });
            return view;
        }
    }

    //更新关键字配置文件
    private void update(){
        mData = new ArrayList<String>();
        try {
            InputStream fin = openFileInput("wallSetting.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = bf.readLine())!=null) {
                mData.add(line);
            }
            fin.close();

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(Calendar.DATE, c.get(Calendar.DATE) - 7);
            Date date= c.getTime();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            MainActivity.wallLastTime = df.format(date);

            OutputStream fout = openFileOutput("wallLastTime.txt",MODE_PRIVATE);
            fout.write(MainActivity.wallLastTime.getBytes());
            fout.close();
            fout = openFileOutput("wall.txt",MODE_PRIVATE);
            fout.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //替换为更新后的Adapter
        listAdapter = new ListAdapter(mData);
        ltv.setAdapter(listAdapter);
    }

    //删除关键字
    private void remove(int position) {
        mData.remove(position);
        System.out.println("wallsetting1");
        try {
            OutputStream fout = openFileOutput("wallSetting.txt",MODE_PRIVATE);
            for (int i = 0;i < mData.size(); ++i){
                fout.write((mData.get(i)+"\n").getBytes());
            }
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //添加关键字
    void add(String s){
        try {
            OutputStream fout = openFileOutput("wallSetting.txt",MODE_APPEND);
            fout.write((s+"\n").getBytes());
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wall_setting);
        //界面设置
        ltv = (ListView) findViewById(R.id.wallSettingltv);
        update();
        addBtn = (Button) findViewById(R.id.wallSettingAdd);
        edt = (EditText) findViewById(R.id.wallSettingEdt);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = edt.getText().toString();
                if (s.length()>0) {
                    if (mData.indexOf(s)>=0) {
                        Toast.makeText(wallSetting.this,"已添加过关键字！",Toast.LENGTH_LONG).show();
                    }
                    else {
                        add(s);
                        edt.setText("");
                    }
                }
                update();
            }
        });
    }
}
