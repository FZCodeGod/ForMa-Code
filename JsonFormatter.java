package com.fz.flink;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;



public class JsonFormatter {

    public static String formatToJson(List<DataProcessor.DataPoint> data) {
        // 每60条数据作为一组
        JSONArray inputArray = new JSONArray();
        for (int i = 0; i < data.size(); i += 60) {
            JSONArray featureGroup = new JSONArray();
            for (int j = i; j < i + 60 && j < data.size(); j++) {
                DataProcessor.DataPoint point = data.get(j);
                JSONArray featureVector = new JSONArray();
                featureVector.put(point.load);
                featureVector.put(point.hourSin);
                featureVector.put(point.hourCos);
                featureVector.put(point.minuteSin);
                featureVector.put(point.minuteCos);
                featureVector.put(point.secondSin);
                featureVector.put(point.secondCos);
                featureVector.put(point.isPeak);

                featureGroup.put(featureVector);
            }
            inputArray.put(featureGroup);

        }
        JSONObject result = new JSONObject();
        result.put("input", inputArray);

        return result.toString();  // 返回包含input的JSON对象

    }

}
