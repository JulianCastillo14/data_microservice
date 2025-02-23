package com.smartuis.module.application.mqtt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartuis.module.domian.entity.Message;
import com.smartuis.module.persistence.repository.InfluxRepository;
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

@Component
public class MqttListener implements MqttCallback {
    private static final String BROKER_URL = "tcp://localhost:1883";
    private static final String CLIENT_ID = "admin";
    private static final String TOPIC = "device/messages";

    private final InfluxRepository influxRepository;
    private final ObjectMapper objectMapper;
    private MqttClient client;

    public MqttListener(InfluxRepository influxRepository) {
        this.influxRepository = influxRepository;
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

            System.out.println("Subscribed to topic: " + TOPIC);
        } catch (MqttException e) {
            System.err.println("Error initializing MQTT Client: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        try {
            String payload = new String(mqttMessage.getPayload());
            System.out.println("Received message on topic [" + topic + "]: " + payload);

            Message message = objectMapper.readValue(payload, Message.class);
            System.out.println("Parsed message: " + message);

            influxRepository.write(message);
        } catch (Exception e) {
            System.err.printf("Error processing message: %s\n", e.getMessage());
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
        System.out.println("Connected to MQTT Broker: " + serverURI);
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {}
}
