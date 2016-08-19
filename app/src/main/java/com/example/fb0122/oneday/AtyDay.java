package com.example.fb0122.oneday;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fb0122.oneday.utils.TimeCalendar;
import com.example.fb0122.oneday.weidget.MyEditText;

import java.util.ArrayList;
import java.util.HashMap;

import db_oneday.OneDaydb;
import oneday.Alarm.Config;


public class AtyDay extends Fragment implements TextWatcher {

    public static String TAG = "AtyDay";

    public SlideListView lvDay;
    private ImageButton btnAdd;
    OneDaydb db;   // dedb供删除使用的数据库
    public static int addLinePosition;
    public Context mContext;
    public Cursor c; //dec供删除使用的Cursor
    public MyAdapter adapter;
    public static HashMap<Integer, Object> map = new HashMap<>();
    ChangeHandler handler = new ChangeHandler(Looper.myLooper());
    TimeHandler timeHandler = new TimeHandler(Looper.myLooper());
    public static ArrayList<HashMap<Integer,Object>> listdata = new ArrayList<>() ;

    public AtyDay(Context context, HashMap<Integer, Object> map) {
        super();
        this.mContext = context;
        this.map = map;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.e(TAG,"beforeTextChanged");
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        Log.e(TAG,"onTextChanged");
    }

    @Override
    public void afterTextChanged(Editable editable) {
        Log.e(TAG,"editing...");
    }

    class MyAdapter extends BaseAdapter {

        private Context mContext;
        private Cursor c;

        public MyAdapter(Context context, Cursor c) {
            this.mContext = context;
            this.c = c;
        }

        @Override
        public int getCount() {
            return c.getCount();
        }


        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void removeItem(int position) {
            c.moveToPosition(position);
            int itemId = c.getInt(c.getColumnIndex("_id"));
            db.delete(itemId);
            notifyDataSetChanged();
        }


        //
        //通过position取得当前滑动的item的view
        public void getLinePosition(int position) {
            int firstPosition = lvDay.getFirstVisiblePosition();
            int  lastPosition = lvDay.getLastVisiblePosition();
            if (position >= firstPosition && position <=  lastPosition) {
                View view = lvDay.getChildAt(position - lvDay.getFirstVisiblePosition());
                ViewHolder lineHold = (ViewHolder) view.getTag();
                lvDay.saddLine(lineHold.addLine,lineHold.scEdit ,lineHold.tvSc,db,lineHold.tvTime.getText().toString());
            }
            lvDay.postInvalidate();
        }


        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_item, parent, false);
                holder = new ViewHolder();
                holder.hSView = (RelativeLayout) convertView.findViewById(R.id.hsv);
                holder.ll_intent = (RelativeLayout) convertView.findViewById(R.id.ll_intent);
                holder.tvSc = (TextView) convertView.findViewById(R.id.tvSc);
                holder.scEdit = (MyEditText)convertView.findViewById(R.id.edit_Sc);
                holder.scEdit.setVisibility(View.GONE);
                holder.scEdit.addTextChangedListener(AtyDay.this);
                holder.tvTime = (TextView) convertView.findViewById(R.id.tvTime);
                holder.tvTo = (TextView) convertView.findViewById(R.id.tvTo);
                holder.line = (TextView) convertView.findViewById(R.id.line);
                holder.other1 = (RelativeLayout) convertView.findViewById(R.id.other1);
                holder.other2 = (RelativeLayout) convertView.findViewById(R.id.other2);
                holder.delete1 = (RelativeLayout) convertView.findViewById(R.id.delete1);
                holder.delete2 = (RelativeLayout) convertView.findViewById(R.id.delete2);
                holder.addLine = (TextView) convertView.findViewById(R.id.Line);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();   //当convertView第一次使用时，我们需要创建它。当第二次使用时，就可以使用getTag()直接使用
            }
            if (map.size() > 0 && map.containsKey(position) && map.get(position).equals("undo")) {
                holder.addLine.setVisibility(View.GONE);
            } else if (map.size() > 0 && map.containsKey(position) && map.get(position).equals("do")) {
                holder.addLine.setVisibility(View.VISIBLE);
                holder.tvSc.setTextColor(getResources().getColor(R.color.shaow));
            } else {
                holder.addLine.setVisibility(View.GONE);
            }
            c.moveToPosition(position);

            holder.tvSc.setText(c.getString(1));
            holder.tvTime.setText(c.getString(2));
            holder.tvTo.setText(c.getString(3));

            return convertView;
        }

        //这里使用到了ViewHolder 的复用
        public class ViewHolder {

            public ViewHolder() {

            }

            public RelativeLayout hSView, other1, other2, delete1, delete2;
            public View content;
            public TextView tvTime, line, tvTo;
            public TextView tvSc ;
            private MyEditText scEdit;
            private TextView addLine;
            public RelativeLayout ll_intent;
        }

    }

    public OnItemClickListener listClick = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                long arg3) {
            addLinePosition = position;
            Intent i = new Intent();
            i.setClass(getActivity(), AtyEditCustom.class);
            startActivity(i);
        }


    };

		/*
         * 删除数据时的回调接口
		 */

    public SlideListView.RemoveListener removeListener = new SlideListView.RemoveListener() {

        @Override
        public void removeItem(SlideListView.RemoveDirection reDirection, int position, View itemView) {
            adapter.removeItem(position);
            adapter.notifyDataSetChanged();
            map.remove(position);
            getData();
            lvDay.setAdapter(adapter);
            new Thread(new ChangeData()).start();
        }
    };

    private void getData() {
        String week = TimeCalendar.getTodayWeek();
        db = new OneDaydb(getActivity(), "oneday");
        c = db.Query(week);

        adapter = new MyAdapter(getActivity(), c);
        Log.e(TAG, "cursor" + c.getCount());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.aty_day, null);
        lvDay = (SlideListView) view.findViewById(R.id.lvDay);
        lvDay.initSlideMode(SlideListView.MOD_BOTH, getActivity());
        lvDay.setHeaderDividersEnabled(true);
        btnAdd = (ImageButton) view.findViewById(R.id.btnAdd);
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        getData();

        //为slidelistview传入当前activity的adapter。让其在滑动的时候动态更新adapter
        lvDay.getAda(adapter);
        lvDay.setAdapter(adapter);
        lvDay.setOnItemClickListener(listClick);
        /*
		 * 调用接口删除数据
		 */

        lvDay.setRemoveListener(removeListener);

        lvDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                lvDay.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
            }
        });

        btnAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setClass(getActivity(), AtyEditCustom.class);
                startActivityForResult(i, 0);
                getActivity().overridePendingTransition(R.anim.zoom_in, R.anim.zoom_out);
            }
        });
        return view;


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        c.close();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Config.CHANGE_DATA) {
                    //动态刷新listview 的方法 notifyDataSetChanged()无法刷新listview
                    getData();
                    adapter.notifyDataSetChanged();
                    lvDay.setAdapter(adapter);
                    new Thread(new ChangeData()).start();
                    MainActivity.notiifyList.clear();
                    Message msg = timeHandler.obtainMessage();
                    msg.what = Config.ADD_NOTIFY;
                    timeHandler.sendMessage(msg);

                }
                break;
        }
    }

    class ChangeData implements Runnable {
        @Override
        public synchronized void run() {
            Message msg = handler.obtainMessage();
            msg.what = Config.CHANGE_WEEK_VIEW;
            handler.sendMessage(msg);
        }
    }
}

