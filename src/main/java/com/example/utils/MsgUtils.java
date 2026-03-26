package com.example.utils;

import com.example.entity.DeadLetterMessage;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;

import java.util.Date;
import java.util.UUID;

public class MsgUtils {

    private static final String DEFAULT_RETRY_COUNT = "3";

    private static final Long DEFAULT_RETRY_INTERVAL = 5L;

    public static Message createMsg(Long taskId, Boolean enableRetry, String retryTimes, Long delayTime, String data, String type) {
        MessageProperties props = new MessageProperties();
        //幂等
        props.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        props.setHeader("taskId", taskId);
        //延迟
        props.setExpiration(String.valueOf(System.currentTimeMillis() + (delayTime * 1000L)));
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setTimestamp(new Date());
        //消息类型
        props.setType(type);
        return MessageBuilder.withBody(data.getBytes())
                .andProperties(props)
                .build();
    }

    public static Message createMsg(Long taskId, String type, String data) {
        return createMsg(taskId, true, DEFAULT_RETRY_COUNT, DEFAULT_RETRY_INTERVAL, data, type);
    }

    public static Message createDelayMsg(Long taskId, String type, String data, Long delayTime) {
        return createMsg(taskId, true, DEFAULT_RETRY_COUNT, delayTime, data, type);
    }

    public static Message createMsgFromDead(DeadLetterMessage dMsg) {
        MessageProperties props = new MessageProperties();
        //幂等
        props.setMessageId(dMsg.getMessageId());
        props.setHeader("taskId", dMsg.getTaskId());
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setTimestamp(new Date());
        //消息类型
        props.setType(dMsg.getTaskType());
        return MessageBuilder.withBody(dMsg.getMessageBody().getBytes())
                .andProperties(props)
                .build();
    }
}
