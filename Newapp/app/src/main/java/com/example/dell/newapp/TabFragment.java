package com.example.dell.newapp;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.newapp.refresh.SuperSwipeRefreshLayout;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.view.View.VISIBLE;

/**
 * Created by dell on 2018/11/22.
 * 子新闻页面
 */

public class TabFragment extends Fragment {
    public List<Data> mData;

    private int position;
    private RecyclerView recyclerView = null;
    private SuperSwipeRefreshLayout refreshTab;

    //根据查询关键字显示页面新闻，x对应哪一子类新闻
    public void getData(MainActivity mainActivity,int x,String query){
        mData = new ArrayList<Data>();
        System.out.print(mData.size()+" ");
        try {
            InputStream fin = mainActivity.openFileInput("news"+String.valueOf(x)+".txt");
            BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
            String line;
            while ((line=bf.readLine())!=null){
                String[] content = line.split("\t");
                Data data= new Data(content[1],content[0],content[2]);
                //System.out.println(line);
                if (query==null) {
                    mData.add(data);
                    //System.out.print(mData.size()+" ");
                }
                else if (content[1].indexOf(query)!=-1)
                {
                    mData.add(data);
                    //System.out.print(mData.size()+" ");
                }
            }
            System.out.println(query+" "+mData.size());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.reverse(mData);
    }

    public static TabFragment newInstance(MainActivity mainActivity, int position){
        TabFragment fragment = new TabFragment();
        fragment.position = position;
        fragment.getData(mainActivity,position,null);
        return fragment;
    }
    public static Fragment newInstance(List<Data> mData) {
        TabFragment fragment = new TabFragment();
        fragment.mData=mData;
        return fragment;
    }

    private TextView textView=null,footerTextView=null;
    private ImageView imageView=null,footerImageView=null;
    private ProgressBar progressBar,footerProgressBar=null;
    /* 上拉加载更多
    private View createFooterView() {
        View footerView = LayoutInflater.from(refreshTab.getContext())
                .inflate(R.layout.refresh_foot, null);
        footerProgressBar = (ProgressBar) footerView
                .findViewById(R.id.footer_pb_view);
        footerImageView = (ImageView) footerView
                .findViewById(R.id.footer_image_view);
        footerTextView = (TextView) footerView
                .findViewById(R.id.footer_text_view);
        footerProgressBar.setVisibility(View.GONE);
        footerImageView.setVisibility(View.VISIBLE);
        footerImageView.setImageResource(R.drawable.down_arrow);
        footerTextView.setText("上拉加载更多...");
        return footerView;
    }
    */

    //下拉刷新
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

    //刷新新闻页面，供外部调用
    public void update(){
        if (recyclerView != null)
            recyclerView.setAdapter(new RecyclerAdapter(this.mData,getActivity()));
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tab, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler);
        final FloatingActionButton fab_up= (FloatingActionButton) rootView.findViewById(R.id.Gotop);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //System.out.println(mData.size());
        recyclerView.setAdapter(new RecyclerAdapter(this.mData,getActivity()));

        //设置回到顶部按钮
        fab_up.hide();
        fab_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.smoothScrollToPosition(0);
            }
        });

        //回到顶部按钮只有下拉时显示
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //lastItemPosition = layoutManager.findLastVisibleItemPosition();
                if (dy>0 && fab_up.getVisibility()!=VISIBLE) {
                    fab_up.show();
                } else if (dy<0 && fab_up.getVisibility()==VISIBLE) {
                    fab_up.hide();
                }
            }
        });

        //下拉刷新样式设置
        refreshTab = (SuperSwipeRefreshLayout) rootView.findViewById(R.id.tab_refresh);
        refreshTab.setHeaderViewBackgroundColor(0xff888888);
        refreshTab.setHeaderView(createHeaderView());// add headerView
        //refreshTab.setFooterView(createFooterView());

        refreshTab.setTargetScrollWithLayout(true);
        //下拉刷新监听器
        refreshTab.setOnPullRefreshListener(new SuperSwipeRefreshLayout.OnPullRefreshListener() {
                    @Override
                    public void onRefresh() {
                        textView.setText("正在刷新");
                        imageView.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                refreshTab.setRefreshing(false);
                                progressBar.setVisibility(View.GONE);
                            }
                        }, 2000);
                        //找到父fragment并调用刷新，每次子类新闻一起刷新
                        newsFragment tmp = (newsFragment)getParentFragment();
                        tmp.refreshnews();
                    }

                    @Override
                    public void onPullDistance(int distance) {
                        // pull distance
                    }
                    @Override
                    public void onPullEnable(boolean enable) {
                        textView.setText((enable ? "松开刷新" : "下拉刷新")+"\n上次更新时间："+MainActivity.newsLastTime.split(" ")[1]);
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setRotation(enable ? 180 : 0);
                    }
                });
        /* 上拉加载更多的监听器
        refreshTab.setOnPushLoadMoreListener(new SuperSwipeRefreshLayout.OnPushLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        footerTextView.setText("正在加载...");
                        footerImageView.setVisibility(View.GONE);
                        footerProgressBar.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                footerImageView.setVisibility(View.VISIBLE);
                                footerProgressBar.setVisibility(View.GONE);
                                refreshTab.setLoadMore(false);
                            }
                        }, 5000);
                    }
                    @Override
                    public void onPushEnable(boolean enable) {
                        footerTextView.setText(enable ? "松开加载" : "上拉加载");
                        footerImageView.setVisibility(View.VISIBLE);
                        footerImageView.setRotation(enable ? 0 : 180);
                    }

                    @Override
                    public void onPushDistance(int distance) {

                    }
                });
        */
        return rootView;
    }

}

