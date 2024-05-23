package com.polar.polarsdkecghrdemo;

import static com.polar.polarsdkecghrdemo.helpers.Person.isPhoneNumber;
import static com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.view.WindowManager;
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

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.polar.polarsdkecghrdemo.helpers.DetectionResult;
import com.polar.polarsdkecghrdemo.helpers.DrawDetection;
import com.polar.polarsdkecghrdemo.helpers.YUVtoRGB;

import com.androidplot.xy.XYPlot;
import com.polar.polarsdkecghrdemo.helpers.Person;
import com.polar.sdk.api.PolarBleApi;
import com.polar.sdk.api.PolarBleApiCallback;
//import com.polar.sdk.api.PolarBleApiDefaultImpl.defaultImplementation;
import com.polar.sdk.api.errors.PolarInvalidArgument;
import com.polar.sdk.api.model.PolarDeviceInfo;
import com.polar.sdk.api.model.PolarHrData;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;

import java.sql.Time;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class IRActivity extends AppCompatActivity implements PlotterListener {
    private final String TAG = "CameraX";
    private final int PERMISSION_CODE = 10;
    private final String[] PERMISSION = new String[]{Manifest.permission.CAMERA};
    private ImageView preview;
    private ImageView preview2;

    private Person p = null;
    public PolarBleApi api;
    private XYPlot plot;
    //private HrAndRrPlotter plotter;
    private Disposable hrDisposable;

    private String deviceId;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private @SuppressLint("RestrictedApi") ImageAnalysis imageAnalysis;
    private CameraSelector cameraSelector;

    private final YUVtoRGB translator = new YUVtoRGB();

    private BarcodeScanner barcodeDetector;
    private FaceDetector faceDetector;
    DetectionResult detectionResult = new DetectionResult();

    private  final int UPDATE_RATE = 15;

    public void onReturn(View view) {
        finish();
    }

    private boolean allPermissionGranted() {
        return Arrays.stream(PERMISSION)
                .allMatch(permission -> ContextCompat.checkSelfPermission(getBaseContext(), permission)
                        == PackageManager.PERMISSION_GRANTED);
    }

    public int second;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        Timer t = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                second++;
            }
        };
        t.scheduleAtFixedRate(task,1000, 1000);
        setContentView(R.layout.activity_iractivity);
        deviceId = "B39A1022";
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN);

        BarcodeScannerOptions bOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build();
        barcodeDetector = BarcodeScanning.getClient(bOptions);

        FaceDetectorOptions fOptions = new FaceDetectorOptions.Builder()
                .enableTracking()
                .build();
        faceDetector = FaceDetection.getClient(fOptions);

        preview = findViewById(R.id.preview);
        preview2 = findViewById(R.id.preview2);

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


        api = defaultImplementation(
                getApplicationContext(),
                new HashSet<>(Arrays.asList(
                        PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                        PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                        PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
                ))
        );
        api.setApiLogger(str -> Log.d("SDK", str));
        api.setApiCallback(new PolarBleApiCallback() {
            @Override
            public void blePowerStateChanged(boolean powered) {
                Log.d(TAG, "BluetoothStateChanged " + powered);
            }

            @Override
            public void deviceConnected(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG, "Device connected " + polarDeviceInfo.getDeviceId());
                Toast.makeText(getApplicationContext(), R.string.connected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceConnecting(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG, "Device connecting " + polarDeviceInfo.getDeviceId());
            }

            @Override
            public void deviceDisconnected(PolarDeviceInfo polarDeviceInfo) {
                Log.d(TAG, "Device disconnected " + polarDeviceInfo.getDeviceId());
            }

            @Override
            public void bleSdkFeatureReady(String identifier, PolarBleApi.PolarBleSdkFeature feature) {
                Log.d(TAG, "feature ready " + feature);

                switch (feature) {
                    case FEATURE_POLAR_ONLINE_STREAMING:
                        streamHR();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void disInformationReceived(String identifier, UUID uuid, String value) {
                if (uuid.equals(UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"))) {
                    String msg = "Firmware: " + value.trim();
                    Log.d(TAG, "Firmware: " + identifier + " " + value.trim());
                }
            }

            @Override
            public void batteryLevelReceived(String identifier, int level) {
                Log.d(TAG, "Battery level " + identifier + " " + level + "%");
                String batteryLevelText = "Battery level: " + level + "%";
            }

            @Override
            public void hrNotificationReceived(String identifier, PolarHrData.PolarHrSample data) {
                // deprecated
            }

            @Override
            public void polarFtpFeatureReady(String identifier) {
                // deprecated
            }

            @Override
            public void streamingFeaturesReady(String identifier, Set<? extends PolarBleApi.PolarDeviceDataType> features) {
                // deprecated
            }

            @Override
            public void hrFeatureReady(String identifier) {
                // deprecated
            }
        });

        try {
            api.connectToDevice(deviceId);

        } catch (PolarInvalidArgument a) {
            a.printStackTrace();
        }

        //textViewDeviceId.setText(deviceIdText);

        /*plotter = new HrAndRrPlotter();
        plotter.setListener(this);
        //plot.addSeries(plotter.getHrSeries(), plotter.getHrFormatter());
        plot.setRangeBoundaries(50, 100, BoundaryMode.AUTO);
        plot.setDomainBoundaries(0, 360000, BoundaryMode.AUTO);
        plot.setRangeStep(StepMode.INCREMENT_BY_VAL, 10.0);
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, 60000.0);
        plot.getGraph().getLineLabelStyle(XYGraphWidget.Edge.LEFT).setFormat(new DecimalFormat("#"));
        plot.setLinesPerRangeLabel(2);*/

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

    private int frameCount = 0;
    private void startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(IRActivity.this), image -> {
                    @OptIn(markerClass = ExperimentalGetImage.class) Image img = image.getImage();
                    Bitmap bitmap = translator.translateYUV(img, IRActivity.this);
                    InputImage inputImage = InputImage.fromBitmap(bitmap, 0);

                    if (detectionResult.getMainFace() != null && Objects.equals(detectionResult.getBarcodeMessage(), " ")) {
                        barcodeDetector.process(inputImage)
                                .addOnSuccessListener(barcodes -> barcodes.stream()
                                        .findFirst()
                                        .ifPresent(barcode -> {
                                            String s = barcode.getRawValue();
                                            if (isPhoneNumber(s)) {
                                                //Toast.makeText(this, "find", Toast.LENGTH_SHORT).show();
                                                detectionResult.setBarcodeMessage(s);
                                            }
                                        })).addOnFailureListener(e -> Log.e(TAG, "Error processing Image", e));
                    }

                    if (frameCount % UPDATE_RATE == 0) {
                        faceDetector.process(inputImage)
                                .addOnSuccessListener(faces -> {
                                    if (faces.isEmpty()) {
                                        detectionResult.setMainFace(null);
                                        detectionResult.setBarcodeMessage(" ");
                                        p = null;
                                    } else {
                                        Face largestFace = faces.stream()
                                                .max(Comparator.comparingInt(face ->
                                                        face.getBoundingBox().width() * face.getBoundingBox().height()))
                                                .orElse(null);
                                        detectionResult.setMainFace(largestFace);
                                    }
                                }).addOnFailureListener(e -> Log.e(TAG, "Error processing Image", e));
                    }

                    preview.setRotation(image.getImageInfo().getRotationDegrees());
                    preview2.setRotation(image.getImageInfo().getRotationDegrees());

                    drawImage(bitmap, (int) (bitmap.getWidth() * 0.08));

                    image.close();
                    frameCount++;
                });

                cameraProvider.bindToLifecycle(IRActivity.this, cameraSelector, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                Log.e(TAG, "Bind Error", e);
            }
        }, ContextCompat.getMainExecutor((this)));
    }

    public void drawImage(Bitmap bitmap, int step) {
        p = MainActivity.base.getPerson(Person.makeId(detectionResult.getBarcodeMessage()));
        if (p != null && second % 6 == 0) {
            //p.setMood(p.getPulseRate());
        }

        Bitmap drawable = DrawDetection.drawDetection(bitmap, p,
                detectionResult.getMainFace());

        preview.setImageBitmap(Bitmap.createBitmap(drawable, step, 0,
                drawable.getWidth() - step, drawable.getHeight()));

        preview2.setImageBitmap(Bitmap.createBitmap(drawable, 0, 0,
                drawable.getWidth() - step, drawable.getHeight()));
    }

    @Override
    public void update() {
        plot.redraw();
    }

    public void streamHR() {

        boolean isDisposed = hrDisposable != null ? hrDisposable.isDisposed() : true;
        if (isDisposed) {
            hrDisposable = api.startHrStreaming(deviceId)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            hrData -> {
                                for (PolarHrData.PolarHrSample sample : hrData.getSamples()) {
                                    Log.d(TAG, "HR " + sample.getHr() + " RR " + sample.getRrsMs());
                                    if (p!=null) {
                                        p.setMood(sample.getHr());
                                    }
                                    //plotter.addValues(sample);
                                }

                            },
                            error -> {
                                Log.e(TAG, "HR stream failed. Reason " + error);
                                hrDisposable = null;
                            },
                            () -> Log.d(TAG, "HR stream complete")
                    );
        } else {
            hrDisposable.dispose();
            hrDisposable = null;
        }
    }
}