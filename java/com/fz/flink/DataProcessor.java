package com.fz.flink;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class DataProcessor {

    private static final int PEAK_THRESHOLD = 4000;

    // OSS 配置信息
    private static final String OSS_ENDPOINT = "oss-cn-shanghai.aliyuncs.com";
    private static final String OSS_ACCESS_KEY_ID = "LTAI5tC9DPavFsFw22KaUguZ";
    private static final String OSS_ACCESS_KEY_SECRET = "riPkwusugypINP0GKWD1pFII6DpgLx";
    private static final String OSS_BUCKET_NAME = "aliyun-wb-ftiztuhzco";

    public static List<DataPoint> readAndParseFile(String ossFilePath) throws IOException {
        List<DataPoint> data = new ArrayList<>();

        // 初始化 OSS 客户端，使用新的方式
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        OSS ossClient = new OSSClient(OSS_ENDPOINT, OSS_ACCESS_KEY_ID, OSS_ACCESS_KEY_SECRET, clientConfiguration);

        // 从 OSS 获取文件内容
        OSSObject ossObject = ossClient.getObject(OSS_BUCKET_NAME, ossFilePath);
        InputStream inputStream = ossObject.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            Pattern pattern = Pattern.compile("\\[(\\d{2}:\\d{2}:\\d{2})\\] (\\d+) (.+)");
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.matches()) {
                String timestamp = matcher.group(1);
                int numBarrages = Integer.parseInt(matcher.group(2));
                String content = matcher.group(3);

                // 解析时间戳
                String[] timeParts = timestamp.split(":");
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                int second = Integer.parseInt(timeParts[2]);

                // 计算8个特征值
                double hourSin = Math.sin(2 * Math.PI * hour / 24);
                double hourCos = Math.cos(2 * Math.PI * hour / 24);
                double minuteSin = Math.sin(2 * Math.PI * minute / 60);
                double minuteCos = Math.cos(2 * Math.PI * minute / 60);
                double secondSin = Math.sin(2 * Math.PI * second / 60);
                double secondCos = Math.cos(2 * Math.PI * second / 60);

                // 判断是否为峰值时段
                int isPeak = numBarrages > PEAK_THRESHOLD ? 1 : 0;

                data.add(new DataPoint(numBarrages, hourSin, hourCos, minuteSin, minuteCos, secondSin, secondCos, isPeak));
            }
        }

        // 关闭 OSS 客户端和流
        reader.close();
        ossClient.shutdown();

        return data;
    }

    // 数据点类
    public static class DataPoint {
        int load;
        double hourSin;
        double hourCos;
        double minuteSin;
        double minuteCos;
        double secondSin;
        double secondCos;
        int isPeak;

        public DataPoint(int load, double hourSin, double hourCos, double minuteSin, double minuteCos, double secondSin, double secondCos, int isPeak) {
            this.load = load;
            this.hourSin = hourSin;
            this.hourCos = hourCos;
            this.minuteSin = minuteSin;
            this.minuteCos = minuteCos;
            this.secondSin = secondSin;
            this.secondCos = secondCos;
            this.isPeak = isPeak;
        }
    }
}

