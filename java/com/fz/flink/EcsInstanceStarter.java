package com.fz.flink;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.ecs.model.v20140526.StopInstancesRequest;
import com.aliyuncs.ecs.model.v20140526.StartInstancesRequest;
import com.aliyuncs.ecs.model.v20140526.StartInstancesResponse;
import com.aliyuncs.ecs.model.v20140526.StopInstancesResponse;


import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import java.util.Arrays;


import java.util.concurrent.TimeUnit;

public class EcsInstanceStarter {

    // 启动 ECS 实例
    public void startEcsInstance(String instanceId) {
        // 创建阿里云 ECS 客户端的配置
        String regionId = "cn-hangzhou";  // 你可以根据自己的区域选择正确的区域 ID
        String accessKeyId = "LTAI5tC9DPavFsFw22KaUguZ";  // 替换为你的 AccessKeyId
        String accessKeySecret = "riPkwusugypINP0GKWD1pFII6DpgLx";  // 替换为你的 AccessKeySecret

        // 配置客户端
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        StartInstancesRequest request = new StartInstancesRequest();
        request.setInstanceIds(Arrays.asList(instanceId));  // 设置实例 ID

        try {
            StartInstancesResponse response = client.getAcsResponse(request);
            System.out.println("实例启动成功: " + response.getRequestId());
        } catch (ClientException e) {
            System.out.println("启动实例失败: " + e.getMessage());
        }
    }
    // 停止 ECS 实例
    public void stopEcsInstance(String instanceId) {
        // 创建阿里云 ECS 客户端的配置
        String regionId = "cn-hangzhou";  // 你可以根据自己的区域选择正确的区域 ID
        String accessKeyId = "LTAI5tC9DPavFsFw22KaUguZ";  // 替换为你的 AccessKeyId
        String accessKeySecret = "riPkwusugypINP0GKWD1pFII6DpgLx";  // 替换为你的 AccessKeySecret

        // 配置客户端
        IClientProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        StopInstancesRequest request = new StopInstancesRequest();
        request.setInstanceIds(Arrays.asList(instanceId));  // 设置实例 ID

        try {
            StopInstancesResponse response = client.getAcsResponse(request);
            System.out.println("实例停止成功: " + response.getRequestId());
        } catch (ClientException e) {
            System.out.println("停止实例失败: " + e.getMessage());
        }
    }

    // 在未来某个时刻启动实例
    public void scheduleInstanceStart(String instanceId, long delaySeconds) {
        System.out.println("计划在 " + delaySeconds + " 秒后启动 ECS 实例：" + instanceId);
        // 创建 ScheduledExecutorService 实例来延迟启动
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

        // 延迟指定的秒数后执行实例启动(同步启动会在Flink环境中出问题，不会执行）
        scheduler.schedule(() -> startEcsInstance(instanceId), delaySeconds, TimeUnit.SECONDS);

        // 使用 CompletableFuture 来确保 startEcsInstance 方法异步执行
        /* scheduler.schedule(() -> {
            // 使用异步方式执行 ECS 启动操作，避免阻塞主线程
            CompletableFuture.runAsync(() -> {
                System.out.println("开始启动 ECS 实例：" + instanceId);
                startEcsInstance(instanceId);  // 调用启动实例的方法
            });
        }, delaySeconds, TimeUnit.SECONDS); */

     //添加一个休眠机制不要让其它线程在启动前结束
        try {
            // 让当前线程休眠 5 秒
            Thread.sleep(3000);  // 5000 毫秒 = 5 秒
        } catch (InterruptedException e) {
            // 处理线程中断的异常
            e.printStackTrace();
        }
        // 关闭 scheduler
        scheduler.shutdown();

    }
    // 在未来某个时刻暂停实例
    public void scheduleInstanceStop(String instanceId, long delaySeconds) {
        System.out.println("计划在 " + delaySeconds + " 秒后停止 ECS 实例：" + instanceId);
        // 创建 ScheduledExecutorService 实例来延迟暂停
        java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();

        // 延迟指定的秒数后执行实例暂停
        scheduler.schedule(() -> stopEcsInstance(instanceId), delaySeconds, TimeUnit.SECONDS);

        // 关闭 scheduler
        scheduler.shutdown();
    }

}

