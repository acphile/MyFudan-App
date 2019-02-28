package com.example.dell.newapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by dell on 2018/11/22.
 * 每条新闻的显示
 */


public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.AuthorViewHolder> {
    private List<Data> mData;
    private Activity mActivity;

    public RecyclerAdapter(List<Data> mData, Activity mActivity)
    {
        this.mData=mData;
        this.mActivity=mActivity;
    }
    @Override
    public AuthorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View childView = inflater.inflate(R.layout.item, parent, false);
        AuthorViewHolder viewHolder = new AuthorViewHolder(childView);
        return viewHolder;
    }

    //日期格式
    public static String timelongTOdate(Long timelong, String style_yyMMddHHmmss) {
        return (new SimpleDateFormat(style_yyMMddHHmmss, Locale.getDefault())).format(timelong);
    }
    @Override
    public void onBindViewHolder(AuthorViewHolder holder, int position) {
        final Data data=mData.get(position);
        holder.mTitle.setText(data.getNew_title());
        String news_date=data.getNew_date();
        holder.mDate.setText(news_date);
        String cur_date=timelongTOdate(System.currentTimeMillis(), "yyyy-MM-dd");
        //System.out.println(cur_date+"+"+news_date);
        //当前日期红色显示
        if (news_date.equals(cur_date))
        {
            holder.mTitle.setTextColor(0xffff4500);
        }
        else {
            holder.mTitle.setTextColor(0xff000000);
        }
        //点击标题浏览器打开新闻链接
        holder.mTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(data.getNew_content());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                mActivity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class AuthorViewHolder extends RecyclerView.ViewHolder {

        TextView mTitle;
        TextView mDate;
        public AuthorViewHolder(View itemView) {
            super(itemView);

            mTitle = (TextView) itemView.findViewById(R.id.news_title);
            mDate = (TextView) itemView.findViewById(R.id.news_date);
        }
    }
}