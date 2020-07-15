package com.example.beaconscan;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity implements BeaconReadingsProvider.BeaconReadingsListener {

    protected static final String TAG = "RangingActivity";
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    Button start;
    Date currentTime = Calendar.getInstance().getTime();
    List<String> logRows = new ArrayList<>();
    String logRow;
    TextView Timer;
    int counter = 0;
    int steps = 0;
    ArrayAdapter<String> adapter;
    CountDownTimer countDownTimer;
    ProgressBar simpleProgressBar;
    Button share;
    File file;
    File singleFile;
    String name = Build.MODEL;
    FileOutputStream fOut;
    private BeaconReadingsProvider beaconReadingsProvider;
    private ListView list;
    private double expermintSteps[] = {0.25, 0.5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 30, 40};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = findViewById(R.id.start);
        start.setText("calculate RSSI on  " + expermintSteps[0] + " meter");

        Timer = findViewById(R.id.timer);
        list = findViewById(R.id.list);
        share = findViewById(R.id.share);
        simpleProgressBar = findViewById(R.id.experimentProgress);
        simpleProgressBar.setMax(expermintSteps.length);

        beaconReadingsProvider = new BeaconReadingsProvider(this, this);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, logRows);
        list.setAdapter(adapter);

        createLog();

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("the couneter value is " + steps);
                if (steps < expermintSteps.length) {
                    beaconReadingsProvider.startUpdating(System.currentTimeMillis());
                    start.setEnabled(false);
                    countDown();
                    steps++;

                } else if (steps == expermintSteps.length - 1) {

                    beaconReadingsProvider.startUpdating(System.currentTimeMillis());
                    countDown();
                    try {
                        fOut.flush();
                        fOut.close();
                    } catch (IOException e) {
                        Log.e("Exception", "File write failed: " + e.toString());
                        e.printStackTrace();
                    }
                } else {
                    start.setText("Start experiment again");
                    steps = 0;
                    onClick(v);
                }
                simpleProgressBar.setProgress(steps);

            }
        });

        share.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/comma_separated_values/csv");// You Can set source type here like video, image text, etc.
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("sdcard/Download/" + name + ".csv"));
                shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(shareIntent, "Share File Using!"));
            }
        });
    }

    public void countDown() {
        countDownTimer = new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                Timer.setText(String.valueOf(counter));
                counter++;

            }

            public void onFinish() {
                Timer.setText("30 Seconds");
                counter = 0;
                start.setText("calculate RSSI on  " + expermintSteps[steps] + " meter");

            }
        }.start();


    }

    @Override
    public void onBeaconReadingsUpdate(Map<String, List<BeaconReading>> outputuidRssi) {

        logRow = currentTime + "," + expermintSteps[steps] + "," + outputuidRssi.get("avgBeacon").get(0).getUuid() + "," + outputuidRssi.get("avgBeacon").get(0).getRssi() + "," + outputuidRssi.get("minBeacon").get(0).getRssi() + "," + outputuidRssi.get("maxBeacon").get(0).getRssi();
        logRows.add(logRow);
        adapter.notifyDataSetChanged();
        beaconReadingsProvider.stopUpdating();
        writeLog(logRow);
        writeSingleLog(logRow);
        start.setEnabled(true);
    }


    private void createLog() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy,HH-mm-ss", Locale.getDefault());
        String formattedDate = df.format(c);
//        File directory = new File(Environment.getExternalStorageDirectory()+);
//        directory.mkdirs();
        file = new File(Environment.getExternalStorageDirectory(), name +"_date"+ formattedDate + ".csv");
       if(file.exists()){
           file.delete();
       }
        try {
            file.createNewFile();
            FileOutputStream myOutWriter = new FileOutputStream(file, true);
            System.out.println("write to log ");
            myOutWriter.write(" time ,distance,uuid, avg,min, max".getBytes());
            myOutWriter.write("\n".getBytes());
            myOutWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            e.printStackTrace();
        }

    }

    private void writeLog(String item) {
        try {
            FileOutputStream myOutWriter = new FileOutputStream(file, true);

            myOutWriter.write(item.getBytes());
            myOutWriter.write("\n".getBytes());
            myOutWriter.close();

        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            e.printStackTrace();
        }
    }

    private void writeSingleLog(String item) {

        singleFile = new File(Environment.getExternalStorageDirectory(), name +"step"+ steps + ".csv");
        try {
            singleFile.createNewFile();
            FileOutputStream myOutWriter = new FileOutputStream(singleFile, true);
            myOutWriter.write(" time ,experimentDistance,uuid, avg,min, max".getBytes());
            myOutWriter.write("\n".getBytes());
            myOutWriter.write(item.getBytes());
            myOutWriter.write("\n".getBytes());
            myOutWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            e.printStackTrace();
        }
    }
}
