package com.example.beaconscan;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class recordLog {

    public void writeToCSV(List<String []> data) throws IOException {

        File folder = new File("/sdcard/Download");

        boolean var = false;
        if (!folder.exists())
            var = folder.mkdir();

        System.out.println("" + var);


        final String filename = folder.toString() + "/" + "Test.csv";


        try {
            CSVWriter writer = new CSVWriter(new FileWriter(filename));
            writer.writeAll(data);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();

        }

    }
}
