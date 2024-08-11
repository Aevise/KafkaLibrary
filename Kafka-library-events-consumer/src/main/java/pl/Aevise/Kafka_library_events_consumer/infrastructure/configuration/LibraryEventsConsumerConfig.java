package pl.Aevise.Kafka_library_events_consumer.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.util.backoff.FixedBackOff;
import pl.Aevise.Kafka_library_events_consumer.business.FailureService;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.configuration.utils.RecordStatus;

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

    @Autowired
    KafkaTemplate kafkaTemplate;

    @Autowired
    FailureService failureService;

    @Value("${topics.retry}")
    private String retryTopic;

    @Value("${topics.dlt}")
    private String deadLetterTopic;

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

    public DeadLetterPublishingRecoverer publishingRecover() {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (consumerRecord, exception) -> {
                    if (exception.getCause() instanceof RecoverableDataAccessException) {
                        return new TopicPartition(retryTopic, consumerRecord.partition());
                    }else {
                        return new TopicPartition(deadLetterTopic, consumerRecord.partition());
                    }
                }
        );
    }

    @SuppressWarnings("unchecked")
    private ConsumerRecordRecoverer getConsumerRecordRecoverer() {
        return (consumerRecord, e) -> {
            var record = (ConsumerRecord<Integer, String>) consumerRecord;
            if (e.getCause() instanceof RecoverableDataAccessException) {
                //recovery logic
                log.info("Inside Recovery");
                failureService.saveFailedRecord(record, e, RecordStatus.RETRY.toString());
            } else {
                //non-recovery logic
                log.info("Inside Non-Recovery");
                failureService.saveFailedRecord(record, e, RecordStatus.DEAD.toString());
            }
        };
    }

    public DefaultErrorHandler errorHandler() {
        //retry failed record twice, delay 1 second
        FixedBackOff fixedBackOff = new FixedBackOff(1000L, 2);

        //gradually increase time interval between retries
        ExponentialBackOffWithMaxRetries exponentialBackOff = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOff.setInitialInterval(1000L);
        exponentialBackOff.setMultiplier(2.0);


        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                getConsumerRecordRecoverer(),
//                publishingRecover(),
                exponentialBackOff);

        setNotRetryableExceptions(errorHandler);
        setCustomRetryListener(errorHandler);

        return errorHandler;
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
