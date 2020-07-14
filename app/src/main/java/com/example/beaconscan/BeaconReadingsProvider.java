package com.example.beaconscan;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.util.Log;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BeaconReadingsProvider implements BeaconConsumer, RangeNotifier {

    private final String TAG = "BeaconReadingsProvider";
    private Context context;
    private BeaconManager beaconManager;
    private Region regionLivingmap;
    private BeaconReadingsListener beaconReadingsListener;
    private List<BeaconReading> beacons;
    private long inTime;
    Map<String, List<BeaconReading>> outputuidRssi;

    BeaconReadingsProvider(Context context, BeaconReadingsListener beaconReadingsListener) {

        this.context = context;
        this.beacons = new ArrayList<>();
        this.beaconReadingsListener = beaconReadingsListener;
//        this.timer = System.currentTimeMillis();
        beaconManager = BeaconManager.getInstanceForApplication(context);
        beaconManager.setForegroundBetweenScanPeriod(300);

        initParsers();
    }

    public void startUpdating(Long inTime) {
        if (beaconManager.isAnyConsumerBound()) {
            Log.d(TAG, "BeaconManager class is bound and working, wait for it to finish and then use it");
        }
        beaconManager.bind(this);
        this.inTime=inTime;
        Log.d(TAG, "Starting scanning for beacon readings");
    }

    public void stopUpdating() {
        beaconManager.removeAllRangeNotifiers();
//        beaconManager.stopRangingBeaconsInRegion(regionLivingmap);
        beaconManager.unbind(this);
        Log.d(TAG, "Stopping scanning for beacon readings");
    }

    public void initParsers() {


        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(Constants.IBEACON_PARSER_LAYOUT));

        // Detects AltBeacon protocols
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.ALTBEACON_LAYOUT));

        // Detects EddystoneUID protocols:
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT));
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_TLM_LAYOUT));

        regionLivingmap = new Region(
                Constants.LIVINGMAP_REGION_ID,
                null,
                null,
                null
        );

    }


    @Override
    public Context getApplicationContext() {
        return context.getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        context.unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return context.bindService(intent, serviceConnection, i);
    }

    private Map<String, List<BeaconReading>> getReadings(List<BeaconReading> readings) {
        Map<String, List<Integer>> uidRssi = new HashMap<>();
        Map<String, List<BeaconReading>> outputuidRssi = new HashMap<>();

        for (BeaconReading reading : readings) {
            if (uidRssi.containsKey(reading.getUuid())) {
                uidRssi.get(reading.getUuid()).add(reading.getRssi());
            } else {
                List<Integer> list = new ArrayList<Integer>();
                list.add(reading.getRssi());
                uidRssi.put(reading.getUuid(), list);
            }
        }

        List<BeaconReading> averagedReadings = new ArrayList<>();
        List<BeaconReading> maxReadings = new ArrayList<>();
        List<BeaconReading> minReadings = new ArrayList<>();

        for (String key : uidRssi.keySet()) {
            int numOfReadings = uidRssi.get(key).size();
            int sumOfRssi = 0;
            for (int rssi : uidRssi.get(key)) {
                sumOfRssi += rssi;
            }
            averagedReadings.add(new BeaconReading(key, sumOfRssi / numOfReadings));
            maxReadings.add(new BeaconReading(key, Collections.max(uidRssi.get(key))));
            minReadings.add(new BeaconReading(key, sumOfRssi / numOfReadings));


        }
        outputuidRssi.put("avgBeacon", averagedReadings);
        outputuidRssi.put("minBeacon", minReadings);
        outputuidRssi.put("maxBeacon", maxReadings);
        return outputuidRssi;
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
                Log.d(TAG, String.format("Beacons ranged = %d - %d", collection.size(), System.currentTimeMillis()));
                if (collection.size() > 0) {
                    for (Beacon beacon : collection) {
                        beacons.add(new BeaconReading(beacon.getIdentifiers(), beacon.getRssi()));
//                        System.out.println("the RSSI is " + beacon.getRssi());
                    }
                    System.out.println( System.currentTimeMillis() +"the difference "+ inTime);
                    long scanTimeDifference = System.currentTimeMillis() - inTime;
                    if (scanTimeDifference > 30000) {


                        if (beaconReadingsListener != null) {
                            beaconReadingsListener.onBeaconReadingsUpdate(outputuidRssi = getReadings(beacons));
                            System.out.println("avg data" + outputuidRssi.get("avgBeacon").get(0).getRssi());
                            System.out.println("min data" + outputuidRssi.get("minBeacon").get(0).getRssi());
                            System.out.println("max data" + outputuidRssi.get("maxBeacon").get(0).getRssi());
                        }
//                        Log.d(TAG, String.format("Beacons ranged = %d - %d", collection.size(), System.currentTimeMillis()));
//                        timer = System.currentTimeMillis();
                    }
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
    }

    interface BeaconReadingsListener {
        void onBeaconReadingsUpdate(Map<String, List<BeaconReading>> outputuidRssi);
    }

}
