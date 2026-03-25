package com.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class RabbitConfig {

    @Bean("taskQueue")
    public Queue taskQueue() {
        return new Queue("taskQueue",true);
    }

    @Bean("taskExchange")
    public DirectExchange taskExchange() {
        return new DirectExchange("taskExchange",true,false);
    }

    @Bean("taskBinding")
    public Binding taskBinding() {
        return new Binding("taskQueue",Binding.DestinationType.QUEUE,"taskExchange","task",null);
    }

    @Bean("bakQueue")
    public Queue bakQueue() {
        return new Queue("bakQueue",true);
    }

    @Bean("bakExchange")
    public DirectExchange bakExchange() {
        return new DirectExchange("bakExchange",true,false);
    }

    @Bean("bakBinding")
    public Binding bakBinding() {
        return new Binding("bakQueue",Binding.DestinationType.QUEUE,"bakExchange","bak",null);
    }

}
