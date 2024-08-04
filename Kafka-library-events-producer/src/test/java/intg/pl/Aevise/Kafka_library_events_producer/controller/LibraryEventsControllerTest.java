package pl.Aevise.Kafka_library_events_producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import pl.Aevise.Kafka_library_events_producer.configuration.AbstractKafkaITConfiguration;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;
import pl.Aevise.Kafka_library_events_producer.util.TestUtils;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.Aevise.Kafka_library_events_producer.controller.LibraryEventsController.LIBRARY_EVENT_ASYNC;
import static pl.Aevise.Kafka_library_events_producer.controller.LibraryEventsController.LIBRARY_EVENT_SYNC;
import static pl.Aevise.Kafka_library_events_producer.util.POJOFixtures.libraryEventRecord;

class LibraryEventsControllerTest extends AbstractKafkaITConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void checkThatLibraryEventIsSentAsync() {
        //given
        LibraryEvent expectedLibraryEvent = libraryEventRecord();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        var httpEntity = new HttpEntity<>(expectedLibraryEvent, httpHeaders);

        //when
        ResponseEntity<LibraryEvent> responseEntity = restTemplate
                .exchange(LIBRARY_EVENT_ASYNC, HttpMethod.POST, httpEntity, LibraryEvent.class);

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(60));

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assert consumerRecords.count() >= 1;
        consumerRecords
                .forEach(record -> {
                    LibraryEvent libraryEvent = TestUtils.parseLibraryEventRecord(objectMapper, record.value());
                    assertEquals(libraryEvent, expectedLibraryEvent);
                });
    }

    @Test
    void checkThatLibraryEventIsSentSync() {
        //given
        LibraryEvent expectedLibraryEvent = libraryEventRecord();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        var httpEntity = new HttpEntity<>(expectedLibraryEvent, httpHeaders);

        //when
        ResponseEntity<LibraryEvent> responseEntity = restTemplate
                .exchange(LIBRARY_EVENT_SYNC, HttpMethod.POST, httpEntity, LibraryEvent.class);

        ConsumerRecords<Integer, String> consumerRecords = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(60));

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assert consumerRecords.count() >= 1;
        consumerRecords
                .forEach(record -> {
                    LibraryEvent libraryEvent = TestUtils.parseLibraryEventRecord(objectMapper, record.value());
                    assertEquals(libraryEvent, expectedLibraryEvent);
                });
    }
}