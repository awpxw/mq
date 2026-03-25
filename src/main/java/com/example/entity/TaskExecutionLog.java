package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.database.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_execution_log")
@Builder
public class TaskExecutionLog extends BaseEntity {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 执行次数（第几次执行）
     */
    private Integer executionNo;

    /**
     * 执行状态：0-开始，1-成功，2-失败
     */
    private Integer status;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 输入数据（JSON格式）
     */
    private String inputData;

    /**
     * 输出数据（JSON格式）
     */
    private String outputData;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 异常堆栈
     */
    private String stackTrace;

    /**
     * 消费者标签
     */
    private String consumerTag;

    /**
     * 队列名称
     */
    private String queueName;

}