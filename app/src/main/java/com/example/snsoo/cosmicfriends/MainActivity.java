package com.example.snsoo.cosmicfriends;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity{

    AlarmManager alarm_manager;
    TimePicker alarm_timepicker;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;

        // 알람매니저 설정
        alarm_manager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // 타임피커 설정
        alarm_timepicker = findViewById(R.id.time_picker);

        // Calendar 객체 생성
        final Calendar calendar = Calendar.getInstance();

        // 알람 시작 버튼
        ImageButton alarm_on = findViewById(R.id.btn_start);
        alarm_on.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                // calendar에 시간 셋팅
                calendar.set(Calendar.HOUR_OF_DAY, alarm_timepicker.getHour());
                calendar.set(Calendar.MINUTE, alarm_timepicker.getMinute());
                calendar.set(Calendar.SECOND, 0);

                // 시간 가져옴
                int hour = alarm_timepicker.getHour();
                int minute = alarm_timepicker.getMinute();
                Toast.makeText(MainActivity.this,hour + "시 " + minute + "분에 알람이 울립니다",Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), Alarm_Receiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        getBaseContext(), 0, intent, 0);

                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        });

    }
}
