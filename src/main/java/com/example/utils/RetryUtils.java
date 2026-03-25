package com.example.utils;

import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.example.entity.TaskExecutionLog;
import com.example.enums.TaskType;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Component
public class RetryUtils {

    @Resource
    private RabbitTemplate rabbitTemplate;

    private static final Integer MAX_RETRY_COUNT = 3;

    public void retry(Message msg) {
        MessageProperties props = msg.getMessageProperties();
        Boolean enableRetry = props.getHeader("enableRetry");
        if (enableRetry != null && enableRetry) {
            Integer type = Integer.parseInt(props.getType());
            String queue = TaskType.fromCode(type);
            String messageId = props.getMessageId();
            int retry = props.getHeader("retryTimes");
            if (retry < MAX_RETRY_COUNT) {
                //重新投递
                props.setHeader("retryTimes", ++retry);
                rabbitTemplate.convertAndSend(queue + "Exchange", queue, msg);
            } else {
                //放入死信队列
                rabbitTemplate.convertAndSend(queue + "DeadExchange", queue, msg);
            }
            //执行次数+1
            ChainWrappers.lambdaUpdateChain(TaskExecutionLog.class)
                    .eq(TaskExecutionLog::getTaskId, messageId)
                    .set(TaskExecutionLog::getExecutionNo, retry)
                    .update();
        }

    }


}
