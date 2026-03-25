package com.example.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.common.database.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_idempotent")
public class MessageIdempotent extends BaseEntity {

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 消费者名称
     */
    private String consumerName;

    /**
     * 处理状态：0-处理中，1-已处理，2-处理失败
     */
    private Integer status;

    /**
     * 处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processTime;

    /**
     * 过期时间（用于清理）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}