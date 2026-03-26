package com.example.dto;

import lombok.Data;


@Data
public class TaskSubmitRequest {

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 优先级 1-10
     */
    private Integer priority;

    /**
     * 任务数据（JSON字符串）
     */
    private String taskData;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 延迟毫秒数
     */
    private Long delayMs;

    /**
     * 批次ID
     */
    private String batchId;

}