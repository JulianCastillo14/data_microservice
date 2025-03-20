package com.smartuis.module.application.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.domian.repository.MessageRepository;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmqxListener implements MqttCallback {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "admin";
    private static final String TOPIC = "device/messages";

    private final  List<MessageRepository> messageRepository;
    private final ObjectMapper objectMapper;
    private MqttClient client;

    public EmqxListener(List<MessageRepository> messageRepository) {
        this.messageRepository = messageRepository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        initializeMqttClient();
    }

    private void initializeMqttClient() {
        try {
            client = new MqttClient(BROKER_URL, CLIENT_ID);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.setCallback(this);
            client.connect();

            MqttSubscription[] subscriptions = {new MqttSubscription(TOPIC, 1)};
            client.subscribe(subscriptions);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        try {
            String payload = new String(mqttMessage.getPayload());

            Message message = objectMapper.readValue(payload, Message.class);
            messageRepository.forEach(repo->repo.write(message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnected(MqttDisconnectResponse mqttDisconnectResponse) {
        System.out.println("Disconnected from MQTT Broker.");
    }

    @Override
    public void mqttErrorOccurred(MqttException e) {
        System.err.println("️ MQTT Error: " + e.getMessage());
    }

    @Override
    public void deliveryComplete(IMqttToken token) {}

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {}
}
