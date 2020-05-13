package com.himanshu.videocall;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.widget.Toast;

import com.himanshu.videocall.databinding.ActivityMainBinding;
import com.himanshu.videocallsdkvendors.constants.VideoSdkVendors;
import com.himanshu.videocallsdkvendors.helper.VideoCallSdkHelper;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.tvStartVideoCall.setOnClickListener(v -> {
            if (binding.etAuthToken.getText() != null && !binding.etAuthToken.getText().toString().isEmpty()) {
                VideoCallSdkHelper.getInstance(this)
                        .setVendor(VideoSdkVendors.TWILIO)
                        .setAuthToken(binding.etAuthToken.getText().toString())
                        .init();
            } else {
                Toast.makeText(this, "Auth Token required", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
