package com.example.config;

import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

@Configuration
@SuppressWarnings("all")
public class RabbitManageConfig {

    //Management HTTP Client
    @Bean
    public Client rabbitManagementClient(
            @Value("${spring.rabbitmq.host:localhost}") String host,
            @Value("${spring.rabbitmq.port:15672}") int port,
            @Value("${spring.rabbitmq.username:guest}") String username,
            @Value("${spring.rabbitmq.password:guest}") String password) throws MalformedURLException, URISyntaxException {
        String baseUrl = "http://" + host + ":" + port + "/api/";
        return new Client(new ClientParameters()
                .url(baseUrl)
                .username(username)
                .password(password));
    }

}
