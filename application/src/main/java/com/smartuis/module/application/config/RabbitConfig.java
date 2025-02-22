package com.smartuis.module.application.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public FanoutExchange topicExchange(){
        return new FanoutExchange("device-messages");
    }

    @Bean
    public Queue anonQueue(){
        return new AnonymousQueue();
    }

    @Bean
    public Binding binding(FanoutExchange fanoutExchange, Queue anonQueue){
        return BindingBuilder.bind(anonQueue).to(fanoutExchange);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter jsonConverter = new Jackson2JsonMessageConverter();
        return jsonConverter;
    }
}
