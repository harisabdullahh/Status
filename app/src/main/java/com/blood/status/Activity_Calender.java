package com.blood.status;

import static com.blood.status.Request.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Activity_Calender extends AppCompatActivity {

    HashSet<CalendarDay> eventDatesOnTime;
    HashSet<CalendarDay> eventDatesLate;
    HashSet<CalendarDay> eventDatesAbsent;
    HashSet<CalendarDay> eventDatesOnLeave;

    MaterialCalendarView calender;
    CardView calender_info;
    TextView calender_ontime_text, calender_late_text, calender_absent_text;
    ImageView back_button_calender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);

        calender = findViewById(R.id.calender);
        calender_info = findViewById(R.id.calender_info);
        calender_ontime_text = findViewById(R.id.calender_ontime_text);
        calender_late_text = findViewById(R.id.calender_late_text);
        calender_absent_text = findViewById(R.id.calender_absent_text);
        back_button_calender = findViewById(R.id.back_button_calender);
        eventDatesOnTime = new HashSet<>();
        eventDatesLate = new HashSet<>();
        eventDatesAbsent = new HashSet<>();
        eventDatesOnLeave = new HashSet<>();

        get_calender(get_calender_lan + emp_id + calender_date_jan);

        //Listener
        back_button_calender.setOnClickListener(v -> {
            onBackPressed();
        });
        calender.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                String Date = String.valueOf(date);
//                Log.d("Trackingx", String.valueOf(date));

                String temp = Date.substring(12);
//                Log.d("Trackingx2", temp);

                String Year = temp.substring(0,4);
//                Log.d("Trackingx3", "Year: " + Year);

                String Month = temp.substring(5);
                String tempMonth = "";
                tempMonth = Month.substring(0,2);
                if(tempMonth.contains("-")){
                    tempMonth = Month.substring(0,1);
                }
//                Log.d("Trackingx3", "tempMonth: " + tempMonth);
                String tempDay = "";
                if(tempMonth.length() == 1){
                    tempDay = temp.substring(7);
                } else if (tempMonth.length() == 2){
                    tempDay = temp.substring(8);
                }
                String Day = tempDay.replace("}","");
//                Log.d("Trackingx3", "tempDay: " + Day);
            }
        });
        calender_info.setOnClickListener(v -> {
            Log.d("Trackingx", String.valueOf(calender.getSelectedDate()));
        });
    }

    @Override
    public void onBackPressed() {
//        Intent i = new Intent(Activity_Calender.this, MainActivity.class);
//        startActivity(i);
        finish();
        super.onBackPressed();
    }

    private OkHttpClient client = new OkHttpClient();

    // Method to make the GET request
    public void get_calender(String url) {
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {

                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            String json = responseBody.string();
                            // Handle the JSON response
                            handleJSONResponse(json);
                            if(debug)
                                Log.d("Tracking: ", "GET Response (Calender) : " + response);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void handleJSONResponse(String json) {
        try {
            JSONObject jsonResponse = new JSONObject(json);

            if (jsonResponse.has("statusCode")) {
                int status_code = jsonResponse.getInt("statusCode");
                if (debug)
                    Log.d("Tracking", "Status Code (Calendar): " + status_code);

                if (status_code == 200) {
                    if (jsonResponse.has("result")) {
                        JSONArray resultArray = jsonResponse.getJSONArray("result");

                        // Create a HashSet of dates to be decorated



                        // Iterate through the resultArray to handle each date and attendance status
                        for (int i = 0; i < resultArray.length(); i++) {
                            JSONObject resultObject = resultArray.getJSONObject(i);

                            // Extract date and attendance status for each entry
                            String date = resultObject.optString("date", "");

                            String attendanceStatus = resultObject.optString("attendancestatus", "");
                            int formattedYear = Integer.parseInt(date.substring(0,4));
                            int formattedMonth = (Integer.parseInt(date.substring(5,7)))-1;
                            int formattedDay = Integer.parseInt(date.substring(8,10));

                            Log.d("Trackingx4", "Year: " + formattedYear);
                            Log.d("Trackingx4", "Month: " + formattedMonth);
                            Log.d("Trackingx4", "Day: " + formattedDay);

                            if(attendanceStatus.equals("OnTime")) {
                                eventDatesOnTime.add(CalendarDay.from(formattedYear, formattedMonth, formattedDay));
                            }
                            if(attendanceStatus.equals("Late")) {
                                eventDatesLate.add(CalendarDay.from(formattedYear, formattedMonth, formattedDay));
                            }
                            if(attendanceStatus.equals("Absent")) {
                                eventDatesAbsent.add(CalendarDay.from(formattedYear, formattedMonth, formattedDay));
                            }if(attendanceStatus.equals("OnLeave")) {
                                eventDatesOnLeave.add(CalendarDay.from(formattedYear, formattedMonth, formattedDay));
                            }



                            // Perform any additional processing or storage as needed
                            if (debug) {
                                Log.d("Tracking", "Date " + i + ": " + date);
                                Log.d("Tracking", "Attendance Status: " + attendanceStatus);
                            }
                        }

                        runOnUiThread(()-> {
                            EventDecorator eventDecoratorOnTime = new EventDecorator(Color.GREEN, eventDatesOnTime);
                            EventDecorator eventDecoratorLate = new EventDecorator(Color.YELLOW, eventDatesLate);
                            EventDecorator eventDecoratorAbsent = new EventDecorator(Color.RED, eventDatesAbsent);
                            EventDecorator eventDecoratorOnLeave = new EventDecorator(Color.BLUE, eventDatesOnLeave);

                            calender.addDecorator(eventDecoratorOnTime);
                            calender.addDecorator(eventDecoratorLate);
                            calender.addDecorator(eventDecoratorAbsent);
                            calender.addDecorator(eventDecoratorOnLeave);
                        });

                    }
                }
            }
        } catch (JSONException e) {
            if (debug)
                Log.e("Tracking", "Error parsing JSON: " + e.getMessage());
        }
    }


    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(int color, Collection<CalendarDay> dates) {
            this.color = color;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(5, color));
        }
    }
}
