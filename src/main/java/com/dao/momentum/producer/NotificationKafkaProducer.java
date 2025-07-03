package com.dao.momentum.producer;

import com.dao.momentum.dto.NotificationMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, NotificationMessage> kafkaTemplate;

    @Value("${custom.kafka.notification-topic}")
    private String topic;

    public NotificationKafkaProducer(KafkaTemplate<String, NotificationMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(String userId, NotificationMessage message) {
        kafkaTemplate.send(topic, userId, message);
    }
}
