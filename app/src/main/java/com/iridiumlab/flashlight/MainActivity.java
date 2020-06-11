package com.example.flashlight;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button buttonPermission;
    private Button buttonPower;
    private Button buttonPrivacy;
    private AdView adView;
    private Boolean isShowingAds;

    private Boolean flashLightStatus = false;
    private final int CAMERA_REQUEST = 1;

    private void flashLightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = Objects.requireNonNull(cameraManager).getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, true);
            flashLightStatus = true;
        } catch (CameraAccessException ignored) {
        }
    }

    private void flashLightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = Objects.requireNonNull(cameraManager).getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            flashLightStatus = false;
        } catch (CameraAccessException ignored) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT < 29) {
            // set status bar color
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#000000"));
        }

        buttonPower = findViewById(R.id.buttonPower);
        buttonPermission = findViewById(R.id.buttonPermission);
        buttonPrivacy = findViewById(R.id.buttonPrivacy);
        adView = findViewById(R.id.adView);

        buttonPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://github.com/example/flashlight/blob/master/privacy_policy.md");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        final boolean hasCameraFlash = getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        boolean isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        buttonPower.setEnabled(isEnabled);
        buttonPermission.setEnabled(false);

        if (!buttonPower.isEnabled()) {
            buttonPower.setBackgroundResource(R.drawable.shape_disabled);
        }

        if (!buttonPermission.isEnabled()) {
            buttonPermission.setVisibility(View.INVISIBLE);
        }

        ActivityCompat.requestPermissions(MainActivity.this,
                new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);

        buttonPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST);
            }
        });

        isShowingAds = true;

        buttonPower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isShowingAds = !isShowingAds;
                if (isShowingAds) {
                    adView.setVisibility(View.VISIBLE);
                } else {
                    adView.setVisibility(View.INVISIBLE);
                }

                if (hasCameraFlash) {
                    if (flashLightStatus) {
                        flashLightOff();
                        buttonPower.setBackgroundResource(R.drawable.shape_off);
                    }
                    else {
                        flashLightOn();
                        buttonPower.setBackgroundResource(R.drawable.shape_on);
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_flash_detected),
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // google ads
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                buttonPermission.setEnabled(false);
                buttonPermission.setVisibility(View.INVISIBLE);

                buttonPower.setEnabled(true);
                buttonPower.setBackgroundResource(R.drawable.shape_off);
            } else {
                buttonPermission.setEnabled(true);
                buttonPermission.setVisibility(View.VISIBLE);

                Toast.makeText(MainActivity.this, getString(R.string.camera_access_required),
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}