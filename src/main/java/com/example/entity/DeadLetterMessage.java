package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.database.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dead_letter_message")
public class DeadLetterMessage extends BaseEntity {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 关联的任务ID
     */
    private String taskId;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 原队列名称
     */
    private String originalQueue;

    /**
     * 原交换机
     */
    private String originalExchange;

    /**
     * 原路由键
     */
    private String originalRoutingKey;

    /**
     * 消息体内容（JSON格式）
     */
    private String messageBody;

    /**
     * 死亡原因：REJECTED/EXPIRED/DELIVERY_LIMIT
     */
    private String deathReason;

    /**
     * 死亡次数
     */
    private Integer deathCount;

    /**
     * 进入死信队列时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deathTime;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 异常堆栈
     */
    private String stackTrace;

    /**
     * x-death完整信息（JSON格式）
     */
    private String xDeathInfo;

    /**
     * 重试状态：0-未重试，1-已重试，2-重试成功，3-重试失败
     */
    private Integer retryStatus;

    /**
     * 最后重试时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime retryTime;

    /**
     * 重试次数
     */
    private Integer retryCount;
}