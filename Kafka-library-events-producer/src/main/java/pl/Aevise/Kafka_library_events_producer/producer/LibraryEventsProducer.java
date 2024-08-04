package pl.Aevise.Kafka_library_events_producer.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryEventsProducer {

    @Value("${spring.kafka.topic}")
    public String topic;

    private final KafkaTemplate<Integer, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CompletableFuture<SendResult<Integer, String>> asynchronousSendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        //1. blocking call - get metadata about Kafka cluster
        //2. send message to Kafka cluster
        var completableFuture = kafkaTemplate.send(topic, key, value);
        return completableFuture
                .whenComplete((sendResult, throwable) -> {
                    if(throwable != null){
                        handleFailure(key, value, throwable);
                    }else {
                        handleSuccess(key, value, sendResult);
                    }
                });
    }

    public SendResult<Integer, String> synchronousSendLibraryEvent(LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        //1. blocking call - get metadata about Kafka cluster
        //2. block and wait until message is sent to Kafka cluster
        var sendResult = kafkaTemplate.send(topic, key, value).get(3, TimeUnit.SECONDS);
        handleSuccess(key, value, sendResult);

        return sendResult;
    }

    public CompletableFuture<SendResult<Integer, String>> asynchronousSendLibraryEventWithProducerRecord(LibraryEvent libraryEvent)
            throws JsonProcessingException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        var producerRecord = buildProducerRecord(key, value);

        //1. blocking call - get metadata about Kafka cluster
        //2. send message to Kafka cluster
        var completableFuture = kafkaTemplate.send(producerRecord);
        return completableFuture
                .whenComplete((sendResult, throwable) -> {
                    if(throwable != null){
                        handleFailure(key, value, throwable);
                    }else {
                        handleSuccess(key, value, sendResult);
                    }
                });
    }

    public SendResult<Integer, String> synchronousSendLibraryEventWithProducerRecord(LibraryEvent libraryEvent)
            throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Integer key = libraryEvent.libraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        var producerRecord = buildProducerRecord(key, value);

        //1. blocking call - get metadata about Kafka cluster
        //2. block and wait until message is sent to Kafka cluster
        var sendResult = kafkaTemplate.send(producerRecord).get(3, TimeUnit.SECONDS);
        handleSuccess(key, value, sendResult);

        return sendResult;
    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
        return new ProducerRecord<>(topic, key, value);
    }

    private void handleSuccess(Integer key, String value, SendResult<Integer, String> sendResult) {
        log.info("Message Sent Successfully. Key: [{}], Value: [{}], Partition: [{}]",
                key, value, sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Integer key, String value, Throwable throwable) {
        log.error("Error sending message. Exception: [{}]", throwable.getMessage(), throwable);
    }
}
