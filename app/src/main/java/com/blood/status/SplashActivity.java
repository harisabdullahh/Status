package com.blood.status;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static com.blood.status.Request.debug;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class SplashActivity extends AppCompatActivity {
    private static final int pic_id = 123;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    Runnable _updateRunnable;
    String convertedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        checkAndRequestCameraPermission();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {
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

            if(debug)
                Log.d("Tracking", "Image captured and saved.");
        }
    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(this, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED;
    }

    // Request camera permission
    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.CAMERA"}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            // Check if the camera permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now use the camera
                // Call the method to initialize the camera or perform camera-related tasks
                initializeCamera();
            } else {
                // Permission denied, handle accordingly (show a message, disable camera features, etc.)
                Toast.makeText(this, "Camera permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkAndRequestCameraPermission() {
        if (checkCameraPermission()) {
            // Camera permission is already granted, you can now use the camera
            initializeCamera();
        } else {
            // Camera permission is not granted, request it
            requestCameraPermission();
        }
    }

    private void initializeCamera() {
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
                    startActivityForResult(camera_intent, pic_id);

                    SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("FIRSTTIME", "true");
                    editor.apply();
                } else {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
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

}