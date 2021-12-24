package com.example.calendar;

import static android.app.Notification.DEFAULT_ALL;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.content.Context;
import android.widget.Toast;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    static HashMap<String, ArrayList<Data>> todoMap = new HashMap<String, ArrayList<Data>>();
    static HashMap<String, DayViewDecorator> dotMap = new HashMap<String, DayViewDecorator>();
    private Context mContext;
    private ArrayList<Data> mArrayList;
    private Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private TextView title;
    private TextView today;
    private EditText todoInput;
    private Button plusBtn;
    private MaterialCalendarView calendarView;
    final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    SimpleDateFormat dFormat;
    String dateText;
    CalendarDay selectedDate;

    DayViewDecorator deco;
    String todo;

    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";

    // Channel을 생성 및 전달해 줄 수 있는 Manager 생성
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;

    //현재 날짜
    long now = System.currentTimeMillis();
    Date date = new Date(now);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/d");
    String getToday = sdf.format(date);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_Calendar);
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);

        calendarView= findViewById(R.id.calendarView);
        title=findViewById(R.id.title);
        today=findViewById(R.id.today); //오늘 날짜 표시
        todoInput=findViewById(R.id.todoInput);
        plusBtn=findViewById(R.id.plusBtn);

        //오늘날짜로 date 초기화
        dFormat = new SimpleDateFormat("yyyy/MM/d");

        mContext = this;
        mRecyclerView = findViewById (R.id.recycler);

        //리사이클러뷰의 항목 배치
        LinearLayoutManager layoutManager = new LinearLayoutManager (mContext, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager (layoutManager);

        mAdapter = new Adapter (mContext, mArrayList);
        mRecyclerView.setAdapter (mAdapter);

        calendarView.setSelectedDate(CalendarDay.today());

        calendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                new OneDayDecorator(),
                new DayDecorator()
        );

        calendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                selectedDate = date;
                dateText = dFormat.format(date.getCalendar().getTime());
                today.setText(dateText);
                todoInput.setText("");
                if (todoMap.containsKey(dateText)) {    //기존 일정 있으면
                    mArrayList = todoMap.get(dateText);
                    mAdapter = new Adapter (mContext, mArrayList);
                } else {
                    mAdapter = new Adapter (mContext, null);
                }
                mRecyclerView.setAdapter (mAdapter);
            }
        });

        //+버튼
        plusBtn.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View view) {
                if(todoInput.getText().toString().trim().equals("")){
                    Toast.makeText (mContext,"내용을 입력해주세요.", Toast.LENGTH_SHORT).show ();
                }else{
                    addTodo(dateText);
                    dotCheck();
                }
            }
        });

    }

    public void addTodo(String dateText) {
        todo = todoInput.getText().toString();
        todoInput.setText("");

        //기존 일정 없으면, 빈 arrayList 추가
        if (!todoMap.containsKey(dateText)) {
            mArrayList = new ArrayList<Data>();
            todoMap.put(dateText, mArrayList);
        }

        //Notification
        if(dateText.equals(getToday)) {
            sendNotification();
        }
        createNotificationChannel();

        mArrayList = todoMap.get(dateText);
        Data data = new Data(todo);
        mArrayList.add(data);
        todoMap.put(dateText, mArrayList);

        mAdapter = new Adapter (mContext, mArrayList);
        mRecyclerView.setAdapter (mAdapter);
    }

    //일정 점 표시
    public void dotCheck() {
        if (todoMap.get(dateText).size()==1 && (dotMap.get(dateText) == null)) {
            deco = new EventDecorator(Color.rgb(238, 182, 0), Collections.singleton(selectedDate), MainActivity.this);
            calendarView.addDecorator(deco);
            dotMap.put(dateText, deco);
        }
        if (todoMap.get(dateText).size()<=0) {
            calendarView.removeDecorator(dotMap.get(dateText));
            dotMap.remove(dateText);
        }
    }

    public void createNotificationChannel() {
        //notification manager 생성
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        if(android.os.Build.VERSION.SDK_INT
                >= android.os.Build.VERSION_CODES.O){
            //Channel 정의 생성자( construct 이용 )
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID
                    ,"Test Notification",mNotificationManager.IMPORTANCE_HIGH);
            //Channel에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Calendar");
            // Manager을 이용하여 Channel 생성
            mNotificationManager.createNotificationChannel(notificationChannel);
        }

    }

    // Notification Builder를 만드는 메소드
    private NotificationCompat.Builder getNotificationBuilder() {
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setSmallIcon(R.drawable.calendar)
                .setDefaults(DEFAULT_ALL)
                .setContentTitle("오늘 일정")
                .setContentText(todo);
        return notifyBuilder;
    }

    // Notification을 보내는 메소드
    public void sendNotification(){
        // Builder 생성
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        // Manager를 통해 notification 디바이스로 전달
        mNotificationManager.notify(NOTIFICATION_ID,notifyBuilder.build());
    }
}

