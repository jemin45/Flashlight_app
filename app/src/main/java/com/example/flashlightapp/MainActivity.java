package com.example.flashlightapp;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ImageButton btnToggle;
    private TextView statusText;
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;
    private boolean isStrobeActive = false;
    private int strobeDelay = 0;
    private Handler handler = new Handler();
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnToggle = findViewById(R.id.btnToggle);
        statusText = findViewById(R.id.statusText);
        Button btnSOS = findViewById(R.id.btnSOS);
        Button btnStrobeSlow = findViewById(R.id.btnStrobeSlow);
        Button btnStrobeFast = findViewById(R.id.btnStrobeFast);
        Button btn15s = findViewById(R.id.btn15s);
        Button btn20s = findViewById(R.id.btn20s);
        Button btn30s = findViewById(R.id.btn30s);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) { e.printStackTrace(); }

        btnToggle.setOnClickListener(v -> {
            if (isStrobeActive || isFlashOn) {
                stopEverything();
            } else {
                toggleFlashlight();
            }
        });

        btnStrobeSlow.setOnClickListener(v -> startStrobe(500));
        btnStrobeFast.setOnClickListener(v -> startStrobe(100));
        btnSOS.setOnClickListener(v -> startStrobe(300));

        btn15s.setOnClickListener(v -> setTimer(15000));
        btn20s.setOnClickListener(v -> setTimer(20000));
        btn30s.setOnClickListener(v -> setTimer(30000));
    }

    private void toggleFlashlight() {
        try {
            isFlashOn = !isFlashOn;
            cameraManager.setTorchMode(cameraId, isFlashOn);
            statusText.setText(isFlashOn ? "ON" : "OFF");
            if (vibrator != null) vibrator.vibrate(50);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void startStrobe(int delay) {
        stopEverything();
        isStrobeActive = true;
        strobeDelay = delay;
        handler.post(strobeRunnable);
    }

    private void setTimer(int milliseconds) {
        stopEverything();
        if (!isFlashOn) toggleFlashlight();
        handler.postDelayed(() -> {
            if (isFlashOn || isStrobeActive) stopEverything();
        }, milliseconds);
    }

    private void stopEverything() {
        isStrobeActive = false;
        isFlashOn = false;
        handler.removeCallbacksAndMessages(null);
        try {
            cameraManager.setTorchMode(cameraId, false);
            statusText.setText("OFF");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private Runnable strobeRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isStrobeActive) return;
            try {
                isFlashOn = !isFlashOn;
                cameraManager.setTorchMode(cameraId, isFlashOn);
                statusText.setText(isFlashOn ? "STROBE ON" : "OFF");
                handler.postDelayed(this, strobeDelay);
            } catch (Exception e) { e.printStackTrace(); }
        }
    };
}