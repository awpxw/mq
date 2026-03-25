package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.database.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task")
public class Task extends BaseEntity {

    /**
     * 任务ID（业务唯一标识）
     */
    private String taskId;

    /**
     * RabbitMQ消息ID
     */
    private String messageId;

    /**
     * 任务类型：EMAIL/SMS/CALC/REPORT/BACKUP
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
     * 当前阶段：PENDING/CONSUMING/PROCESSING/RETRYING
     */
    private String currentStage;

    /**
     * 任务数据（JSON格式）
     */
    private String taskData;

    /**
     * 任务执行结果（JSON格式）
     */
    private String taskResult;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetry;

    /**
     * 延迟毫秒数
     */
    private Long delayMs;

    /**
     * 计划执行时间（延迟任务）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime executeTime;

    /**
     * 开始执行时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 队列名称
     */
    private String queueName;

    /**
     * 交换机名称
     */
    private String exchangeName;

    /**
     * 路由键
     */
    private String routingKey;

    /**
     * 链路追踪ID
     */
    private String traceId;

    /**
     * 批次ID（批量提交时）
     */
    private String batchId;

}