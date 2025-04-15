package com.fz.flink;


import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class FlinkJob {

    public static void main(String[] args) throws Exception {
        // 设置 Flink 流处理环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从文件中读取数据
        DataStream<DataProcessor.DataPoint> dataStream = env.addSource(new SourceFunction<DataProcessor.DataPoint>() {
            @Override
            public void run(SourceContext<DataProcessor.DataPoint> ctx) throws Exception {
                String filePath = "test-latency.txt";  // 替换为实际的文件路径
                List<DataProcessor.DataPoint> data = DataProcessor.readAndParseFile(filePath);

                for (DataProcessor.DataPoint dataPoint : data) {
                    ctx.collect(dataPoint);
                }
            }

            @Override
            public void cancel() {
                // 可以根据需要实现取消操作
            }
        });

        // 按照每60条数据进行窗口切分
        DataStream<String> responseStream = dataStream
                .countWindowAll(60)  // 定义每60条数据作为一个窗口

                .process(new ProcessAllWindowFunction<DataProcessor.DataPoint, String, GlobalWindow>() {
                    @Override
                    public void process(Context context, Iterable<DataProcessor.DataPoint> elements, Collector<String> out) throws Exception {
                        // 将当前窗口的所有数据点收集到一个 List 中
                        List<DataProcessor.DataPoint> dataPoints = new ArrayList<>();
                        for (DataProcessor.DataPoint dataPoint : elements) {
                            dataPoints.add(dataPoint);
                        }

                        // 将数据点转换为 JSON 格式
                        String jsonInput = JsonFormatter.formatToJson(dataPoints);

                        // 调用 Flask API 获取预测结果
                       String response = FlaskAPIcaller.callFlaskApi(jsonInput);

                        // 输出返回的响应数据
                        out.collect(response);
                        // 后续逻辑：解析负载预测结果并处理实例启动
                        LoadPredictionHandler loadHandler = new LoadPredictionHandler();
                        List<Double> predictedLoads = loadHandler.parseLoadPrediction(response);

                        // 获取负载最高的时刻
                        int highestLoadIndex = loadHandler.getHighestLoadIndex(predictedLoads)+1;
                        System.out.println("负载最高的时刻是第 " + highestLoadIndex + " 秒");

                        // 根据最高负载时刻延迟启动 ECS 实例
                       long delayInSeconds = highestLoadIndex;  // 延迟设为负载峰值时刻
                        String instanceId = "i-bp10cesomme27olv3kvh";
                        EcsInstanceStarter starter = new EcsInstanceStarter();
                        starter.scheduleInstanceStart(instanceId,0); // 延迟启动实例
                        starter.scheduleInstanceStop(instanceId,20);
                        try {
                            // 让当前线程休眠 50 秒
                            Thread.sleep(55000);  // 50000毫秒 = 50 秒
                        } catch (InterruptedException e) {
                            // 处理线程中断的异常
                            e.printStackTrace();
                        }
                    }
                });
        // 打印结果（或者你可以将其写入其他数据源）
        responseStream.print();

        // 执行任务
        env.execute("Flink Job");
    }
}
