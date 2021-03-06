package oneday.Alarm;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.example.fb0122.oneday.utils.TimeCalendar;

import java.util.ArrayList;
import java.util.Calendar;

public class NotifyService extends Service {

    private ArrayList<String> list = new ArrayList<>();
    public static final String NOTIFY_ACTION = "NotifyService.Intent";
    public static final String SERVICE_DESTROY_ACTION = "ServiceDestroy.Intent";
    private boolean quit = false;

    private String week;
    private String hour;
    private String minute;
    private FilterCustomAsync filterCustomAsync;

    private int totalNotifies = 0;

    public void setWeek(String week) {
        this.week = week;
    }

    public String getWeek() {
        return week;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getHour() {
        return hour;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getMinute() {
        return minute;
    }

    @Override
    public void onCreate() {
        filterCustomAsync = new FilterCustomAsync();
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null) {
            list.clear();
            list = intent.getStringArrayListExtra("time");
            if (!intent.getStringExtra(Config.NOTIFY_TODAY).equals(TimeCalendar.getTodayWeek())){
                totalNotifies = 0;
            }
        }
        if (!(filterCustomAsync.getStatus() == AsyncTask.Status.RUNNING)) {
            filterCustomAsync.execute(0);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void getTime() {
        Calendar c = Calendar.getInstance();
        hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
        minute = String.valueOf(c.get(Calendar.MINUTE));
        setWeek(TimeCalendar.getTodayWeek());
        setHour(hour);
        setMinute(minute);
    }

    @Override
    public void onDestroy() {
        this.quit = true;
        Intent i = new Intent(SERVICE_DESTROY_ACTION);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("timeList", list);
        i.putExtras(bundle);
        i.putExtra(Config.NOTIFY_NUMBER,totalNotifies);
        sendBroadcast(i);
        super.onDestroy();
    }

    class FilterCustomAsync extends AsyncTask<Integer,Integer,String>{

        @Override
        protected String doInBackground(Integer... values) {
            String finalTime = "";
            while (!quit) {
                getTime();
                String nowTime;
                if (Integer.parseInt(getMinute()) < 10) {
                    nowTime = getHour() + ":0" + getMinute();
                } else {
                    nowTime = getHour() + ":" + getMinute();
                }
                for (int i = 0; i < list.size(); i++) {
                    if (nowTime.equals(list.get(i))) {
                        Intent notifyIntent = new Intent(NOTIFY_ACTION);
                        notifyIntent.putExtra("nowTime", list.get(i));
                        finalTime = list.get(i);
                        list.remove(nowTime);
                        totalNotifies++;
                        notifyIntent.putExtra(Config.NOTIFY_NUMBER,totalNotifies);
                        sendBroadcast(notifyIntent);
                    }
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return finalTime;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

}
