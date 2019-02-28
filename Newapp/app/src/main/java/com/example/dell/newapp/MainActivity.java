package com.example.dell.newapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.BottomNavigationItemView;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private MenuItem menuItem;
    public newsFragment news;
    public wallFragment wall;
    private classtableFragment classtable;
    public static final String ACTION_NOTIFICATION = "action.notification";
    //private settingFragment setting;

    public static int testA=1;
    public static boolean setLock=true;
    public static String newsLastTime=null;
    public static String wallLastTime=null;

    private String userFile,newsLastTimeFile;
    private SearchView searchView;
    //private Button settingBtn;
    //该类用来取消BottomNavigation的滑动效果
    public static class BottomNavigationViewHelper {
        public static void disableShiftMode(BottomNavigationView view) {
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) view.getChildAt(0);
            try {
                Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
                shiftingMode.setAccessible(true);
                shiftingMode.setBoolean(menuView, false);
                shiftingMode.setAccessible(false);
                for (int i = 0; i < menuView.getChildCount(); i++) {
                    BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                    //noinspection RestrictedApi
                    item.setShiftingMode(false);
                    // set once again checked value, so view will be updated
                    //noinspection RestrictedApi
                    item.setChecked(item.getItemData().isChecked());
                }
            } catch (NoSuchFieldException e) {
                Log.e("BNVHelper", "Unable to get shift mode field", e);
            } catch (IllegalAccessException e) {
                Log.e("BNVHelper", "Unable to change value of shift mode", e);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.remove("android:support:fragments");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userFile = "user.txt";
        newsLastTimeFile = "newsLastTime.txt";

        //初始化文件配置
        initial();

        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        BottomNavigationViewHelper.disableShiftMode(bottomNavigationView);

        viewPager = (ViewPager)findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }
                menuItem = bottomNavigationView.getMenu().getItem(position);
                menuItem.setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);

        List<Fragment> list = new ArrayList<>(); //创建页面
        news = new newsFragment();
        classtable = new classtableFragment();
        //setting = new settingFragment();
        wall = new wallFragment();
        list.add(news);
        list.add(wall);
        list.add(classtable);
        //list.add(setting);
        viewPagerAdapter.setList(list);

        viewPager.setCurrentItem(2); //激活所有fragment
        viewPager.setCurrentItem(0);

        //设置AlarmManager定时唤醒服务
        //虽然定时为360s即10分钟，但是国产手机普遍不按照这个规则来...很可能要等很久，甚至会被杀死
        //检验该效果一般要在虚拟机上测试，挂机等个一天，如果复旦有更新新闻（这个频率比较低）就能看到
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        //Intent intent = new Intent(MainActivity.this,backendReceiver.class);
        //PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this,0,intent,0);
        Intent intent = new Intent(MainActivity.this,notificationService.class);
        PendingIntent sender = PendingIntent.getService(MainActivity.this,0,intent,0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),360*1000,sender);
    }

    //初始化新闻和表白墙内容获取时间，第一次默认获取7天前至今
    private String initial_time(String name)
    {
        File file = new File(getFilesDir()+"/"+name);
        String res="";
        if (file.exists()==false){
            System.out.println("hello "+name);
            try{
                FileOutputStream fout = openFileOutput(name,MODE_PRIVATE);

                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                c.set(Calendar.DATE, c.get(Calendar.DATE) - 7);
                Date date= c.getTime();
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String lastTime = df.format(date);
                res=lastTime;
                //String lastTime = "2010-1-1 12:00:00";
                byte[] bytes = lastTime.getBytes();
                fout.write(bytes);
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            try {
                InputStream fin = openFileInput(name);
                BufferedReader bf = new BufferedReader(new InputStreamReader(fin));
                res = bf.readLine();
                fin.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    //初始化关键字记录文件
    private void initial() {
        File file = new File(getFilesDir()+"/"+userFile);
        if (file.exists()==false){
            System.out.println("hello world!");
            try {
                FileOutputStream fout = openFileOutput(userFile,MODE_PRIVATE);
                fout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        newsLastTime=initial_time(newsLastTimeFile);
        wallLastTime = initial_time("wallLastTime.txt");
        System.out.println("TIME: "+newsLastTime+" "+wallLastTime);

        for (int i=0;i<5;++i){
            String newsPart = "news"+String.valueOf(i)+".txt";
            file = new File(getFilesDir()+"/"+newsPart);
            if (file.exists()==false){
                System.out.println("hello"+newsPart);
                try{
                    FileOutputStream fout = openFileOutput(newsPart,MODE_PRIVATE);
                    fout.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        file = new File(getFilesDir()+"/wallSetting.txt");
        if (file.exists() == false){
            FileOutputStream fout = null;
            try {
                fout = openFileOutput("wallSetting.txt",MODE_PRIVATE);
                fout.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //使用菜单填充器获取menu下的菜单资源文件
        getMenuInflater().inflate(R.menu.search_menu,menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        //设置标题栏搜索的事件
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                System.out.println("key:"+query);
                if (TextUtils.isEmpty(query)) {
                    news.searchNews(MainActivity.this,null);
                }
                else {
                    news.searchNews(MainActivity.this,query);
                }
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.equals("")){
                    news.searchNews(MainActivity.this,null);
                    return true;
                }
                else{
                    return false;
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.table_setting:
                System.out.println(MainActivity.this);
                Intent intent = new Intent(MainActivity.this,login.class);
                startActivityForResult(intent,100);
                return true;
            case R.id.wall_setting:
                Intent intent1 = new Intent(MainActivity.this,wallSetting.class);
                //startActivity(intent1);
                startActivityForResult(intent1,101);
                return true;
            case R.id.lock_setting:
                if (setLock){
                    if (classtable.exists_table){
                        classtable.stopclock();
                        item.setTitle("开启上课锁屏服务");
                        Toast.makeText(MainActivity.this,"已关闭",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this,"课表未被设置，锁屏服务没有运行！",Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    if (classtable.exists_table){
                        classtable.setclock();
                        item.setTitle("关闭上课锁屏服务");
                        Toast.makeText(MainActivity.this,"已开启！",Toast.LENGTH_LONG).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this,"请先设置课表再开启锁屏服务！",Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // startActivityForResult回调，方便页面更新
        System.out.println(requestCode);
        System.out.println(resultCode);
        if (requestCode == 100 && resultCode == 100) {
            //课表更新
            System.out.println("clear");
            String name = data.getStringExtra("name");
            String year = data.getStringExtra("year");
            String xq = data.getStringExtra("xq");
            if (xq != classtable.xq || year != classtable.year || name != classtable.name) {
                classtable.update(name, year, xq);
                System.out.println("new classtable");
            }
        }
        if (requestCode == 101){
            //表白墙更新
            wall.update();
        }
    }
    //设置底部导航栏BottomNavigation
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            menuItem = item;
            switch (item.getItemId()) {
                case R.id.newsMenu:

                    searchView.setVisibility(View.VISIBLE);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (TextUtils.isEmpty(query)) {
                                news.searchNews(MainActivity.this,null);
                            }
                            else {
                                news.searchNews(MainActivity.this,query);
                            }
                            return true;
                        }
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            if (newText.equals("")){
                                news.searchNews(MainActivity.this,null);
                                return true;
                            }
                            else{
                                return false;
                            }
                        }
                    });
                    viewPager.setCurrentItem(0);
                    return true;

                case R.id.wallMenu:

                    searchView.setVisibility(View.VISIBLE);
                    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            System.out.println("key:"+query);
                            if (TextUtils.isEmpty(query)) {
                                wall.refreshData(null);
                            }
                            else {
                                wall.refreshData(query);
                            }
                            return true;
                        }
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            if (newText.equals("")){
                                wall.refreshData(null);
                                return true;
                            }
                            else{
                                return false;
                            }
                        }
                    });
                    viewPager.setCurrentItem(1);
                    return true;

                case R.id.classtableMenu:
                    searchView.setVisibility(View.GONE);
                    viewPager.setCurrentItem(2);
                    return true;

                /*
                case R.id.settingMenu:
                    searchView.setVisibility(View.GONE);
                    viewPager.setCurrentItem(3);
                    return true;*/
            }
            return false;
        }
    };
}
