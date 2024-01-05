package com.blood.status;

import static com.blood.status.Request.device_id;
import static com.blood.status.Request.emp_id;
import static com.blood.status.Request.debug;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class Activity_Settings extends AppCompatActivity {
    private static final int pic_id = 123;
    String convertedImage;
    private ImageView _back_button;
    private TextView _emp_name_txt, _network_txt;
    private EditText _device_id_txt, _emp_id_txt;
    private MaterialButton _save_button, _image_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
    }

    @Override
    protected void onStart() {

        SharedPreferences sh = getSharedPreferences("SHARED_PREFS", Context.MODE_PRIVATE);
        String deviceid = sh.getString("DEVICEID", "");
        String empid = sh.getString("EMPID", "");

        _device_id_txt.setText(deviceid);
        _emp_id_txt.setText(empid);

        if(Request.use_wan_text.equals("false")) {
            _network_txt.setText("LAN");
        }
        else {
            _network_txt.setText("WAN");
        }

        String name = sh.getString("firstName","");
        _emp_name_txt.setText(name);

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
            SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("IMAGE",convertedImage);
            editor.apply();
            Snackbar snackbar
                    = Snackbar
                    .make(
                            _image_button,
                            "Image Saved",
                            Snackbar.LENGTH_LONG);
            snackbar.show();

            if(debug)
                Log.d("Tracking", "Image captured and saved.");
        }
    }

    protected void initView() {
        _back_button = findViewById(R.id.back_button);
        _emp_id_txt = findViewById(R.id.emp_id_txt);
        _emp_name_txt = findViewById(R.id.emp_name_txt);
        _device_id_txt = findViewById(R.id.device_id_txt);
        _network_txt = findViewById(R.id.network_txt);
        _save_button = findViewById(R.id.save_button);
        _image_button = findViewById(R.id.image_button);

        _image_button.setOnClickListener(v -> {
            Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera_intent, pic_id);
        });

        _save_button.setOnClickListener(v -> {
            String temp = String.valueOf(_device_id_txt.getText());
            String temp2 = String.valueOf(_emp_id_txt.getText());

            SharedPreferences sharedPreferences = getSharedPreferences("SHARED_PREFS", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("DEVICEID", temp);
            editor.putString("EMPID", temp2);
            editor.apply();
        });

        _device_id_txt.addTextChangedListener(new TextWatcher() {
            String temp1 = "";
            String temp2 = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                _save_button.setBackgroundColor(ContextCompat.getColor(Activity_Settings.this, R.color.main_color));
            }
        });

        _back_button.setOnClickListener(v -> {
            finish();
        });

    }
}