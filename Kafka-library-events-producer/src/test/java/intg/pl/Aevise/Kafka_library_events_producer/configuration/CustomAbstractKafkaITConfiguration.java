package pl.Aevise.Kafka_library_events_producer.configuration;

import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "library-events")
@TestPropertySource(
        properties = {
                "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "spring.kafka.admin.properties.bootstrap-servers=${spring.embedded.kafka.brokers}"
        }
)
public abstract class CustomAbstractKafkaITConfiguration {

    @Autowired
    protected EmbeddedKafkaBroker embeddedKafkaBroker;
    protected Consumer<Integer, String> consumer;

    protected abstract void setUpKafkaBroker();

    @BeforeEach
    void setUp() {
        setUpKafkaBroker();
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

}
