package com.polar.polarsdkecghrdemo;


import static com.polar.polarsdkecghrdemo.helpers.Person.isPhoneNumber;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.polar.polarsdkecghrdemo.helpers.DetectionResult;
import com.polar.polarsdkecghrdemo.helpers.YUVtoRGB;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class QRScanActivity extends AppCompatActivity {

    private final String TAG = "QRScanner";
    private final int PERMISSION_CODE = 10;
    private final String[] PERMISSION = new String[]{Manifest.permission.CAMERA};
    private ImageView preview;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private @SuppressLint("RestrictedApi") ImageAnalysis imageAnalysis;
    private CameraSelector cameraSelector;

    private final YUVtoRGB translator = new YUVtoRGB();

    private BarcodeScanner barcodeDetector;
    DetectionResult detectionResult = new DetectionResult();


    public void onReturn(View view) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("ID", detectionResult.getBarcodeMessage());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean allPermissionGranted() {
        return Arrays.stream(PERMISSION)
                .allMatch(permission -> ContextCompat.checkSelfPermission(getBaseContext(), permission)
                        == PackageManager.PERMISSION_GRANTED);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qrscan);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        BarcodeScannerOptions bOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeDetector = BarcodeScanning.getClient(bOptions);

        preview = findViewById(R.id.preview);

        imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(4600, 4600))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        if (allPermissionGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSION,
                    PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            if (allPermissionGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "PermissionError", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(QRScanActivity.this), image -> {
                    @OptIn(markerClass = ExperimentalGetImage.class) Image img = image.getImage();
                    Bitmap bitmap = translator.translateYUV(img, QRScanActivity.this);
                    InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

                    barcodeDetector.process(inputImage)
                            .addOnSuccessListener(barcodes -> barcodes.stream()
                                    .findFirst()
                                    .ifPresent(barcode -> {
                                        String s = barcode.getRawValue();
                                        if (isPhoneNumber(s)) {
                                            detectionResult.setBarcodeMessage(s);
                                            onReturn(preview);
                                        }
                                    })).addOnFailureListener(e -> Log.e(TAG,
                                    "Error processing Image", e));

                    preview.setRotation(image.getImageInfo().getRotationDegrees());
                    preview.setImageBitmap(bitmap);
                    image.close();
                });

                cameraProvider.bindToLifecycle(QRScanActivity.this, cameraSelector, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                Log.e(TAG, "Bind Error", e);
            }
        }, ContextCompat.getMainExecutor((this)));
    }
}