package com.example.petya.testkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Emotion;
import com.microsoft.projectoxford.face.contract.Face;
import com.wonderkiln.camerakit.CameraKit;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventCallback;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private CameraView mCameraView;


    private final String apiEndpoint = "https://eastus.api.cognitive.microsoft.com/face/v1.0";
    private final String subscriptionKey = "key";

    private final FaceServiceClient faceServiceClient =
            new FaceServiceRestClient(apiEndpoint, subscriptionKey);

    private static List<Face> mFaceList;
    Handler h;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        final int fName = intent.getIntExtra("fname", 10);

        mFaceList = new ArrayList<>();
        mCameraView = findViewById(R.id.camera);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                mCameraView.captureImage();
            }
        };

        Thread t = new Thread(new Runnable() {
            public void run() {
                for (int i = 1; i <= fName; i++) {
                    try {
                        TimeUnit.SECONDS.sleep(8);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    h.sendEmptyMessage(i);
                }
                h.sendEmptyMessage(999);
            }
        });
        t.start();

        mCameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                detectAndFrame(cameraKitImage.getBitmap());
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void init(Face[] face) {
        if (!mFaceList.isEmpty()) {
            BarChart barChart = findViewById(R.id.chart);

            BarData data = new BarData(getXAxisValues(),getDataSet(face));
            barChart.setData(data);
            barChart.invalidate();
        }
    }

    private ArrayList getDataSet(Face[] face) {
        ArrayList dataSets = null;

        Emotion emotion = middle(face);

        ArrayList valueSet1 = new ArrayList<>();
        BarEntry v1e1 = new BarEntry((float) emotion.anger, 0);
        valueSet1.add(v1e1);
        BarEntry v1e2 = new BarEntry((float) emotion.contempt, 1);
        valueSet1.add(v1e2);
        BarEntry v1e3 = new BarEntry((float) emotion.disgust, 2);
        valueSet1.add(v1e3);
        BarEntry v1e4 = new BarEntry((float) emotion.fear, 3);
        valueSet1.add(v1e4);
        BarEntry v1e5 = new BarEntry((float) emotion.happiness, 4);
        valueSet1.add(v1e5);
        BarEntry v1e6 = new BarEntry((float) emotion.neutral, 5);
        valueSet1.add(v1e6);
        BarEntry v1e7 = new BarEntry((float) emotion.sadness, 6);
        valueSet1.add(v1e7);
        BarEntry v1e8 = new BarEntry((float) emotion.surprise, 7);
        valueSet1.add(v1e8);

        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Brand 1");
        barDataSet1.setColor(Color.rgb(0, 155, 0));

        dataSets = new ArrayList<>();
        dataSets.add(barDataSet1);

        return dataSets;
    }

    private ArrayList<String> getXAxisValues() {
        ArrayList<String> xAxis = new ArrayList<>();
        xAxis.add("anger");
        xAxis.add("contempt");
        xAxis.add("disgust");
        xAxis.add("fear");
        xAxis.add("happiness");
        xAxis.add("neutral");
        xAxis.add("sadness");
        xAxis.add("surprise");
        return xAxis;
    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraView.stop();
    }

    private void detectAndFrame(final Bitmap imageBitmap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        @SuppressLint("StaticFieldLeak") AsyncTask<InputStream, String, Face[]> detectTask =
                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    new FaceServiceClient.FaceAttributeType[] {
                                            FaceServiceClient.FaceAttributeType.Emotion}
                            );
                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        if (result == null) return;
                        mFaceList.addAll(Arrays.asList(result));
                        init(result);
                    }
                };
        detectTask.execute(inputStream);
    }


    private Emotion middle(Face[] face) {
        Emotion emotion = new Emotion();

        for (Face aFace : face) {
            emotion.anger += aFace.faceAttributes.emotion.anger;
            emotion.contempt += aFace.faceAttributes.emotion.contempt;
            emotion.disgust += aFace.faceAttributes.emotion.disgust;
            emotion.fear += aFace.faceAttributes.emotion.fear;
            emotion.happiness += aFace.faceAttributes.emotion.happiness;
            emotion.neutral += aFace.faceAttributes.emotion.neutral;
            emotion.sadness += aFace.faceAttributes.emotion.sadness;
            emotion.surprise += aFace.faceAttributes.emotion.surprise;
        }

        emotion.anger = emotion.anger / face.length;
        emotion.contempt = emotion.contempt / face.length;
        emotion.disgust = emotion.disgust / face.length;
        emotion.fear = emotion.fear / face.length;
        emotion.happiness = emotion.happiness / face.length;
        emotion.neutral = emotion.neutral / face.length;
        emotion.sadness = emotion.sadness / face.length;
        emotion.surprise = emotion.surprise / face.length;

        return emotion;
    }




}
