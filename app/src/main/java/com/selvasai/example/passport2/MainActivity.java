package com.selvasai.example.passport2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.innovatrics.mrz.MrzRecord;
import com.selvasai.passport.CameraView;
import com.selvasai.passport.Confidence;
import com.selvasai.passport.Presenter;
import com.selvasai.passport.Recognizer;
import com.selvasai.passport.RecognizerImpl;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Presenter {
    private static final int REQ_PERMISSION_RESULT = 0;

    Recognizer mRecognizer;

    Button mGallery;

    TextView mInfo;

    private static final int GALLERY_REQUEST_CODE = 1000;
    private Camera.Size mPreviewSize;

    private boolean mRunningRecognition = false;


    @Override
    public void onMrzRecognized(Bitmap bitmap, MrzRecord mrz) {
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("mrz", mrz);

        startActivity(intent);

        stopRecognition(this);

        finish();
    }

    @Override
    public void addCallbackBuffer(byte[] bytes) {

    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public Rect getAreaOfInterest() {
        return null;
    }

    @Override
    public void setAreaOfInterest(Rect rect) {

    }

    @Override
    public Camera.Size getPreviewSize() {
        return mPreviewSize;
    }

    @Override
    public int getCameraOrientation() {
        return 0;
    }

    @Override
    public void setStatus(Confidence confidence) {

    }

    @Override
    public void startPreview() {

    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void autoFocus() {

    }

    @Override
    public void toggleFlash() {

    }

    @Override
    public void startRecognition(Context context) {
        mRunningRecognition = true;
        mRecognizer.startRecognition(context);
    }

    @Override
    public void stopRecognition(Context context) {
        if (!mRunningRecognition) {
            return;
        }
        mRunningRecognition = false;
        mRecognizer.stopRecognition(context);
    }

    @Override
    public void setMrzArea(Rect rect) {
        mRecognizer.setMrzArea(rect);
    }

    @Override
    public void clearRecognitionResults() {
        mRecognizer.clearRecognitionResults();
    }

    @Override
    public void submitRequestedFrame(byte[] bytes) {
        mRecognizer.submitRequestedFrame(bytes);
    }

    @Override
    public String getVersion() {
        return mRecognizer.getVersion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mRecognizer = new RecognizerImpl(this);

        mGallery = (Button) findViewById(R.id.gallery);
        mGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestPermission();
            }
        });

        mInfo = (TextView) findViewById(R.id.sdk_info);

        init();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void requestPermission() {
        ArrayList<String> permissions = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.CAMERA);
        }

        if (permissions.size() > 0) {
            String[] temp = new String[permissions.size()];
            permissions.toArray(temp);
            ActivityCompat.requestPermissions(MainActivity.this, temp, REQ_PERMISSION_RESULT);
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    // 선택된 이미지 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Bitmap inputImage = null;
            try {
                inputImage = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                // Use the `bitmap` object as needed
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            if (!RecognizerImpl.isValidLicense(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "License expired!", Toast.LENGTH_SHORT).show();
                return;
            } else if (null == inputImage) {
                Toast.makeText(getApplicationContext(), "Image Load Fail!", Toast.LENGTH_SHORT).show();
                return;
            }
            stopRecognition(MainActivity.this);
            inputImage = Bitmap.createScaledBitmap(inputImage, inputImage.getWidth() + inputImage.getWidth() % 2, inputImage.getHeight() + inputImage.getHeight() % 2, false);
            Camera cam = Camera.open();
            mPreviewSize = cam.new Size(inputImage.getWidth(), inputImage.getHeight());
            setMrzArea(new Rect(0, 551, 1280, 720));
            startRecognition(MainActivity.this);
            submitRequestedFrame(bitmapToNV21(inputImage));
        }
    }

    private void init() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(RecognizerImpl.getExpiredDate(this));
        mInfo.setText("License expiration date: " + strDate);
    }
    public static byte[] bitmapToNV21(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // ARGB_8888 format에서 YUV420 format으로 변환
        byte[] nv21;
        int[] argb = new int[width * height];
        bitmap.getPixels(argb, 0, width, 0, 0, width, height);
        nv21 = new byte[width * height * 3 / 2];
        encodeYUV420SP(nv21, argb, width, height);

        return nv21;
    }

    private static void encodeYUV420SP(byte[] nv21, int[] argb, int width, int height) {
        final int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int argbIndex = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                a = (argb[argbIndex] >> 24) & 0xff;
                R = (argb[argbIndex] >> 16) & 0xff;
                G = (argb[argbIndex] >> 8) & 0xff;
                B = argb[argbIndex] & 0xff;

                // RGB to YUV
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                // 4 Y (luminance) values followed by 1 V (chrominance) value and 1 U (chrominance) value
                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                if (j % 2 == 0 && i % 2 == 0) {
                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
                }

                argbIndex++;
            }
        }
    }
}
