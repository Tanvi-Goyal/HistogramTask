package com.example.tanvi.histogramtask;

import android.util.SparseArray;

import org.opencv.core.Mat;

import java.util.ArrayList;

public class HistogramHelper {


    private HistogramHelper() {

    }

    public static SparseArray<ArrayList<Float>> createCompartments(Mat histogram) {
        int binsCount = 256;
        float[] histData = new float[binsCount];
        histogram.get(0, 0, histData);

        int compartmentsCount = 5;
        int interval = binsCount / compartmentsCount;

        SparseArray<ArrayList<Float>> compartments = new SparseArray<>();
        for (int i = 0; i < compartmentsCount; i++) {
            int start = interval * i;
            int end = start + interval;
            ArrayList<Float> tmp = new ArrayList<>();
            for (int j = start; j < end; j++) {
                tmp.add(histData[j]);
            }
            compartments.put(i, tmp);
        }
        return compartments;
    }


    public static float sumCompartmentsValues(SparseArray<ArrayList<Float>> compartments) {
        float sum = 0L;
        for (int i = 0; i < compartments.size(); i++) {
            for (int j = 0; j < compartments.get(i).size(); j++) {
                sum += compartments.get(i).get(j);
            }
        }
        return sum;
    }

    public static float averageValueOfCompartments(SparseArray<ArrayList<Float>> compartments) {
        return sumCompartmentsValues(compartments) / compartments.size();
    }


}
