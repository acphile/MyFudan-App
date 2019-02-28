package com.example.dell.newapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
/*
  登录界面
 */
public class login extends AppCompatActivity {
    private Button login_btn;
    private EditText user_text;
    private EditText password_text;
    private Spinner yearSp;
    private Spinner xqSp;
    private String classtable,result,name,year,pwd,xq;

    //登录和后端连接，获取课表
    class login_conn extends AsyncTask<String, Void, String>{
        //doInBackground获取后端内容
        @Override
        protected String doInBackground(String... params) {
            String path ="http://119.23.240.17:8000/classtable/";
            result = "";
            classtable = "";
            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(30000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                System.out.println(params[0]);
                System.out.println(params[1]);
                String postStr = "name=" + params[0] + "&password=" + params[1] + "&year=" + params[2] + "&xq=" + params[3];
                conn.getOutputStream().write(postStr.getBytes());
                int code = conn.getResponseCode();
                if (code == 200){
                    InputStream is = conn.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    result = br.readLine();
                    System.out.println(result);
                    if (result.indexOf("login success")!=-1){
                        //System.out.println("hello world 2");
                        String line;
                        while ((line = br.readLine()) != null) {
                            classtable+=line + "\n";
                        }
                        System.out.println(classtable);
                    }
                    return result;
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }
        //将课表存储进文件中
        @Override
        protected void onPostExecute(String result){
            System.out.println(result);
            if (result.indexOf("login success")!=-1){
                String fileName ="table_"+name+"_Y"+year+"_X"+xq;
                System.out.println(fileName);
                FileOutputStream fos;
                try {
                    fos = openFileOutput(fileName,MODE_PRIVATE);
                    fos.write(classtable.getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Toast.makeText(login.this,"获取成功！",Toast.LENGTH_LONG).show();
                returnResult();
            }
            else{
                Toast.makeText(login.this,result,Toast.LENGTH_LONG).show();
            }
        }

    }
    //判断文件是否存在
    private boolean fileIsExists(String strFile)
    {
        boolean flag=true;
        try {
            FileInputStream inStream=openFileInput(strFile);
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
    private void returnResult(){
        try {
            FileOutputStream fout = openFileOutput("user.txt",MODE_PRIVATE);
            String tot = name+"\r\n"+ year + "\r\n" + xq;
            byte[] bytes = tot.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent it=new Intent();
        it.putExtra("name",name);
        it.putExtra("year",year);
        it.putExtra("xq",xq);
        setResult(100,it);
        finish();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //user_text:学号|password_text：密码|yearSp：学年|xqSp：学期

        login_btn = (Button)findViewById(R.id.login_btn);
        user_text = (EditText)findViewById(R.id.user);
        yearSp = (Spinner)findViewById(R.id.yearSp);
        xqSp = (Spinner)findViewById(R.id.xqSp);

        password_text = (EditText)findViewById(R.id.password);

        //向后端请求课表
        login_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                name = user_text.getText().toString();
                pwd = password_text.getText().toString();
                year = yearSp.getSelectedItem().toString();
                xq = xqSp.getSelectedItem().toString();
                if (xq.equals("秋季学期")) xq="1学期";
                else if (xq.equals("春季学期")) xq="2学期";
                String filename="table_"+name+"_Y"+year+"_X"+xq;
                System.out.println(filename);
                if (fileIsExists(filename)==false) {
                    Toast.makeText(login.this,"请稍后……",Toast.LENGTH_LONG).show();
                    new login_conn().execute(name, pwd, year, xq);
                }
                else {
                    Toast.makeText(login.this,"之前已获取过",Toast.LENGTH_LONG).show();
                    System.out.println(classtable);
                    returnResult();
                }
            }
        });
    }
}
