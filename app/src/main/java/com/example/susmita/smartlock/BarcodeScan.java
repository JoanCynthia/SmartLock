package com.example.susmita.smartlock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class BarcodeScan extends AppCompatActivity {

    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
//    Button btnAction;
    String intentData = "";
    boolean isEmail = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scan);

        initViews();
    }

    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);



//        btnAction.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (intentData.length() > 0) {
//                    if (isEmail) ;
////                        startActivity(new Intent(Scanned_Barcode.this, EmailActivity.class).putExtra("email_address", intentData));
//                    else {
////                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
//                    }
//                }
//
//
//            }
//        });
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(BarcodeScan.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                        Toast.makeText(getApplicationContext(), "IF", Toast.LENGTH_SHORT).show();

                    } else {
                        ActivityCompat.requestPermissions(BarcodeScan.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                        Toast.makeText(getApplicationContext(), "ELSE", Toast.LENGTH_SHORT).show();

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                Toast.makeText(getApplicationContext(), "Entered", Toast.LENGTH_SHORT).show();

                if (barcodes.size() != 0) {

                    Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_SHORT).show();
                    txtBarcodeValue.post(new Runnable() {

                        @Override
                        public void run() {

                            if (barcodes.valueAt(0).rawValue != null) {
                                txtBarcodeValue.removeCallbacks(null);
                                intentData = barcodes.valueAt(0).rawValue;
                                txtBarcodeValue.setText(intentData.equals("")?"":"Detected");
                                isEmail = true;
//                                btnAction.setText("Unlock");
                                setResult(RESULT_OK, new Intent().putExtra("qr_data",intentData));

                                //
                                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150);
                                //

                                finish();
                            } else {
                                setResult(RESULT_CANCELED, new Intent().putExtra("qr_data", "failed"));
                                finish();
                            }
                        }
                    });

                }
                else {
                    Toast.makeText(getApplicationContext(), "barcode size 0", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();


    }
}
