package com.example.comsumer;

import com.example.utils.RetryUtils;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
public class TaskConsumer {

    @Resource
    private RetryUtils retryUtils;

    @RabbitHandler
    @RabbitListener(queues = "taskQueue")
    public void handleTask(Message message, Channel channel) {
        try {
            processTak(message);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (Exception e) {
            retryUtils.retry(message);
        }


    }



    @RabbitHandler
    @RabbitListener(queues = "bakQueue")
    public void handleBak() {

    }

    @Transactional
    public void processTak(Message message) {


    }


}
