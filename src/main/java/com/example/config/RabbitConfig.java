package com.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitConfig {

    @Bean("exchange")
    public DirectExchange exchange() {
        return new DirectExchange("exchange", true, false);
    }

    @Bean("taskQueue")
    public Queue taskQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "deadExchange");
        arguments.put("x-dead-letter-routing-key", "task");
        return new Queue("taskQueue", true, false, false, arguments);
    }

    @Bean("bakQueue")
    public Queue bakQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "deadExchange");
        arguments.put("x-dead-letter-routing-key", "bak");
        return new Queue("bakQueue", true, false, false, arguments);
    }

    @Bean("taskBinding")
    public Binding taskBinding() {
        return new Binding("taskQueue", Binding.DestinationType.QUEUE, "exchange", "task", null);
    }

    @Bean("bakBinding")
    public Binding bakBinding() {
        return new Binding("bakQueue", Binding.DestinationType.QUEUE, "exchange", "bak", null);
    }

    //==================== 死信交换机和队列 ====================

    @Bean("deadExchange")
    public DirectExchange deadExchange() {
        return new DirectExchange("deadExchange", true, false);
    }

    @Bean("taskDeadQueue")
    public Queue taskDeadQueue() {
        return new Queue("taskDeadQueue", true);
    }

    @Bean("bakDeadQueue")
    public Queue bakDeadQueue() {
        return new Queue("bakDeadQueue", true);
    }

    @Bean("taskDeadBinding")
    public Binding taskDeadBinding() {
        return new Binding("taskDeadQueue", Binding.DestinationType.QUEUE, "deadExchange", "task", null);
    }

    @Bean("bakDeadBinding")
    public Binding bakDeadBinding() {
        return new Binding("bakDeadQueue", Binding.DestinationType.QUEUE, "deadExchange", "bak", null);
    }

    //==================== 延迟队列 : 投递时设置消息过期时间，即为延迟时间 ====================

    @Bean("delayExchange")
    public DirectExchange delayExchange() {
        return new DirectExchange("delayExchange", true, false);
    }

    @Bean("taskDelayQueue")
    public Queue taskDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "deadExchange");
        arguments.put("x-dead-letter-routing-key", "task");
        return new Queue("taskDelayQueue", true, false, false, arguments);
    }

    @Bean("bakDelayQueue")
    public Queue bakDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "deadExchange");
        arguments.put("x-dead-letter-routing-key", "bak");
        return new Queue("bakDelayQueue", true, false, false, arguments);
    }

    @Bean("taskDelayBinding")
    public Binding taskDelayBinding() {
        return new Binding("taskDelayQueue", Binding.DestinationType.QUEUE, "delayExchange", "task", null);
    }

    @Bean("bakDeadBinding")
    public Binding bakDelayBinding() {
        return new Binding("bakDelayQueue", Binding.DestinationType.QUEUE, "delayExchange", "bak", null);
    }

}
