package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.database.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("queue_monitor_history")
public class QueueMonitorHistory extends BaseEntity {

    /**
     * 队列名称
     */
    private String queueName;

    /**
     * 虚拟主机
     */
    private String vhost;

    /**
     * 待消费消息数
     */
    private Integer messagesReady;

    /**
     * 未确认消息数
     */
    private Integer messagesUnacknowledged;

    /**
     * 总消息数
     */
    private Integer messagesTotal;

    /**
     * 消费者数量
     */
    private Integer consumers;

    /**
     * 发布速率（条/秒）
     */
    private BigDecimal publishRate;

    /**
     * 消费速率（条/秒）
     */
    private BigDecimal deliverRate;

    /**
     * 确认速率（条/秒）
     */
    private BigDecimal ackRate;

    /**
     * 内存使用（字节）
     */
    private Long memoryUsed;

}