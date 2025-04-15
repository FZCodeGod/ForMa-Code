package com.fz.flink;

import java.io.*;
import java.net.*;

public class FlaskAPIcaller {

    public static String callFlaskApi(String jsonInput) throws IOException {
        // Flask API URL
        String url = "http://202.114.6.152:5000/predict"; // 修改为实际的API地址

        // 创建连接
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        // 发送POST请求
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInput.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // 获取响应
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        return response.toString();
    }


}
