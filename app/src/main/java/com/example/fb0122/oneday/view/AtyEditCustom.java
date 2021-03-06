package com.example.fb0122.oneday.view;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fb0122.oneday.R;
import com.example.fb0122.oneday.utils.TimeCalendar;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import db_oneday.OneDaydb;
import oneday.Alarm.Config;

public class AtyEditCustom extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, OnClickListener {

    private final static String TAG = "AtyEditCustom";
    public static final String TIMEPICKER_TAG = "timepickerdialog";

    Toolbar toolbar;

    private TextView etCusTime, etCusToTime;
    final Calendar calendar = Calendar.getInstance();
    final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);
    boolean c;
    private EditText etCustom;
    private SQLiteDatabase dbWrite;
    private TextView Sun, Mon, Tue, Wed, Thu, Fri, Sat;
    ContentValues cv = new ContentValues();
    public static int a = 0;
    public static String title = "oneday";
    public static List<Integer> list = new ArrayList<>();

    private OneDaydb db = new OneDaydb(this, "oneday");
    private Calendar alacale = Calendar.getInstance();
    private String spinnerTick;
    private ArrayList<String> saveWeek = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);

        etCusTime = (TextView) findViewById(R.id.etCusTime);
        etCusToTime = (TextView) findViewById(R.id.etCusToTime);
        etCustom = (EditText) findViewById(R.id.etCustom);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //清楚list内存储的选择星期的数据
        list.clear();

        initView();

        //Material Desigh Toolbar loading
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        Spinner s = (Spinner) findViewById(R.id.etExRemind);
        ArrayAdapter spadapter = ArrayAdapter.createFromResource(this, R.array.repece_times, android.R.layout.simple_dropdown_item_1line);
        spadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //获取spinner选择字符
        spinnerTick = s.getSelectedItem().toString();


        s.setAdapter(spadapter);

        dbWrite = db.getWritableDatabase();

        etCusTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                c = true;
                timepicker();
            }
        });
        etCusToTime.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                c = false;
                timepicker();

            }
        });

    }

    private void initView() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Sun = (TextView) findViewById(R.id.Sun);
        Mon = (TextView) findViewById(R.id.Mon);
        Tue = (TextView) findViewById(R.id.Tue);
        Wed = (TextView) findViewById(R.id.Wed);
        Thu = (TextView) findViewById(R.id.Thu);
        Fri = (TextView) findViewById(R.id.Fri);
        Sat = (TextView) findViewById(R.id.Sat);

        Sun.setOnClickListener(this);
        Mon.setOnClickListener(this);
        Tue.setOnClickListener(this);
        Wed.setOnClickListener(this);
        Thu.setOnClickListener(this);
        Fri.setOnClickListener(this);
        Sat.setOnClickListener(this);

    }


    public void Done(View v) {
        if (etCustom.getText().toString().equals("")){
            Toast.makeText(getBaseContext(),"您还没有输入习惯",Toast.LENGTH_SHORT).show();
            return;
        }
        if (saveWeek.size() > 0) {
            for (int j = 0; j < saveWeek.size(); j++) {
                Log.e(TAG,"insert");
                db.insertData(etCustom.getText().toString(),etCusTime.getText().toString(),
                        etCusToTime.getText().toString(),"周" + saveWeek.get(j));
            }
        }else {
            db.insertData(etCustom.getText().toString(),etCusTime.getText().toString(),
                    etCusToTime.getText().toString(),TimeCalendar.getTodayWeek());
        }
        setResult(Config.CHANGE_DATA);
        finish();
        this.overridePendingTransition(R.anim.scoredetail_in,R.anim.scoredetail_out);
    }

    public void timepicker() {
        timePickerDialog.show(getSupportFragmentManager(), TIMEPICKER_TAG);
    }


    public void backTo(View v) {
        finish();
        this.overridePendingTransition(R.anim.scoredetail_in,R.anim.scoredetail_out);
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
        if (c) {
                if (minute < 10) {
                    String minute_str = "0" + minute;
                    etCusTime.setText(hourOfDay + ":" + minute_str);
                } else {
                    etCusTime.setText(hourOfDay + ":" + minute + "");
                }
                alacale.setTimeInMillis(System.currentTimeMillis());
                alacale.set(Calendar.HOUR_OF_DAY, hourOfDay);
                alacale.set(Calendar.MINUTE, minute);
                alacale.set(Calendar.SECOND, 0);
                alacale.set(Calendar.MILLISECOND, 0);

            } else {
                if (minute < 10) {
                    String minute_str = "0" + minute;
                    etCusToTime.setText(hourOfDay + ":" + minute_str);
                } else {
                    etCusToTime.setText(hourOfDay + ":" + minute + "");
                }
        }
    }

    /*
        根据设置的提前时间计算需要提示的时间
     */
    private String dealTime(String spinner,String originTime){
        int hour  = Integer.valueOf(originTime.split(":")[0]);
        int minute = Integer.valueOf(originTime.split(":")[1]);
        switch (spinner){
            case "提前10分钟":
                minute -= 10;
            break;
            case "提前30分钟":
                minute -= 10;
                break;
            case "提前1小时":
                break;
            case "提前2小时":
                break;

        }
        return originTime;
    }
    @Override
    public void onClick(View view) {
        TextView tv = (TextView) view;
        if (tv.getTag()!= null && (int)tv.getTag() == 0) {
            tv.setBackground(getResources().getDrawable(R.drawable.week_click));
            saveWeek.remove(tv.getText().toString());
            tv.setTag(1);
        } else {
            tv.setBackground(getResources().getDrawable(R.drawable.double_click));
            saveWeek.add(tv.getText().toString());
            tv.setTag(0);
        }

    }

}
