package pl.Aevise.Kafka_library_events_producer.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;

public class TestUtils {
    public static LibraryEvent parseLibraryEventRecord(ObjectMapper objectMapper, String json) {
        try {
            return objectMapper.readValue(json, LibraryEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
