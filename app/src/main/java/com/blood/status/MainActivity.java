package com.blood.status;

import static com.blood.status.Request.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    private MaterialButton _lan_button, _wan_button;
    private TextView _date_text, _time_text, _post_text, _network_text, _time_in_text, _name_text;
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
        ping();
    }

    @Override
    protected void onStart() {

//        hold_button = true;
//        get_button.setBackgroundColor(ContextCompat.getColor(this, androidx.cardview.R.color.cardview_dark_background));
//        post_button.setBackgroundColor(ContextCompat.getColor(this, androidx.cardview.R.color.cardview_dark_background));
//        makeGetRequest(get_status_wan);
        if(get_success){
            String formattedDate = convertDateFormat(status_date, outputDateFormat);
            String formattedTime = convertTimeFormat(status_time, outputTimeFormat);
            _time_text.setText(formattedTime);
            _date_text.setText(formattedDate);
            _name_text.setText(firstName);
        }

        SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        device_id = sh.getString("DEVICEID", "");
        emp_id = sh.getString("EMPID", "");

        super.onStart();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Match the request 'pic id with requestCode
        if (requestCode == pic_id) {
            // BitMap is data structure of image file which store the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            convertedImage = new String(Base64.encodeToString(byteArray, Base64.DEFAULT));
//            Log.d("Tracking: ", String.valueOf(convertedImage));
            if(debug)
                Log.d("Tracking", "Image captured and saved.");
            post_button.setVisibility(View.GONE);
            _progressBar1.setVisibility(View.VISIBLE);
            sendPostRequest();
            // Set the image in imageview for display
//            click_image_id.setImageBitmap(photo);
        }
    }

    private void ping() {
        new PingTask().execute("172.16.100.111");
    }

    protected void initView() {

        //Initializtion
        LinearMain = findViewById(R.id.LinearMain);
        get_button = findViewById(R.id.get_button);
        post_button = findViewById(R.id.post_button);
        _date_text = findViewById(R.id.date_text);
        _time_text = findViewById(R.id.time_text);
        _post_text = findViewById(R.id.post_text);
        _name_text = findViewById(R.id.name_text);
        _settings_button = findViewById(R.id.settings_button);
        _progressBar1 = findViewById(R.id.progressBar1);
        _progressBar2 = findViewById(R.id.progressBar2);
        _card_prompt = findViewById(R.id.card_prompt);
        _time_in_text = findViewById(R.id.time_in_text);



        //Action
        _progressBar1.setVisibility(View.GONE);
        _progressBar2.setVisibility(View.GONE);
        _card_prompt.setVisibility(View.GONE);


        //Listeners
        get_button.setOnClickListener(v -> {
            if(!emp_id.equals("") && !device_id.equals("")){
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
            }
        });

        post_button.setOnClickListener(v -> {

//            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            // Start the activity with camera_intent, and request pic id
//            startActivityForResult(camera_intent, pic_id);

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
                    // Handle the failure
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
                    // Handle the failure
                }
            });
        }


    public static String extractFullName(String jsonResponse) {
        try {
            JSONObject responseObj = new JSONObject(jsonResponse);

            // Check if the "result" array exists and has at least one element
            if (responseObj.has("result") && !responseObj.isNull("result")) {
                if (responseObj.get("result") instanceof JSONArray) {
                    JSONArray resultArray = responseObj.getJSONArray("result");

                    // Check if the array has at least one employee
                    if (resultArray.length() > 0) {
                        JSONObject employeeObj = resultArray.getJSONObject(0);

                        // Extract first and last names
                        String firstName = employeeObj.getString("firstName");
                        String lastName = employeeObj.getString("lastName");

                        // Concatenate first and last names
                        return firstName + " " + lastName;
                    }
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null; // Return null if any error occurs
    }

        // Method to handle the JSON response
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
                    if(debug)
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
                        _name_text.setText(firstName);
                        try {
                            updateEarlyDate(formattedDate, formattedTime);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    private void sendPostRequest() {
        SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        String deviceId = sh.getString("DEVICEID", "");
        String empId = sh.getString("EMPID", "");
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

        new PostRequestTask(deviceId, convertedImage, callback).execute();
    }






//    private void sendPostRequest() {
//
//        String json = "";
//        json = "{\n\"deviceId\": \"" + deviimageDatace_id + "\",\n\"image_data\": \"" + part1 + part2 + part3 + part4 + part5 + part6 + part7 + part8 + part9 + part10 + part11 + part12 + part13 + part14 + part15 + part16 + part17 + part18 + part19 + part20 + part21 + part22 + part23 + part24 + "\"\n}";
//        Log.d("Tracking: ", json);
//
//        post_pressed = false;
//
//        // Create a request body with the JSON data
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
//
//        // Create the POST request
//        Request request = null;
//        if(use_wan){
//            request = new Request.Builder()
//                    .url(post_mark_attendance_wan + emp_id)
//                    .post(requestBody)
//                    .build();
//        } else if(!use_wan) {
//            request = new Request.Builder()
//                    .url(post_mark_attendance_lan + emp_id)
//                    .post(requestBody)
//                    .build();
//        }
//
//        Log.d("Tracking: ", String.valueOf(requestBody));
//
//        // Execute the request
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onResponse(Call call, Response response) {
//                if (response.isSuccessful()) {
//                    try {
////                        _post_text.setText("Response Sent");
//                        ResponseBody responseBody = response.body();
//                        if (responseBody != null) {
//                            String postResponse = responseBody.string();
//                            Log.d("Tracking: ", postResponse);
//                            JSONObject jsonResponse = new JSONObject(postResponse);
//                            boolean isSuccess = jsonResponse.optBoolean("isSuccess", false);
//                            runOnUiThread(() -> {
//                                if(isSuccess) {
//                                    setPrompt("Attendance Marked");
//                                    if(use_wan){
//                                        makeGetRequest(get_status_wan);
//                                    } else if(!use_wan) {
//                                        makeGetRequest(get_status_lan);
//                                    }
//                                }
//                                else {
//                                    setPrompt("Attendance Not Marked");
//                                }
//                                post_button.setVisibility(View.VISIBLE);
//                                _progressBar1.setVisibility(View.GONE);
//                                handler = new Handler();
//                                Log.d("Handler: ", "handler created");
//                                _updateRunnable = new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        Log.d("Handler: ", "run start");
//                                        setPrompt("");
//                                        handler.postDelayed(this, 3000);
//                                        handler.removeCallbacks(_updateRunnable);
//                                        Log.d("Handler: ", "handler ended");
//                                    }
//                                };
//                                Log.d("Handler: ", "outside runnable");
//                                handler.postDelayed(_updateRunnable, 3000);
//                                makeGetRequest(get_status_wan);
//                            });
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//                runOnUiThread(() -> {
//                    setPrompt("Error");
//                    post_button.setVisibility(View.VISIBLE);
//                    _progressBar1.setVisibility(View.GONE);
//
//                    handler = new Handler();
//                    Log.d("Handler: ", "handler created");
//                    _updateRunnable = new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.d("Handler: ", "run start");
//                            setPrompt("");
//                            handler.postDelayed(this, 3000);
//                            handler.removeCallbacks(_updateRunnable);
//                            Log.d("Handler: ", "handler ended");
//                        }
//                    };
//                    Log.d("Handler: ", "outside runnable");
//                    handler.postDelayed(_updateRunnable, 3000);
//
//                });
//            }
//        });
//    }

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
//            if(prompt.equals("")){
//                _post_text.setText("");
//                _card_prompt.setVisibility(View.GONE);
//            } else {
//                _post_text.setText(prompt);
//                _card_prompt.setVisibility(View.VISIBLE);
//            }
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
            int timeout = 1200; // Timeout in milliseconds

            return PingUtils.isServerReachable(serverAddress, timeout);
        }

        @Override
        protected void onPostExecute(Boolean isReachable) {
            if (isReachable) {
//                Toast.makeText(MainActivity.this, "Server is reachable", Toast.LENGTH_SHORT).show();
                use_wan = false;
                get_success = false;
                com.blood.status.Request.use_wan_text = String.valueOf(false);
                hold_button = false;
                get_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
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
//                Toast.makeText(MainActivity.this, "Server is not reachable", Toast.LENGTH_SHORT).show();
                use_wan = true;
                com.blood.status.Request.use_wan_text = String.valueOf(true);
                hold_button = false;
                get_button.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.main_color));
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
