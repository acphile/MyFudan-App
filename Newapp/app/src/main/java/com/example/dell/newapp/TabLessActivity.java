package com.example.dell.newapp;

import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import java.util.ArrayList;

/**
 * Created by dell on 2018/11/22.
 * 测试Fragment使用
 */

public class TabLessActivity extends AppCompatActivity {
    private TabLayout tabLayout = null;

    private ViewPager viewPager;

    private Fragment[] mFragmentArrays = new Fragment[5];

    private String[] mTabTitles = new String[5];

    private ArrayList<ArrayList<Data> > datas=new ArrayList<>();
    private ArrayList<Data> tmp=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tab_layout);
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        viewPager = (ViewPager) findViewById(R.id.tab_viewpager);
        initView();
    }

    private void initView() {
        mTabTitles[0] = "推荐";
        mTabTitles[1] = "热点";
        mTabTitles[2] = "科技";
        mTabTitles[3] = "体育";
        mTabTitles[4] = "健康";
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        //设置tablayout距离上下左右的距离
        //tab_title.setPadding(20,20,20,20);
        for (int i = 0; i <= 4; i++) {
            tmp = new ArrayList<Data>();
            for (int j = 0; j <= i * 2 + 1; j++) {
                Data data = new Data("新闻标题" + i, j + "~新闻内容~~~~~~~~","1");
                tmp.add(data);
            }
            datas.add(tmp);
        }
        for (int i=0; i<=4; i++)
            mFragmentArrays[i] = TabFragment.newInstance(datas.get(i));

        PagerAdapter pagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        //将ViewPager和TabLayout绑定
        tabLayout.setupWithViewPager(viewPager);
    }

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
