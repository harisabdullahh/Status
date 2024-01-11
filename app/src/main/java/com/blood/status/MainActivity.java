package com.blood.status;

import static com.blood.status.Request.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private String SHARED_PREFS = "shared_prefs";
    private static final int pic_id = 123;
    String convertedImage;
    private String firstName = "";
    private String firstMessage;
    private boolean post_success = false;
    private boolean get_success = false;
    public boolean use_wan = false;
    private boolean post_pressed = false;
    private boolean get_pressed = false;
    private boolean hold_button = false;
    private boolean start_func = false;
    MaterialButton get_button, post_button;
    LinearLayout LinearMain;
    SwipeRefreshLayout swipeRefreshLayout;
    private MaterialButton _lan_button, _wan_button;
    private TextView _date_text, _time_out_text, _post_text, _network_text, _time_in_text, _name_text, _ontime_text, _late_text, _leave_text;
    private CardView _card_prompt, _card_ontime, _card_late, _card_absent, _card_leave;
    private ImageView _settings_button;
    private ProgressBar _progressBar1;
    private ProgressBar _progressBar2;
    private Handler handler;
    private Runnable _updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start_func = true;
        initView();
    }

    @Override
    protected void onStart() {
        getLink();
        if(get_success){
            String formattedDate = convertDateFormat(status_date, outputDateFormat);
            String formattedTime = convertTimeFormat(status_time, outputTimeFormat);
            _time_in_text.setText(formattedTime);
            _date_text.setText(formattedDate);
            _name_text.setText(firstName);
        }

//        SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
//        device_id = sh.getString("DEVICEID", "");
//        emp_id = sh.getString("EMPID", "");

        super.onStart();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            convertedImage = new String(Base64.encodeToString(byteArray, Base64.DEFAULT));
            if(debug)
                Log.d("Tracking", "Image captured and saved.");
            post_button.setVisibility(View.GONE);
            _progressBar1.setVisibility(View.VISIBLE);
            sendPostRequest();
        }
    }

    private void ping() {
        new PingTask().execute("172.16.100.111");
    }

    protected void initView() {

        //Initializtion
        LinearMain = findViewById(R.id.LinearMain);
//        get_button = findViewById(R.id.get_button);
        post_button = findViewById(R.id.post_button);
        _date_text = findViewById(R.id.date_text);
        _time_out_text = findViewById(R.id.time_out_text);
        _post_text = findViewById(R.id.post_text);
        _name_text = findViewById(R.id.name_text);
        _settings_button = findViewById(R.id.settings_button);
        _progressBar1 = findViewById(R.id.progressBar1);
//        _progressBar2 = findViewById(R.id.progressBar2);
        _card_prompt = findViewById(R.id.card_prompt);
        _time_in_text = findViewById(R.id.time_in_text);
        _ontime_text = findViewById(R.id.ontime_text);
        _late_text = findViewById(R.id.late_text);
        _leave_text = findViewById(R.id.leave_text);
        _card_ontime = findViewById(R.id.card_ontime);
        _card_late = findViewById(R.id.card_late);
        _card_absent = findViewById(R.id.card_absent);
        _card_leave = findViewById(R.id.card_leave);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);



        //Action
        _progressBar1.setVisibility(View.GONE);
//        _progressBar2.setVisibility(View.GONE);
        _card_prompt.setVisibility(View.GONE);
        swipeRefreshLayout.setColorSchemeResources(R.color.main_color);


        //Listeners

        _card_ontime.setOnClickListener(v -> {
            openCalender();
        });

        _card_absent.setOnClickListener(v -> {
            openCalender();
        });

        _card_late.setOnClickListener(v -> {
            openCalender();
        });

        _card_leave.setOnClickListener(v -> {
            openCalender();
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!emp_id.equals("") && !device_id.equals("")){
                    if(!hold_button){
//                        get_button.setVisibility(View.GONE);
//                        _progressBar2.setVisibility(View.VISIBLE);
                        get_pressed = true;
                        ping();
                    }
                }
            }
        });

//        get_button.setOnClickListener(v -> {
//            if(!emp_id.equals("") && !device_id.equals("")){
//                if(!hold_button){
//                    get_button.setVisibility(View.GONE);
//                    _progressBar2.setVisibility(View.VISIBLE);
//                    get_pressed = true;
//                    ping();
//                }
//            }
//        });

        post_button.setOnClickListener(v -> {

            if(!emp_id.equals("") && !device_id.equals("")) {
                if (!hold_button) {
                    SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
                    String temp1 = sh.getString("IMAGE", "");
                    if(!temp1.equals("")){
                        convertedImage = temp1;
                    }
                    post_button.setVisibility(View.GONE);
                    _progressBar1.setVisibility(View.VISIBLE);
                    post_pressed = true;
                    ping();
                }
            }
        });

        _settings_button.setOnClickListener(v -> {
            Intent i = new Intent(this, Activity_Settings.class);
            startActivity(i);
        });
    }

    private OkHttpClient client = new OkHttpClient();

        // Method to make the GET request
        public void makeGetRequest(String url) {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            get_success = true;
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                String json = responseBody.string();
                                // Handle the JSON response
                                handleJSONResponse(json);
                                if(debug)
                                    Log.d("Tracking: ", "GET Response: " + response);
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

        public void getData(String url) {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) {
                    if (response.isSuccessful()) {
                        try {
                            get_success = true;
                            ResponseBody responseBody = response.body();
                            if (responseBody != null) {
                                String json = responseBody.string();
                                handleJSONResponse(json);
                                if(debug)
                                    Log.d("Tracking: ", "GET Response: " + response);
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


    public static String extractFullName(String jsonResponse) {
        try {
            JSONObject responseObj = new JSONObject(jsonResponse);
            if (responseObj.has("result") && !responseObj.isNull("result")) {
                if (responseObj.get("result") instanceof JSONArray) {
                    JSONArray resultArray = responseObj.getJSONArray("result");

                    if (resultArray.length() > 0) {
                        JSONObject employeeObj = resultArray.getJSONObject(0);
                        String firstName = employeeObj.getString("firstName");
                        String lastName = employeeObj.getString("lastName");
                        return firstName + " " + lastName;
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void handleJSONResponse(String json) {
        firstName = extractFullName(json);
        SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("firstName", firstName);
        editor.apply();
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray resultArray = jsonObject.getJSONArray("result");

            if (resultArray.length() > 0) {
                JSONObject firstResult = resultArray.getJSONObject(0);

                String lastAttendance = firstResult.getString("lastAttendance");
                if (debug)
                    Log.d("Tracking: ", lastAttendance);
                status_result = lastAttendance;
                int indexOfT = lastAttendance.indexOf('T');
                int lengthOfT = lastAttendance.length();
                status_date = lastAttendance.substring(0, indexOfT);
                status_time = lastAttendance.substring(indexOfT + 1, indexOfT + 6);

                JSONArray attendanceStatusArray = firstResult.getJSONArray("attendanceStatus");
                List<String> attendanceStatusList = new ArrayList<>();

                for (int i = 0; i < attendanceStatusArray.length(); i++) {
                    JSONObject statusObject = attendanceStatusArray.getJSONObject(i);
                    int total = statusObject.getInt("total");
                    String name = statusObject.getString("name");
                    String statusInfo = name + ": " + total;
                    attendanceStatusList.add(statusInfo);

                    if(name.contains("Late")){
                        status_late = String.valueOf(total);
                        if(debug)
                            Log.d("Tracking: attendanceStatus", statusInfo);
                    }

                    if(name.contains("OnTime")){
                        status_ontime = String.valueOf(total);
                        if(debug)
                            Log.d("Tracking: attendanceStatus", statusInfo);
                    }
                }

                JSONObject leaveAllowanceObject = firstResult.getJSONObject("leaveAllowance");
                status_leaveAssign = leaveAllowanceObject.getString("leaveAssign");
                status_leaveAvailable = leaveAllowanceObject.getString("available");


                runOnUiThread(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        String formattedDate = convertDateFormat(status_date, outputDateFormat);
                        String formattedTime = convertTimeFormat(status_time, outputTimeFormat);
                        _time_in_text.setText(formattedTime);
                        _date_text.setText(formattedDate);
                        _name_text.setText(firstName);
                        _ontime_text.setText(status_ontime);
                        _late_text.setText(status_late);

                        if(!status_leaveAvailable.equals("") && !status_leaveAssign.equals(""))
                            _leave_text.setText(status_leaveAvailable + "/" + status_leaveAssign);

//                        try {
//                            updateEarlyDate(formattedDate, formattedTime);
//                        } catch (ParseException e) {
//                            throw new RuntimeException(e);
//                        }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendPostRequest() {
        SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
//        String deviceId = sh.getString("DEVICEID", "");
//        String empId = sh.getString("EMPID", "");
        if(debug)
            Log.d("Tracking: ", convertedImage);
        PostRequestTask.PostRequestCallback callback = new PostRequestTask.PostRequestCallback() {
            @Override
            public void onSuccess(String result) {
//                Log.d("Tracking: ", "Success: " + result);
                post_button.setVisibility(View.VISIBLE);
                _progressBar1.setVisibility(View.GONE);
                get_pressed = true;
                ping();

                firstName = extractFullName(result);

                try {
                    JSONObject jsonResponse = new JSONObject(result);

                    if(jsonResponse.has("statusCode" )) {
                        int status_code = jsonResponse.getInt("statusCode");
                        if(debug)
                            Log.d("Tracking", "Status Code: " + status_code);

                        if (status_code == 200){
                            if (jsonResponse.has("result") && jsonResponse.getJSONArray("result").length() > 0) {
                                // Get the first item in the "result" array
                                JSONObject resultObject = jsonResponse.getJSONArray("result").getJSONObject(0);
                                JSONArray messagesArray = jsonResponse.getJSONArray("messages");
                                firstMessage = messagesArray.getString(0);
                                setPrompt(firstMessage);
                                // Extract the name from the resultObject
                                firstName = resultObject.optString("firstName", "");

                            }
                        } else if(status_code == 400){
                                // Get the first item in the "result" array
                                JSONArray messagesArray = jsonResponse.getJSONArray("messages");
                                firstMessage = messagesArray.getString(0);
                                setPrompt(firstMessage);
                        }
                    }
                } catch (JSONException e) {
                    if(debug)
                        Log.e("Tracking", "Error parsing JSON: " + e.getMessage());
                }

                Handler handler = new Handler();
                _updateRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if(debug)
                            Log.d("Handler: ", "run start");
//                        setPrompt("");
                        handler.postDelayed(this, 3000);
                        handler.removeCallbacks(_updateRunnable);
                        if(debug)
                            Log.d("Handler: ", "handler ended");
                    }
                };
                if(debug)
                    Log.d("Handler: ", "outside runnable");
                handler.postDelayed(_updateRunnable, 3000);

            }

            @Override
            public void onError() {
//                Log.d("Tracking: ", "Error");
                post_button.setVisibility(View.VISIBLE);
                _progressBar1.setVisibility(View.GONE);
                setPrompt("Error");
            }
        };

        new PostRequestTask(device_id, convertedImage, callback).execute();
    }

    private void openCalender() {
            Intent i = new Intent(this, Activity_Calender.class);
            startActivity(i);
            finish();
    }

    private void updateEarlyDate(String newDate, String newTime) throws ParseException {

        SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        String savedTime = sh.getString("time", "");
        String savedDate = sh.getString("date", "");
        _time_in_text.setText("Time in:\n" + savedTime);

        SharedPreferences.Editor editor = sh.edit();
        editor.putString("timetemp",newTime);
        editor.putString("datetemp",newDate);
        editor.apply();

        if(savedTime.equals("") && savedDate.equals("")){
            _time_in_text.setText("Time in:\n" + newTime);
            SharedPreferences.Editor editor2 = sh.edit();
            editor2.putString("time",newTime);
            editor2.putString("date",newDate);
            editor2.apply();
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
            Date savedDate2 = dateFormat.parse(savedDate);
            Date newDate2 = dateFormat.parse(newDate);
            Date savedTime2 = timeFormat.parse(savedTime);
            Date newTime2 = timeFormat.parse(newTime);
            if(debug){
                Log.d("Tracking: xxx","old Date: " + savedDate);
                Log.d("Tracking: xxx","old Time: " + savedTime);
                Log.d("Tracking: xxx","new Date: " + newDate);
                Log.d("Tracking: xxx","new Time: " + newTime);
            }

            if (savedDate2.before(newDate2)) {
                SharedPreferences.Editor editor2 = sh.edit();
                editor2.putString("time",newTime);
                editor2.putString("date",newDate);
                editor2.apply();
                _time_in_text.setText("Time in:\n" + newTime);
            }
        }

    }

    private void setPrompt(String prompt) {
        Snackbar snackbar
                = Snackbar
                .make(
                        LinearMain,
                        prompt,
                        Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private static String convertDateFormat(String inputDate, String outputFormat) {
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputDateFormat = new SimpleDateFormat(outputFormat, Locale.getDefault());

        try {
            Date date = inputDateFormat.parse(inputDate);
            return outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertTimeFormat(String inputTime, String outputFormat) {
        SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat sdf12 = new SimpleDateFormat(outputFormat, Locale.getDefault());

        try {
            Date date = sdf24.parse(inputTime);
            return sdf12.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void getLink() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(com.android.volley.Request.Method.GET, get_emp_info, null, new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray infoArray = response.getJSONArray("info");
                    if (infoArray.length() > 0) {
                        JSONObject infoObject = infoArray.getJSONObject(0); // Assuming there is only one object in the "info" array
                        emp_id = String.valueOf(infoObject.getInt("id"));
                        device_id = infoObject.getString("deviceid");
                        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("EMPID", emp_id);
                        editor.putString("DEVICEID", device_id);
                        editor.apply();
                        if(!emp_id.equals("") && !device_id.equals("")) {
                            ping();
                        }
                        //This will initiate on each episode click so have to fix it so that it can handle multiple clicks
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Tracking: ", error.getMessage());
            }
        });

        Volley.newRequestQueue(MainActivity.this).add(jsonObjectRequest);
    }

    private class PingTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String serverAddress = params[0];
            int timeout = 1200; // Timeout in milliseconds

            return PingUtils.isServerReachable(serverAddress, timeout);
        }

        @Override
        protected void onPostExecute(Boolean isReachable) {
            if (isReachable) {


                use_wan = false;
                get_success = false;
                com.blood.status.Request.use_wan_text = String.valueOf(false);
                hold_button = false;
//                get_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                post_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                if(start_func) {
                    start_func = false;
                    makeGetRequest(get_status_lan + emp_id);
                }
                if (get_pressed){
                    get_pressed = false;
                    makeGetRequest(get_status_lan + emp_id);
                }
                if(post_pressed){
                    post_pressed = false;
                    sendPostRequest();
                }
            } else {
                use_wan = true;
                com.blood.status.Request.use_wan_text = String.valueOf(true);
                hold_button = false;
//                get_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                post_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                if(start_func) {
                    start_func = false;
                    makeGetRequest(get_status_wan + emp_id);
                }
                if (get_pressed){
                    get_pressed = false;
                    makeGetRequest(get_status_wan + emp_id);
                }
                if(post_pressed){
                    post_pressed = false;
                    sendPostRequest();
                }
            }
        }
    }
}
