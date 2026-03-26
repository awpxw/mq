package com.example.utils;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.entity.MessageIdempotent;
import com.example.entity.Task;
import com.example.enums.TaskStatus;
import com.example.mapper.MessageIdempotentMapper;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

@Component
public class IdempotentUtils {

    @Resource
    private MessageIdempotentMapper messageIdempotentMapper;

    public Boolean check(Message msg) {
        MessageProperties props = msg.getMessageProperties();
        String messageId = props.getMessageId();
        boolean repeat = ChainWrappers.lambdaQueryChain(MessageIdempotent.class)
                .eq(MessageIdempotent::getMessageId, messageId)
                .eq(MessageIdempotent::getStatus, TaskStatus.SUCCESS.getCode())
                .count() > 0;
        boolean running = ChainWrappers.lambdaQueryChain(MessageIdempotent.class)
                .eq(MessageIdempotent::getMessageId, messageId)
                .eq(MessageIdempotent::getStatus, TaskStatus.RUNNING.getCode())
                .count() > 0;
        if (!repeat || !running) {
            MessageIdempotent idempotent = MessageIdempotent.builder()
                    .messageId(props.getMessageId())
                    .taskId(props.getHeader("taskId"))
                    .consumerName("系统")
                    .status(TaskStatus.RUNNING.getCode())
                    .build();
            messageIdempotentMapper.insert(idempotent);
        }
        return repeat;
    }

}
