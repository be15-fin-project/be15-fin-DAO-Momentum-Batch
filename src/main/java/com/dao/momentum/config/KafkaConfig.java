package com.dao.momentum.config;

import com.dao.momentum.dto.NotificationMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    /**
     * Kafka용 ObjectMapper 생성
     * - LocalDateTime → ISO-8601 문자열로 직렬화되도록 설정
     */
    private ObjectMapper kafkaObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // LocalDateTime 지원
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 문자열로 직렬화
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // null 제거
        return mapper;
    }

    /* ProducerFactory 설정
     * - Kafka에 메시지를 보내는(produce) 역할을 수행
     * - 서버 주소, 키/값 직렬화 방식 등을 지정
     */
    @Bean
    public ProducerFactory<String, NotificationMessage> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // 카프카 클러스터의 호스트:포트 (Docker Compose나 외부 서버 등)
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // 메시지 키를 String 으로 직렬화
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // 메시지 값을 JSON 으로 직렬화 (NotificationMessage 객체 → JSON 문자열)
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // 재시도 설정
        config.put(ProducerConfig.RETRIES_CONFIG, 5); // 최대 5회 재시도

        // 전송 타임아웃 설정 (네트워크 장애 대비)
        config.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000); // 3초 제한
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);   // 1초 간격

        // 커스텀 ObjectMapper를 가진 JsonSerializer 사용
        JsonSerializer<NotificationMessage> jsonSerializer = new JsonSerializer<>(kafkaObjectMapper());
        jsonSerializer.setAddTypeInfo(false); // @class 제거

        return new DefaultKafkaProducerFactory<>(config, new StringSerializer(), jsonSerializer);
    }

    /* KafkaTemplate 빈 등록
     * - 메시지를 보내기 위한 편리한 API 제공
     * - producerFactory를 통해 생성된 프로듀서를 내부에서 사용
     */
    @Bean
    public KafkaTemplate<String, NotificationMessage> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}