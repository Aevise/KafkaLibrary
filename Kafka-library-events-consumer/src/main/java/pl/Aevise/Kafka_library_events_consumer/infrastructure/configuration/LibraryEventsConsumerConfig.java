package pl.Aevise.Kafka_library_events_consumer.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.List;

@EnableKafka
@Configuration
@Slf4j
public class LibraryEventsConsumerConfig {

    //    sets manual acknowledgement
//    private static void setAcknowledgeModeToManual(ConcurrentKafkaListenerContainerFactory<Object, Object> factory) {
//        ContainerProperties properties = factory.getContainerProperties();
//        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
//    }
//
    static int NUMBER_OF_THREADS_FOR_KAFKA_LISTENER = 3;

    public DefaultErrorHandler errorHandler(){
        //retry failed record twice, delay 1 second
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);

        //gradually increase time interval between retries
        ExponentialBackOffWithMaxRetries exponentialBackOff = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOff.setInitialInterval(1000L);
        exponentialBackOff.setMultiplier(2.0);


        DefaultErrorHandler errorHandler = new DefaultErrorHandler(exponentialBackOff);
        setNotRetryableExceptions(errorHandler);
        setCustomRetryListener(errorHandler);

        return errorHandler;
    }

    private static void setNotRetryableExceptions(DefaultErrorHandler errorHandler) {
        List<Class<IllegalArgumentException>> nonRetryableExceptions = List.of(
                IllegalArgumentException.class
        );

        nonRetryableExceptions
                .forEach(errorHandler::addNotRetryableExceptions);
    }

    private static void setCustomRetryListener(DefaultErrorHandler errorHandler) {
        errorHandler
                .setRetryListeners(
                        (record, ex, deliveryAttempt) -> {
                            log.info("Failed record in Retry Listener. Delivery attempt: {}, Exception : {}", deliveryAttempt, ex.getMessage());
                        }
                );
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setConcurrency(NUMBER_OF_THREADS_FOR_KAFKA_LISTENER);
        factory.setCommonErrorHandler(errorHandler());

//        setAcknowledgeModeToManual(factory);

        return factory;
    }
}
