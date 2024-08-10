package pl.Aevise.Kafka_library_events_consumer.config;

import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@EnableKafka
@Configuration
public class LibraryEventsConsumerConfig {

//    sets manual acknowledgement
//    private static void setAcknowledgeModeToManual(ConcurrentKafkaListenerContainerFactory<Object, Object> factory) {
//        ContainerProperties properties = factory.getContainerProperties();
//        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
//    }
//
//    @Bean
//    ConcurrentKafkaListenerContainerFactory<?, ?> concurrentKafkaListenerContainerFactory(
//            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
//            ConsumerFactory<Object, Object> kafkaConsumerFactory
//    ) {
//        var factory = new ConcurrentKafkaListenerContainerFactory<>();
//        configurer.configure(factory, kafkaConsumerFactory);
//
//        setAcknowledgeModeToManual(factory);
//
//        return factory;
//    }
}
