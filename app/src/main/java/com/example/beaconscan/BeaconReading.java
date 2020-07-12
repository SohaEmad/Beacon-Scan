package com.example.beaconscan;

import org.altbeacon.beacon.Identifier;

import java.util.List;

public class BeaconReading {
    private String uuid;
    private int rssi;

    public BeaconReading(List<Identifier> identifiers, int rssi) {
        this.uuid = generateID(identifiers);
        this.rssi = rssi;
    }

    public BeaconReading(String uuid, int rssi) {
        this.uuid = uuid;
        this.rssi = rssi;
    }

    public String getUuid() {
        return uuid;
    }

    public int getRssi() {
        return rssi;
    }

    private String generateID(List<Identifier> identifiers) {
        StringBuilder ID = new StringBuilder();
        for (Identifier id : identifiers) {
            ID.append(id.toString());
        }
        return ID.toString().toUpperCase();
    }
}
