package com.module.service.impl;

import com.smartuis.module.domian.entity.Message;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class AmqpRequeueService {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    private Queue anonymousQueue;

    public AmqpRequeueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = new RabbitAdmin(rabbitTemplate.getConnectionFactory());
        this.anonymousQueue = new AnonymousQueue();
    }

    public void requeue(Message message) {
        try {
            String targetExchange = message.getHeader().getTopic();
            FanoutExchange fanoutExchange = new FanoutExchange(targetExchange);
            rabbitAdmin.declareQueue(anonymousQueue);
            rabbitAdmin.declareExchange(fanoutExchange);
            rabbitAdmin.declareBinding(BindingBuilder.bind(anonymousQueue).to(fanoutExchange));
            rabbitTemplate.convertAndSend(targetExchange, "", message);

            System.out.println("Mensaje reencolado en RabbitMQ en el exchange '" + targetExchange + "': " + message);
        } catch (Exception e) {
            System.err.println("Error al reencolar el mensaje en RabbitMQ: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
