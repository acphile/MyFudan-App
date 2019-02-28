package com.example.dell.newapp.classtable;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.TableLayout;

/**
 * Created by dell on 2018/11/24.
 */

public class CourseDetail extends TableLayout {

    public CourseDetail(Context context)
    {
        super(context);
    }

    public CourseDetail(Context context, AttributeSet attrs) {
        super(context, attrs);
        ListView view = new ListView(context);
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed,l, t, r, b);
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

}
