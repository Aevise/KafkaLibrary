package pl.Aevise.Kafka_library_events_consumer.infrastructure.configuration;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@EnableKafka
@Configuration
public class LibraryEventsConsumerConfig {

    //    sets manual acknowledgement
//    private static void setAcknowledgeModeToManual(ConcurrentKafkaListenerContainerFactory<Object, Object> factory) {
//        ContainerProperties properties = factory.getContainerProperties();
//        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
//    }
//
    static int NUMBER_OF_THREADS_FOR_KAFKA_LISTENER = 3;

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(NUMBER_OF_THREADS_FOR_KAFKA_LISTENER);

//        setAcknowledgeModeToManual(factory);

        return factory;
    }
}
