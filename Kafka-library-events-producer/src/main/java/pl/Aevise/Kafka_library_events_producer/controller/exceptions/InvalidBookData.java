package pl.Aevise.Kafka_library_events_producer.controller.exceptions;

public class InvalidBookData extends RuntimeException {
    public InvalidBookData(String message) {
        super(message);
    }
}
