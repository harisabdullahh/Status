package com.blood.status;

import static com.blood.status.Request.device_id;
import static com.blood.status.Request.emp_id;
import static com.blood.status.Request.get_status_lan;
import static com.blood.status.Request.get_status_wan;
import static com.blood.status.Request.outputDateFormat;
import static com.blood.status.Request.outputTimeFormat;
import static com.blood.status.Request.part1;
import static com.blood.status.Request.part2;
import static com.blood.status.Request.part3;
import static com.blood.status.Request.part4;
import static com.blood.status.Request.part5;
import static com.blood.status.Request.post_mark_attendance_lan;
import static com.blood.status.Request.post_mark_attendance_wan;
import static com.blood.status.Request.status_date;
import static com.blood.status.Request.status_time;
import static com.blood.status.Request.status_result;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private boolean post_success = false;
    private boolean get_success = false;
    private boolean use_wan = false;
    private boolean post_pressed = false;
    private boolean get_pressed = false;
    private boolean hold_button = false;
    private boolean start_func = false;
    MaterialButton get_button, post_button;
    private MaterialButton _lan_button, _wan_button;
    private TextView _date_text, _time_text, _post_text;
    private CardView _card_prompt;
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
        ping();
        hold_button = true;
        get_button.setBackgroundColor(ContextCompat.getColor(this, androidx.cardview.R.color.cardview_dark_background));
        post_button.setBackgroundColor(ContextCompat.getColor(this, androidx.cardview.R.color.cardview_dark_background));
//        makeGetRequest(get_status_wan);
        if(get_success){
            String formattedDate = convertDateFormat(status_date, outputDateFormat);
            String formattedTime = convertTimeFormat(status_time, outputTimeFormat);
            _time_text.setText(formattedTime);
            _date_text.setText(formattedDate);
        }
        super.onStart();
    }

    private void ping() {
        new PingTask().execute("172.16.100.111");
    }

    protected void initView() {

        //Initializtion
        get_button = findViewById(R.id.get_button);
        post_button = findViewById(R.id.post_button);
        _date_text = findViewById(R.id.date_text);
        _time_text = findViewById(R.id.time_text);
        _post_text = findViewById(R.id.post_text);
        _settings_button = findViewById(R.id.settings_button);
        _progressBar1 = findViewById(R.id.progressBar1);
        _progressBar2 = findViewById(R.id.progressBar2);
        _card_prompt = findViewById(R.id.card_prompt);



        //Action
        _progressBar1.setVisibility(View.GONE);
        _progressBar2.setVisibility(View.GONE);
        _card_prompt.setVisibility(View.GONE);


        //Listeners
        get_button.setOnClickListener(v -> {
            if(!hold_button){
                get_button.setVisibility(View.GONE);
                _progressBar2.setVisibility(View.VISIBLE);
                get_pressed = true;
                ping();
//                if(use_wan){
//                    makeGetRequest(get_status_wan);
//                } else {
//                    makeGetRequest(get_status_lan);
//                }
            }
        });

        post_button.setOnClickListener(v -> {
            if(!hold_button){
                post_button.setVisibility(View.GONE);
                _progressBar1.setVisibility(View.VISIBLE);
                post_pressed = true;
                ping();
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
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                    // Handle the failure
                }
            });
        }

        // Method to handle the JSON response
        private void handleJSONResponse(String json) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                JSONArray resultArray = jsonObject.getJSONArray("result");

                if (resultArray.length() > 0) {
                    JSONObject firstResult = resultArray.getJSONObject(0);

                    String lastAttendance = firstResult.getString("lastAttendance");
                    Log.d("Tracking: ", lastAttendance);
                    status_result = lastAttendance;
                    int indexOfT = lastAttendance.indexOf('T');
                    int lengthOfT = lastAttendance.length();
                    status_date = lastAttendance.substring(0, indexOfT);
                    status_time = lastAttendance.substring(indexOfT+1, indexOfT+6);

                    // Update UI or perform actions with the lastAttendance data
                    runOnUiThread(() -> {
                        _progressBar2.setVisibility(View.GONE);
                        get_button.setVisibility(View.VISIBLE);
                        String formattedDate = convertDateFormat(status_date, outputDateFormat);
                        String formattedTime = convertTimeFormat(status_time, outputTimeFormat);
                        _time_text.setText(formattedTime);
                        _date_text.setText(formattedDate);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    private void sendPostRequest() {
        String json = "{\n\"deviceId\": \"" + device_id + "\",\n\"image_data\": \"" + part1 + part2 + part3 + part4 + part5 + "\"\n}";
        Log.d("Tracking: ", json);
        
//        post_pressed = false;

        // Create a request body with the JSON data
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);

        // Create the POST request
        Request request = null;
        if(use_wan){
            request = new Request.Builder()
                    .url(post_mark_attendance_wan + emp_id)
                    .post(requestBody)
                    .build();
        } else if(!use_wan) {
            request = new Request.Builder()
                    .url(post_mark_attendance_lan + emp_id)
                    .post(requestBody)
                    .build();
        }

        // Execute the request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
//                        _post_text.setText("Response Sent");
                        ResponseBody responseBody = response.body();
                        if (responseBody != null) {
                            String postResponse = responseBody.string();
                            Log.d("Tracking: ", postResponse);
                            JSONObject jsonResponse = new JSONObject(postResponse);
                            boolean isSuccess = jsonResponse.optBoolean("isSuccess", false);
                            runOnUiThread(() -> {
                                if(isSuccess) {
                                    setPrompt("Attendance Marked");
                                    if(use_wan){
                                        makeGetRequest(get_status_wan);
                                    } else if(!use_wan) {
                                        makeGetRequest(get_status_lan);
                                    }
                                }
                                else {
                                    setPrompt("Attendance Not Marked");
                                }
                                post_button.setVisibility(View.VISIBLE);
                                _progressBar1.setVisibility(View.GONE);
                                handler = new Handler();
                                Log.d("Handler: ", "handler created");
                                _updateRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("Handler: ", "run start");
                                        setPrompt("");
                                        handler.postDelayed(this, 3000);
                                        handler.removeCallbacks(_updateRunnable);
                                        Log.d("Handler: ", "handler ended");
                                    }
                                };
                                Log.d("Handler: ", "outside runnable");
                                handler.postDelayed(_updateRunnable, 3000);
                                makeGetRequest(get_status_wan);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    setPrompt("Error");
                    post_button.setVisibility(View.VISIBLE);
                    _progressBar1.setVisibility(View.GONE);

                    handler = new Handler();
                    Log.d("Handler: ", "handler created");
                    _updateRunnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d("Handler: ", "run start");
                            setPrompt("");
                            handler.postDelayed(this, 3000);
                            handler.removeCallbacks(_updateRunnable);
                            Log.d("Handler: ", "handler ended");
                        }
                    };
                    Log.d("Handler: ", "outside runnable");
                    handler.postDelayed(_updateRunnable, 3000);

                });
            }
        });
    }

    private void setPrompt(String prompt) {
            if(prompt.equals("")){
                _post_text.setText("");
                _card_prompt.setVisibility(View.GONE);
            } else {
                _post_text.setText(prompt);
                _card_prompt.setVisibility(View.VISIBLE);
            }
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

    private class PingTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            String serverAddress = params[0];
            int timeout = 5000; // Timeout in milliseconds

            return PingUtils.isServerReachable(serverAddress, timeout);
        }

        @Override
        protected void onPostExecute(Boolean isReachable) {
            if (isReachable) {
//                Toast.makeText(MainActivity.this, "Server is reachable", Toast.LENGTH_SHORT).show();
                use_wan = false;
                hold_button = false;
                get_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                post_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                if(start_func) {
                    start_func = false;
                    makeGetRequest(get_status_lan);
                }
                if (get_pressed){
                    get_pressed = false;
                    makeGetRequest(get_status_lan);
                }
                if(post_pressed){
                    post_pressed = false;
                    sendPostRequest();
                }
            } else {
//                Toast.makeText(MainActivity.this, "Server is not reachable", Toast.LENGTH_SHORT).show();
                use_wan = true;
                hold_button = false;
                get_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                post_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
                if(start_func) {
                    start_func = false;
                    makeGetRequest(get_status_wan);
                }
                if (get_pressed){
                    get_pressed = false;
                    makeGetRequest(get_status_wan);
                }
                if(post_pressed){
                    post_pressed = false;
                    sendPostRequest();
                }

            }
        }
    }
}
