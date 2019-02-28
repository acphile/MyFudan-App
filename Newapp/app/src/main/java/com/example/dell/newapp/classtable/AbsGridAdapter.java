package com.example.dell.newapp.classtable;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.newapp.R;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by dell on 2018/11/24.
 * 用于课表显示的Adapter
 */

public class AbsGridAdapter extends BaseAdapter {

    private Context mContext;
    private static int[] class_time={800,855,955,1050,1145,1330,1425,1525,1610,1715,1830,1925,2020,2115};
    private String[][] contents;
    private int tot=0;
    private int rowTotal;
    private Map hp=null;
    private int columnTotal;

    private int positionTotal;

    public AbsGridAdapter(Context context) {
        this.mContext = context;
        tot=0;
        hp=new HashMap();
    }

    public int getCount() {
        return positionTotal;
    }

    public long getItemId(int position) {
        return position;
    }

    public Object getItem(int position) {
        //获取第几行第几列
        int column = position % columnTotal;
        int row = position / columnTotal;
        return contents[row][column];
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        if( convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.table_grid, null);
        }
        TextView textView = (TextView)convertView.findViewById(R.id.text);
        //如果有课,那么添加数据
        if( !getItem(position).equals("")) {
            String cont=(String)getItem(position),show;
            try {
                show=cont.split(" ")[0];
            }catch (Exception e){
                e.printStackTrace();
                show=cont;
                System.out.println(cont);
            }
            textView.setText(show);
            textView.setTextColor(Color.WHITE);
            //变换颜色，相同课程同一颜色
            if (hp.get(show)==null)
            {
                tot+=1;
                hp.put(show,tot);
            }
            int rand = ((int)hp.get(show)) % columnTotal;
            switch( rand ) {
                case 0:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.grid_item_bg));
                    break;
                case 1:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_12));
                    break;
                case 2:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_13));
                    break;
                case 3:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_14));
                    break;
                case 4:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_15));
                    break;
                case 5:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_16));
                    break;
                case 6:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_17));
                    break;
                case 7:
                    textView.setBackground(mContext.getResources().getDrawable(R.drawable.bg_18));
                    break;
            }

            //点击显示详细信息
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int row = position / columnTotal;
                    int column = position % columnTotal;
                    int hour=class_time[row]/100;
                    int mini=class_time[row]%100;

                    String con = "上课时间: " +String.format("%02d:%02d\n",hour,mini)+contents[row][column];
                    Toast.makeText(mContext, con, Toast.LENGTH_SHORT).show();
                }

            });
        }
        return convertView;
    }

    /**
     * 设置内容、行数、列数
     */

    public void setContent(String[][] contents, int row, int column) {
        this.contents = contents;
        this.rowTotal = row;
        this.columnTotal = column;
        positionTotal = rowTotal * columnTotal;
    }
}

