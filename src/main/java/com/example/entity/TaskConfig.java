package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.database.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_config")
public class TaskConfig extends BaseEntity {

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务类型名称
     */
    private String taskTypeName;

    /**
     * 默认优先级
     */
    private Integer defaultPriority;

    /**
     * 默认最大重试次数
     */
    private Integer defaultMaxRetry;

    /**
     * 默认超时时间（毫秒）
     */
    private Integer defaultTimeout;

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
     * 死信队列名称
     */
    private String dlqQueueName;

    /**
     * 死信交换机名称
     */
    private String dlqExchangeName;

    /**
     * 是否启用：0-禁用，1-启用
     */
    private Integer enabled;

    /**
     * 描述
     */
    private String description;
}