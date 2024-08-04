package pl.Aevise.Kafka_library_events_producer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import pl.Aevise.Kafka_library_events_producer.configuration.AbstractKafkaITConfiguration;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.Aevise.Kafka_library_events_producer.controller.LibraryEventsController.LIBRARY_EVENT_ASYNC;
import static pl.Aevise.Kafka_library_events_producer.util.POJOFixtures.libraryEventRecord;

class LibraryEventsControllerTest extends AbstractKafkaITConfiguration {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void checkThatLibraryEventIsSentAsync() {
        //given
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        var httpEntity = new HttpEntity<>(libraryEventRecord(), httpHeaders);

        //when
        ResponseEntity<LibraryEvent> responseEntity = restTemplate
                .exchange(LIBRARY_EVENT_ASYNC, HttpMethod.POST, httpEntity, LibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void checkThatLibraryEventIsSentSync() {
        //given
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        var httpEntity = new HttpEntity<>(libraryEventRecord(), httpHeaders);

        //when
        ResponseEntity<LibraryEvent> responseEntity = restTemplate
                .exchange(LIBRARY_EVENT_ASYNC, HttpMethod.POST, httpEntity, LibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
}