package com.thesis.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.pedro.library.view.OpenGlView;
import com.pedro.rtspserver.RtspServerCamera2;
import com.pedro.common.ConnectChecker;
import com.thesis.myapplication.utils.NetworkUtils;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ConnectChecker {

    private RtspServerCamera2 rtspCamera1;
    private OpenGlView openGlView;
    private TextView ipTextView;
    private Button startStopButton;
    private final Handler handler = new Handler(Objects.requireNonNull(Looper.myLooper()));
    private boolean isStreaming = false;
    private boolean isConnected = false;
    private static final int REQUEST_PERMISSIONS = 1;
    private String labelText;
    private String buttonText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.ACCESS_WIFI_STATE
            }, REQUEST_PERMISSIONS);
        } else {
            init();
        }
    }

    private void init() {
        openGlView = findViewById(R.id.surfaceView);
        ipTextView = findViewById(R.id.ipTextView);
        startStopButton = findViewById(R.id.startStopButton);

        rtspCamera1 = new RtspServerCamera2(openGlView, this, 1935);
        this.labelText = "Not Streaming";
        ipTextView.setText(this.labelText);
        startStopButton.setOnClickListener(view -> handleStartStop());
    }

    private void handleStartStop() {
        if (!isStreaming) {
            if (prepareStreamWithScaling()) {
//                rtspCamera1.getStreamClient().setAuthorization("test","test");
                rtspCamera1.startStream();
                this.labelText = "Stream URL: rtsp://" + NetworkUtils.getDeviceIpAddress(this) + ":1935";
                ipTextView.setText(this.labelText);
                isStreaming = true;
                this.buttonText="Stop Streaming";
                startStopButton.setText(this.buttonText);
                startConnectionCheck();
            } else {
                Toast.makeText(this, "Error preparing stream", Toast.LENGTH_SHORT).show();
            }
        } else {
            stopStreaming();
        }
    }

    private boolean prepareStreamWithScaling() {
        int videoWidth = 640;
        int videoHeight = 640;
        return rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo(videoWidth, videoHeight, 2500000);
    }

    private void stopStreaming() {
        rtspCamera1.stopStream();
        isStreaming = false;
        handler.removeCallbacksAndMessages(null);
        openGlView.stop();
        this.buttonText="Start Streaming";
        startStopButton.setText(this.buttonText);
        this.labelText = "Not Streaming";
        ipTextView.setText(this.labelText);
    }

    private void startConnectionCheck() {
        handler.postDelayed(() -> {
            if (!isConnected && isStreaming) {
                stopStreaming();
                Toast.makeText(MainActivity.this, "No clients connected. Stopping stream.", Toast.LENGTH_SHORT).show();
            }
            handler.postDelayed(this::startConnectionCheck, 500000);
        }, 500000);
    }

    @Override
    public void onConnectionStarted(@NonNull String rtspUrl) {
        isConnected = true;
    }

    @Override
    public void onConnectionSuccess() {
        isStreaming = true;
        Toast.makeText(this, "RTSP Connection successful", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull String reason) {
        isStreaming = false;
        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDisconnect() {
//        stopStreaming();
        isConnected = false;
    }

    @Override
    public void onAuthError() {
        isConnected = false;
        Toast.makeText(this, "RTSP Authentication failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAuthSuccess() {
        isConnected = true;
        Toast.makeText(this, "RTSP Authentication successful", Toast.LENGTH_SHORT).show();
    }

    private boolean hasPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                init();
            } else {
                Toast.makeText(this, "Permissions are required to use this app", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
