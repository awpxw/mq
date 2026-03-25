package com.example;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;

import java.util.Date;
import java.util.UUID;

public class MsgUtils {

    private static final String DEFAULT_RETRY_COUNT = "3";

    private static final Long DEFAULT_RETRY_INTERVAL = 5L;

    public static Message createMsg(String retryTimes, Long delayTime, String data) {
        MessageProperties props = new MessageProperties();
        //幂等
        props.setMessageId(UUID.randomUUID().toString().replace("-", ""));
        //延迟
        props.setExpiration(String.valueOf(System.currentTimeMillis() + (delayTime * 1000L)));
        props.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        props.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        props.setTimestamp(new Date());
        //重试
        props.setHeader("retryTimes", retryTimes);
        return MessageBuilder.withBody(data.getBytes())
                .andProperties(props)
                .build();
    }

    public static Message createMsg(String data) {
        return createMsg(DEFAULT_RETRY_COUNT, DEFAULT_RETRY_INTERVAL, data);
    }

}
