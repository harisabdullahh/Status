package com.blood.status;

import static com.blood.status.Request.device_id;
import static com.blood.status.Request.emp_id;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class Activity_Settings extends AppCompatActivity {

    private ImageView _back_button;
    private TextView _device_id_txt, _emp_id_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initView();
    }

    @Override
    protected void onStart() {
        _device_id_txt.setText(device_id);
        _emp_id_txt.setText(emp_id);
        super.onStart();
    }

    protected void initView() {
        _back_button = findViewById(R.id.back_button);
        _emp_id_txt = findViewById(R.id.emp_id_txt);
        _device_id_txt = findViewById(R.id.device_id_txt);

        _back_button.setOnClickListener(v -> {
            finish();
//            Intent i = new Intent(this, MainActivity.class);
//            startActivity(i);
        });

    }
}