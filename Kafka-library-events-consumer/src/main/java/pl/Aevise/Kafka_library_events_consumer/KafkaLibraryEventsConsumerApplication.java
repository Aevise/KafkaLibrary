package pl.Aevise.Kafka_library_events_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KafkaLibraryEventsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaLibraryEventsConsumerApplication.class, args);
    }

}
