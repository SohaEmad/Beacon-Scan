package com.example.beaconscan;

class Constants {
    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    static final String DECIMAL_REGEX_PATTERN = "-?[0-9.]+";

    // These should match the S3 folder they are contained in
    static final String WIFI_GRID = "wifi";
    static final String BEACON_GRID = "beacon";
    static final String MAG_GRID = "magnetics";

    //Beacon Constants
    static final String IBEACON_PARSER_LAYOUT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    static final String LIVINGMAP_REGION_ID = "livingmap_region_id";

    static final double SMALL_TOLERANCE = 0.0001;
}
