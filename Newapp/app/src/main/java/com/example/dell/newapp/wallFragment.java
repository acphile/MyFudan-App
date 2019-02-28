package com.example.dell.newapp;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.newapp.refresh.SuperSwipeRefreshLayout;

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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static android.view.View.VISIBLE;

/**
 * Created by 郑卓睿 on 2018/12/12.
 * 表白墙界面
 */

public class wallFragment extends android.support.v4.app.Fragment {
    private List<Data> mData = null;
    private ListAdapter listAdapter;
    private ListView listView;
    private SuperSwipeRefreshLayout refreshTab;
    private TextView textView=null,footerTextView=null;
    private ImageView imageView=null,footerImageView=null;
    private ProgressBar progressBar,footerProgressBar=null;
    private int updateNum=0;
    //异步任务请求后端表白墙数据
    class wallConn extends AsyncTask<String,Void,String> {
        //向后端请求数据
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
            //字符串处理
            if (result.equals("None"))
            {
                Toast.makeText(getActivity(),"更新失败！",Toast.LENGTH_LONG).show();
                //return;
            }
            else {
                //字符串处理部分
                String[] lines = result.split("\n");
                OutputStream fout;
                int num = Integer.parseInt(lines[0]);
                //System.out.println(num+" "+lines.length);
                try {
                    fout = getActivity().openFileOutput("wall.txt", Context.MODE_APPEND);
                    for (int j = 1; j <= num; ++j) {
                        if (j >= lines.length) {
                            Toast.makeText(getActivity(), "Error occurs when receiving data", Toast.LENGTH_LONG).show();
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

                    fout = getActivity().openFileOutput("wallLastTime.txt", Context.MODE_PRIVATE);
                    System.out.println(ds);
                    fout.write(ds.getBytes());
                    fout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                updateNum += num;
            }
            //刷新界面
            refreshData(null);
        }
    }

    //用于显示界面的Adapter
    class ListAdapter extends BaseAdapter{
        private List<Data> mData;

        public ListAdapter(List<Data> data){
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
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.wallitem,viewGroup,false);
            TextView tv0 = (TextView) view.findViewById(R.id.wallkey);
            TextView tv1 = (TextView) view.findViewById(R.id.wallItem);
            TextView tv2 = (TextView) view.findViewById(R.id.wallItemDate);

            String s1 = mData.get(i).getNew_content();
            String s2 = mData.get(i).getNew_date();
            //System.out.println(s);
            tv0.setText(mData.get(i).getNew_title());
            tv1.setText(s1);
            tv2.setText(s2);
            return view;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //alarmManager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
    }
    //进行更新操作
    public void update(){
        InputStream fin;
        int x=0;
        try{
            fin = getActivity().openFileInput("wallSetting.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = bf.readLine())!=null){
                ++x;
                new wallConn().execute(MainActivity.wallLastTime,line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(String.format("updatenum=%d",x));
        System.out.println(MainActivity.wallLastTime);

        updateNum = 0;
        if (x==0) {
            new wallConn().execute(MainActivity.wallLastTime, "");
        }
    }

    public void refreshData(String query) //刷新表白墙，供外部调用
    {
        getData(query);
        listAdapter = new ListAdapter(mData);
        listView.setAdapter(listAdapter);//更换新的Adapter
    }
    //根据搜索栏关键字显示内容
    private void getData(String query){
        mData = new ArrayList<Data>();
        try {
            InputStream fin = getActivity().openFileInput("wall.txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line = bf.readLine())!=null){
                String[] ans = line.split("\t");
                //System.out.println(ans.length);
                Data data = new Data(ans[0],ans[1],ans[2]);
                if (query==null) {
                    mData.add(data);
                }
                else if (ans[1].indexOf(query)!=-1)
                {
                    mData.add(data);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(mData);
    }

    //下拉刷新头部
    private View createHeaderView() {
        View headerView = LayoutInflater.from(refreshTab.getContext())
                .inflate(R.layout.refresh_head, null);
        progressBar = (ProgressBar) headerView.findViewById(R.id.pb_view);
        textView = (TextView) headerView.findViewById(R.id.text_view);
        textView.setText("下拉刷新");
        imageView = (ImageView) headerView.findViewById(R.id.image_view);
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.down_arrow);
        progressBar.setVisibility(View.GONE);
        return headerView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.wall, container, false);
        update();

        listView = (ListView)view.findViewById(R.id.wallLtv);
        listView.setAdapter(listAdapter);

        //回到顶部按钮设置
        final FloatingActionButton fab_up= (FloatingActionButton) view.findViewById(R.id.Gotop);
        fab_up.hide();
        fab_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listView.smoothScrollToPosition(0);
            }
        });
        //设置监听，下拉时回到顶部按钮显示
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            int scrollState;
            int mLastTopIndex=0; //记录上次位置
            int mLastTopPixel=0;
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.scrollState = scrollState;
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                View v = view.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                int scrollDirection = 0;
                if (firstVisibleItem > mLastTopIndex) {
                    scrollDirection = 1;
                } else if (firstVisibleItem < mLastTopIndex) {
                    scrollDirection = 2;
                } else {
                    if(top < mLastTopPixel) {
                        scrollDirection = 1; //下拉
                    } else if(top > mLastTopPixel) {
                        scrollDirection = 2; //上拉
                    }
                }
                mLastTopIndex = firstVisibleItem;
                mLastTopPixel = top;

                if (scrollDirection == 2 && fab_up.getVisibility()==VISIBLE) {
                    fab_up.hide();
                } else if (scrollDirection == 1 && fab_up.getVisibility()!=VISIBLE) {
                    fab_up.show();
                }
            }
        });

        //下拉刷新样式设置
        refreshTab = (SuperSwipeRefreshLayout) view.findViewById(R.id.wallRefresh);
        refreshTab.setHeaderViewBackgroundColor(0xff888888);
        refreshTab.setHeaderView(createHeaderView());// add headerView
        refreshTab.setTargetScrollWithLayout(true);
        //下拉刷新监听
        refreshTab.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {
            @Override
            public void onRefresh() {
                textView.setText("正在刷新");
                imageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                update();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshTab.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                    }
                }, 2000);
            }

            @Override
            public void onPullDistance(int distance) {
                // pull distance
            }
            @Override
            public void onPullEnable(boolean enable) {
                textView.setText((enable ? "松开刷新" : "下拉刷新")+"\n上次更新时间："+MainActivity.wallLastTime.split(" ")[1]);
                imageView.setVisibility(View.VISIBLE);
                imageView.setRotation(enable ? 180 : 0);
            }
        });

        return view;
    }
}
