package com.example.dell.newapp;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Created by dell on 2018/11/22.
 * 新闻页面显示
 */

public class newsFragment extends android.support.v4.app.Fragment {

    //异步获取新闻更新
    class newsConn extends AsyncTask<String,Void,String>{

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
            //字符串处理部分
            if (result.equals("None"))
            {
                Toast.makeText(getActivity(),"更新失败！",Toast.LENGTH_LONG).show();
                return;
            }
            String[] lines = result.split("\n");
            OutputStream fout ;
            int x=0,up_tot=0;
            for (int i=0;i<6;++i){
                try {
                    fout = getActivity().openFileOutput("news"+String.valueOf(i)+".txt", Context.MODE_APPEND);
                    if (x>lines.length)
                    {
                        Toast.makeText(getActivity(),"Error occurs when receiving data",Toast.LENGTH_LONG).show();
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
                            Toast.makeText(getActivity(),"Error occurs when receiving data",Toast.LENGTH_LONG).show();
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
            /*
            for (int i=0; i<5; i++) {
                //mFragmentArrays[i].mData = mFragmentArrays[i].getData((MainActivity) getActivity(),to[i]);
                mFragmentArrays[i].getData((MainActivity) getActivity(), to[i],null);
                mFragmentArrays[i].update();
            }*/
            searchNews((MainActivity) getActivity(), null);

            try {
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String ds = df.format(new Date());
                MainActivity.newsLastTime=ds;

                fout = getActivity().openFileOutput("newsLastTime.txt",Context.MODE_PRIVATE);
                System.out.println(ds);
                fout.write(ds.getBytes());
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (up_tot==0)
            {
                Toast.makeText(getActivity(), String.format("暂无更新 %s", MainActivity.newsLastTime.split(" ")[1]), Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getActivity(), String.format("共更新%d条消息 %s", up_tot, MainActivity.newsLastTime.split(" ")[1]), Toast.LENGTH_LONG).show();
            }
        }
    }

    private TabLayout tabLayout = null;
    private ViewPager viewPager;
    //private SearchView search;

    private TabFragment[] mFragmentArrays = new TabFragment[5];
    private String[] mTabTitles = new String[5];

    private ArrayList<ArrayList<Data>> datas=new ArrayList<>();
    private ArrayList<Data> tmp=null;
    private int[] to;

    public newsFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //to数组: 历史遗留问题，因为前端显示顺序和后端返回顺序不一样，to数组是一个置换
        to = new int[]{4,5,1,0,3};
        //创建分项新闻页面
        for (int i=0; i<5; i++) {
            mFragmentArrays[i] = TabFragment.newInstance((MainActivity) getActivity(),to[i]);
        }
    }

    //搜索存在某关键字的新闻，供标题搜索栏调用
    public void searchNews(MainActivity activity, String query)
    {
        for (int i=0; i<5; i++) {
            mFragmentArrays[i].getData(activity, to[i], query);
            mFragmentArrays[i].update();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab_layout, container, false);
        tabLayout = (TabLayout) view.findViewById(R.id.tablayout);
        viewPager = (ViewPager) view.findViewById(R.id.tab_viewpager);
        /* 旧的搜索栏
        search=(SearchView) view.findViewById(R.id.searchNews);
        //search.setSubmitButtonEnabled(true);
        //search.onActionViewExpanded();
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            public boolean onQueryTextSubmit(String query) {
                System.out.println("key:"+query);
                if (TextUtils.isEmpty(query)) {
                    searchNews((MainActivity) getActivity(), null);
                } else {
                    searchNews((MainActivity) getActivity(), query);
                }
                return true;
            }
            public boolean onQueryTextChange(String newText)
            {
                return true;
            }
        });*/
        initView();
        return view;
    }

    //刷新，开启异步任务查找是否有更新
    public void refreshnews(){
        System.out.println("last:"+MainActivity.newsLastTime);
        //Toast.makeText(getActivity(),"waiting",Toast.LENGTH_LONG).show();
        new newsConn().execute(MainActivity.newsLastTime);
    }

    //初始化新闻页面
    private void initView() {
        mTabTitles[0] = "复旦新闻";
        mTabTitles[1] = "教务处";
        mTabTitles[2] = "外事处";
        mTabTitles[3] = "计算机学院";
        mTabTitles[4] = "体教部";

        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        PagerAdapter pagerAdapter = new MyViewPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        refreshnews(); //更新新闻
    }

    //子新闻FragmentPagerAdapter
    final class MyViewPagerAdapter extends FragmentPagerAdapter {
        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentArrays[position];
        }

        @Override
        public int getCount() {
            return mFragmentArrays.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles[position];
        }
    }
}

