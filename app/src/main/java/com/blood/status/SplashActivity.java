package com.blood.status;

import androidx.appcompat.app.AppCompatActivity;
import static com.blood.status.Request.debug;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import java.io.ByteArrayOutputStream;

public class SplashActivity extends AppCompatActivity {

    private static final int pic_id = 123;
    Runnable _updateRunnable;
    String convertedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);





        Handler handler = new Handler();
        _updateRunnable= new Runnable() {
            @Override
            public void run() {


                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                boolean previouslyStarted = prefs.getBoolean("firstTime", false);
                if(!previouslyStarted) {
                    SharedPreferences.Editor edit = prefs.edit();
                    edit.putBoolean("firstTime", Boolean.TRUE);
                    edit.apply();

                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Start the activity with camera_intent, and request pic id
                    startActivityForResult(camera_intent, pic_id);

                    SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("FIRSTTIME", "true");
                    editor.apply();
//                    showHelp();
                } else {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }

//                SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
//                firstTime = sh.getString("FIRSTTIME", "false");

//                if(firstTime.equals("true")){
//                    Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    // Start the activity with camera_intent, and request pic id
//                    startActivityForResult(camera_intent, pic_id);
//
//                    SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("FIRSTTIME", "true");
//                    editor.apply();
//                } else {
//                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
//                    startActivity(i);
//                    finish();
//                }

                if(debug)
                    Log.d("Handler: ", "run start");
                handler.postDelayed(this, 1500);
                handler.removeCallbacks(_updateRunnable);
                if(debug)
                    Log.d("Handler: ", "handler ended");
            }
        };
        if(debug)
            Log.d("Handler: ", "outside runnable");
        handler.postDelayed(_updateRunnable, 1500);

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
            SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("IMAGE",convertedImage);
            editor.apply();
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);
            finish();
//            Log.d("Tracking: ", String.valueOf(convertedImage));
            if(debug)
                Log.d("Tracking", "Image captured and saved.");
//            post_button.setVisibility(View.GONE);
//            _progressBar1.setVisibility(View.VISIBLE);
//            sendPostRequest();
            // Set the image in imageview for display
//            click_image_id.setImageBitmap(photo);
        }
    }

}