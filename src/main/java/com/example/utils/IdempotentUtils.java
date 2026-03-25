package com.example.utils;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.entity.MessageIdempotent;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

public class IdempotentUtils {

    public static Boolean check(Message msg) {
        MessageProperties props = msg.getMessageProperties();
        String messageId = props.getMessageId();
        return ChainWrappers.lambdaQueryChain(MessageIdempotent.class)
                .eq(MessageIdempotent::getMessageId, messageId)
                .count() > 0;
    }

}
