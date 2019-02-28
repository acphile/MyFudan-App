package com.example.dell.newapp;

/**
 * Created by dell on 2018/11/23.
 * 用于向adapter传输数据
 */

public class Data {

    private String new_title;
    private String new_content;
    private String new_date;
    public Data(){}

    public Data(String new_title, String new_content,String date) {
        this.new_title = new_title;
        this.new_content = new_content;
        this.new_date=date;
    }

    public String getNew_title() {
        return new_title;
    }

    public String getNew_content() {
        return new_content;
    }

    public String getNew_date(){return new_date;}

    public void setNew_title(String new_title) {
        this.new_title = new_title;
    }

    public void setNew_date(String new_date) {
        this.new_date = new_date;
    }

    public void setNew_content(String new_content) {
        this.new_content = new_content;
    }
}
