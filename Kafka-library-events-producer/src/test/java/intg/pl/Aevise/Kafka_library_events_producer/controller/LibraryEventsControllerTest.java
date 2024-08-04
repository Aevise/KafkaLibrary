package pl.Aevise.Kafka_library_events_producer.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;
import pl.Aevise.Kafka_library_events_producer.util.POJOFixtures;

import static org.junit.jupiter.api.Assertions.*;
import static pl.Aevise.Kafka_library_events_producer.controller.LibraryEventsController.LIBRARY_EVENT;
import static pl.Aevise.Kafka_library_events_producer.util.POJOFixtures.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(
        properties = {
                "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.admin.properties.bootstrap-servers=${spring.embedded.kafka.brokers}"
        }
)
class LibraryEventsControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void checkThatLibraryEventIsSent() {
        //given
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        var httpEntity = new HttpEntity<>(libraryEventRecord(), httpHeaders);

        //when
        ResponseEntity<LibraryEvent> responseEntity = restTemplate
                .exchange(LIBRARY_EVENT, HttpMethod.POST, httpEntity, LibraryEvent.class);

        //then
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }
}