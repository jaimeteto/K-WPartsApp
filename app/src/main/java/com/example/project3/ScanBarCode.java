package com.example.project3;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.project3.databinding.ActivityScanBarCodeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanBarCode extends AppCompatActivity {
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    //    private ContentResolver contentResolver;
   private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
//
//    private static final String TAG = "CameraXApp";
//    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
//    private static final String PHOTO_EXTENSION = ".jpg";
//    private static final String VIDEO_EXTENSION = ".mp4";
//
private ActivityScanBarCodeBinding viewBinding;
//    private ExecutorService cameraExecutor;
//    private ImageCapture imageCapture;
//    //private VideoCapture<Recorder> videoCapture;
//    private Recording recording;
//    private ImageView imageView;
//
//    private Executor executor = Executors.newSingleThreadExecutor();
//
//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityScanBarCodeBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        // Request camera permissions
        if (allPermissionsGranted()) {
            Log.e("iniside if ", "allpermissions failed");
            startBarcodeScanner();
        } else {
            requestPermissions();
        }
    }
    private void startBarcodeScanner() {
        // Start the ZXing barcode scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);  // Use ONE_D_CODE_TYPES for one-dimensional barcodes
        integrator.setPrompt("Scan a Code 128 barcode");
        integrator.setOrientationLocked(false);  // Allow both portrait and landscape orientations
        integrator.initiateScan();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Receive the result from the ZXing barcode scanner
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Handle the scanned barcode value
                String barcodeValue = result.getContents();
                Log.e("BarcodeResult ", barcodeValue);
                // Process the barcode value as needed
                // You can send it to another activity, display it, etc.
            } else {
                // Handle the case where the user canceled the scan
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123 && grantResults.length > 0 && grantResults[0] == 0) {
            // Camera permission granted, start barcode scanner
            startBarcodeScanner();
        } else {
            // Handle the case where the user denied the camera permission
            // You may want to show a message or take appropriate action
        }
    }
//        imageView = viewBinding.imageView;
//        // Set up the listeners for take photo and video capture buttons
//        viewBinding.captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                takePhoto();
//                //startActivityForResult(intent, ADD_NEW_ENTRY_REQUEST_CODE);
//
//            }
//        });
//        //viewBinding.videoCaptureButton.setOnClickListener(v -> captureVideo());
//
//        cameraExecutor = Executors.newSingleThreadExecutor();
//    }
//
//    private void startCamera() {
//        ProcessCameraProvider cameraProvider = null;
//        try {
//            cameraProvider = ProcessCameraProvider.getInstance(this).get();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        if (cameraProvider != null) {
//            // Set up the preview use case
//            Preview preview = new Preview.Builder().build();
//            CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
//            imageCapture = new ImageCapture.Builder().build();
//
//            try {
//                // Unbind any existing use cases before binding new use cases
//                cameraProvider.unbindAll();
//
//                // Bind the preview use case
//                cameraProvider.bindToLifecycle(this, cameraSelector, preview,imageCapture);
//
//                // Attach the preview surface provider to the PreviewView
//                preview.setSurfaceProvider(viewBinding.previewView.getSurfaceProvider());
//            } catch (Exception exc) {
//                Log.e(TAG, "Use case binding failed", exc);
//            }
//        }
//    }
//
//    private void takePhoto() {
//        // Get a stable reference of the modifiable image capture use case
//        if (imageCapture == null) {
//            return;
//        }
//
//        // Create time-stamped name and MediaStore entry.
//        String name = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
//                .format(System.currentTimeMillis());
//
//
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
//        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
//        contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image");
//
//
//         //Create output options object which contains file + metadata
////        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(
////                contentResolver,
////                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
////                contentValues)
////                .build();
//
//        //File imageFile = new File(getExternalMediaDirs()[0], "myImage.jpg");
//        File file = new File(getExternalFilesDir(null), "HeyThisISJayuir.jpg");
//
//// Create an OutputFileOptions object
//        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file)
//                .build();
//       // File outputFile = new File("/storage/emulated/0/Pictures/CameraX-Image/image_filename.jpg");
////        File outputDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "CameraX-Image");
////        if (!outputDirectory.exists()) {
////            outputDirectory.mkdirs();
////        }
////        String path = Environment.getExternalStorageDirectory().getPath();
////        File outputFile = new File(path, "image_filename.jpg");
////        ImageCapture.OutputFileOptions outputOptions= new ImageCapture.OutputFileOptions.Builder(outputFile).build();
//
//
//
//        // Set up image capture listener, which is triggered after the photo has been taken
//        imageCapture.takePicture(
//                outputFileOptions,
//                ContextCompat.getMainExecutor(ScanBarCode.this),
//                new ImageCapture.OnImageSavedCallback() {
//                    @Override
//                    public void onError(ImageCaptureException exc) {
//                        Log.e("TAG", "Photo capture failed: " + exc.getMessage(), exc);
//                    }
//
//                    @Override
//                    public void onImageSaved(ImageCapture.OutputFileResults output) {
//                        String msg = "Photo capture succeeded: " + output.getSavedUri();
//                        Toast.makeText(ScanBarCode.this, msg, Toast.LENGTH_SHORT).show();
//                        Log.d("TAG", msg);
//                        imageView.setImageURI(output.getSavedUri());
//                        imageView.setVisibility(View.VISIBLE);
//                        viewBinding.previewView.setVisibility(View.GONE);
//                        processImageForBarcode(output.getSavedUri());
//
//                    }
//
//                }
//
//        );
//    }
//
//
//
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
//
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, CAMERA_PERMISSION_REQUEST_CODE);
    }
}
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }
//
//    private File createFile(File baseFolder, String format, String extension) {
//        return new File(baseFolder, new SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension);
//    }
//
//    private File outputDirectory() {
//        File mediaDir = getExternalMediaDirs()[0];
//        return new File(mediaDir, getResources().getString(R.string.app_name));
//    }
//    private void processImageForBarcode(Uri imageUri) {
//        try {
//            InputImage image = InputImage.fromFilePath(this, imageUri);
//            BarcodeScannerOptions options =
//                    new BarcodeScannerOptions.Builder()
//                            .setBarcodeFormats(Barcode.FORMAT_CODE_128)
//                            .build();
//            BarcodeScanner scanner = BarcodeScanning.getClient(options);
//
//            scanner.process(image)
//                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
//                        @Override
//                        public void onSuccess(List<Barcode> barcodes) {
//                            // Handle barcode results
//                            if(barcodes.isEmpty()){
//                                Log.d("Barcode", "Error empty");
//                            }
//                            for (Barcode barcode : barcodes) {
//                                String value = barcode.getDisplayValue();
//                                // Handle the barcode value as needed
//                                Log.d("Barcode", "Value: " + value);
//                                //Toast.makeText(ScanBarCode.this, "success Barcode: " + value, Toast.LENGTH_SHORT).show();
//
//                            }
//
//                            // Switch back to the camera preview
//                            //switchToCameraPreview();
//                           // Toast.makeText(ScanBarCode.this, "success Barcode: ", Toast.LENGTH_SHORT).show();
//
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            // Handle the failure
//                            e.printStackTrace();
//
//                            // Switch back to the camera preview
//                            Log.d("Barcode", "Error ");
//
//
//                            Toast.makeText(ScanBarCode.this, "Error taking Barcode", Toast.LENGTH_SHORT).show();
//                            //switchToCameraPreview();
//                        }
//                    });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void switchToCameraPreview() {
//        imageView.setVisibility(View.GONE);
//        viewBinding.previewView.setVisibility(View.VISIBLE);
//    }
//
//}




