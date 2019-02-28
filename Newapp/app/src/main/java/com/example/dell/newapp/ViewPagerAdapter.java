package com.example.dell.newapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by 郑卓睿 on 2018/11/28.
 * 一个ViewPager的Adapter，用链表List存储
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> list;
    public void setList(List<Fragment> list) {
        this.list = list;
        notifyDataSetChanged();
    }
    public void updateList(int index,Fragment fm)
    {
        this.list.set(index,fm);
    }
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return list.get(position);
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0;
    }
}
