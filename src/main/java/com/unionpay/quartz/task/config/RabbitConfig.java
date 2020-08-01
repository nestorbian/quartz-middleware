package com.unionpay.quartz.task.config;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rabbitmq.client.Channel;
import com.unionpay.quartz.task.Constants;

@Configuration
public class RabbitConfig {
	
//	@Value("${spring.rabbitmq.host}")
//	private String host;
//	
//	@Value("${spring.rabbitmq.port}")
//	private int port;
//	
//	@Value("${spring.rabbitmq.username}")
//	private String username;
//	
//	@Value("${spring.rabbitmq.password}")
//	private String password;
	
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(Constants.EXCAHNGE_NAME);
    }
    
    @Bean
    public Connection connection(ConnectionFactory connectionFactory) throws IOException, TimeoutException {
    	Connection connection = connectionFactory.createConnection();
    	return connection;
    }
    
    @Bean
    public Channel channel(Connection connection) throws IOException {
    	Channel channel = connection.createChannel(false);
    	return channel;
    }
    
}
