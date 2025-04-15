package com.fz.flink;


import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
public class LoadPredictionHandler {

    // 从 Flask API 获取负载预测结果

    // 解析 Flask 返回的预测数据
    public List<Double> parseLoadPrediction(String response) {
        // 解析 JSON 格式的响应
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray predictionArray = jsonResponse.getJSONArray("prediction");

        // 假设我们只关心第一个数组中的负载预测
        JSONArray loadArray = predictionArray.getJSONArray(0);

        List<Double> predictedLoads = new ArrayList<>();
        for (int i = 0; i < loadArray.length(); i++) {
            predictedLoads.add(loadArray.getDouble(i));  // 将预测结果转换为 List<Double>
        }
        return predictedLoads;
    }

    // 获取负载最高的时刻
    public int getHighestLoadIndex(List<Double> predictedLoads) {
        double maxLoad = Double.MIN_VALUE;
        int maxLoadIndex = -1;

        for (int i = 0; i < predictedLoads.size(); i++) {
            if (predictedLoads.get(i) > maxLoad) {
                maxLoad = predictedLoads.get(i);
                maxLoadIndex = i;
            }
        }

        return maxLoadIndex;
    }
}