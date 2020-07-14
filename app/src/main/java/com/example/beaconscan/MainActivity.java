package com.example.beaconscan;

//import android.support.v4.content.LocalBroadcastManager;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends Activity implements BeaconReadingsProvider.BeaconReadingsListener {

    Button start;
    protected static final String TAG = "RangingActivity";
    private BeaconReadingsProvider beaconReadingsProvider;
    private ListView list;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_BACKGROUND_LOCATION = 2;
    private double expermintSteps[] = {0.25, 0.5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 30, 40};
    Date currentTime = Calendar.getInstance().getTime();
    int[][] readingsArray = new int[20][3];
    List<String> data = new ArrayList<>();
    String[] array;
    String item;
    TextView Timer;
    int counter = 0;
    int steps = 0;
    ArrayAdapter<String> adapter;
     CountDownTimer countDownTimer;
    ProgressBar simpleProgressBar;
    Button share;

 File file;
    String name = Build.MODEL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        file = new File("/sdcard/Download/", name+".csv");
        start = findViewById(R.id.start);
        Timer = findViewById(R.id.timer);
        list = findViewById(R.id.list);
       simpleProgressBar= findViewById(R.id.experimentProgress); // initiate the progress bar
share = findViewById(R.id.share);
        simpleProgressBar.setMax(expermintSteps.length);
  start.setText("calculate RSSI on  " + expermintSteps[0] + " meter");
        beaconReadingsProvider = new BeaconReadingsProvider(this, this);

         adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, data);
        list.setAdapter(adapter);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("the couneter value is " + steps);
                if (steps < expermintSteps.length) {
                    beaconReadingsProvider.startUpdating(System.currentTimeMillis());
                    start.setEnabled(false);
                    countDown();
                    steps++;
                } else if (steps == expermintSteps.length-1) {

                    beaconReadingsProvider.startUpdating(  System.currentTimeMillis());
                    countDown();
                    writeLog();
                } else {
                    start.setText("Start experiment again");
                    steps = 0;
                    onClick(v);
                }
simpleProgressBar.setProgress(steps);

    }
        });

        share.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/comma_separated_values/csv");// You Can set source type here like video, image text, etc.
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("sdcard/Download/"+name+".csv"));
                shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(shareIntent, "Share File Using!"));
        }
        });








    }
      public void countDown (){
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
//


    @Override
    public void onBeaconReadingsUpdate(Map<String, List<BeaconReading>> outputuidRssi) {
        readingsArray[steps] = new int[]{outputuidRssi.get("avgBeacon").get(0).getRssi(), outputuidRssi.get("minBeacon").get(0).getRssi(), outputuidRssi.get("maxBeacon").get(0).getRssi()};

        item = currentTime + "," + expermintSteps[steps] + "," + outputuidRssi.get("avgBeacon").get(0).getUuid() + "," + outputuidRssi.get("avgBeacon").get(0).getRssi() + "," + outputuidRssi.get("minBeacon").get(0).getRssi() + "," + outputuidRssi.get("maxBeacon").get(0).getRssi();
        data.add(item);
        adapter.notifyDataSetChanged();
        System.out.println(item);
        beaconReadingsProvider.stopUpdating();
        start.setEnabled(true);

    }


    private void writeLog() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd_MMM_yyyy", Locale.getDefault());
        String formattedDate = df.format(c);
        System.out.println(formattedDate);

        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            System.out.println("write to log ");
            myOutWriter.write(" time ,distance,uuid, avg,min, max");
            myOutWriter.append("\n");
            for (String item : data
            ) {
                myOutWriter.append(item);
                myOutWriter.append("\n");

            }
            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
            e.printStackTrace();
        }
    }
}
