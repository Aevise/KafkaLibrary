package pl.Aevise.Kafka_library_events_consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.DefaultAbstractKafkaProducerITConfiguration;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;
import pl.Aevise.Kafka_library_events_consumer.business.LibraryEventsService;
import pl.Aevise.Kafka_library_events_consumer.domain.LibraryEventType;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.BookEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.LibraryEventEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa.FailureRecordJpaRepository;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa.LibraryEventsJpaRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static util.EntityFixtures.bookEntity1;

@EmbeddedKafka(topics = {
        "library-events", "library-events.RETRY", "library-events.DLT"
            },
        partitions = 3)
@TestPropertySource(
        inheritProperties = true,
        properties = {"retryListener.startup=false"})
class LibraryEventsConsumerIT extends DefaultAbstractKafkaProducerITConfiguration {

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumerSpy;
    @SpyBean
    LibraryEventsService libraryEventsServiceSpy;

    @Autowired
    LibraryEventsJpaRepository libraryEventsJpaRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    FailureRecordJpaRepository failureRecordJpaRepository;

    @Value("${topics.retry}")
    private String retryTopic;
    @Value("${topics.dlt}")
    private String deadLetterTopic;

    private Consumer<Integer, String> consumer;

    @AfterEach
    void tearDown() {
        libraryEventsJpaRepository.deleteAll();
    }

    @Test
    void publishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        String expectedBookId = "123";
        String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":" + expectedBookId + ",\"bookName\":\"Random Book\",\"bookAuthor\":\"Random Author\"}}";
        //synchronous
        kafkaTemplate.sendDefault(json).get();

        //when
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(libraryEventsConsumerSpy, times(2)).consumeRecord(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(2)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEventEntity> allLibraryEvents = libraryEventsJpaRepository.findAll();
        assert allLibraryEvents.size() == 1;
        allLibraryEvents.forEach(
                libraryEvent -> {
                    assert libraryEvent.getLibraryEventId() != null;
                    assertEquals(Integer.parseInt(expectedBookId), libraryEvent.getBook().getBookId());
                }
        );
    }

    @Test
    public void publishUpdateLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        String bookId = "123";
        String json = "{\"libraryEventId\":null,\"libraryEventType\":\"NEW\",\"book\":{\"bookId\":" + bookId + ",\"bookName\":\"Random Book\",\"bookAuthor\":\"Random Author\"}}";
        LibraryEventEntity libraryEvent = objectMapper.readValue(json, LibraryEventEntity.class);
        libraryEvent.getBook().setLibraryEventEntity(libraryEvent);
        libraryEventsJpaRepository.save(libraryEvent);

        BookEntity newBook = bookEntity1();
        newBook.setBookId(Integer.valueOf(bookId));
        libraryEvent.setLibraryEventType(LibraryEventType.UPDATE);
        libraryEvent.setBook(newBook);
        String updatedJson = objectMapper.writeValueAsString(libraryEvent);

        //when
        kafkaTemplate.sendDefault(libraryEvent.getLibraryEventId(), updatedJson).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        Optional<LibraryEventEntity> updatedLibraryEvent = libraryEventsJpaRepository.findById(libraryEvent.getLibraryEventId());

        assertTrue(updatedLibraryEvent.isPresent());
        LibraryEventEntity libraryEventEntity = updatedLibraryEvent.get();
        assertEquals(newBook.getBookId(), libraryEventEntity.getBook().getBookId());
        assertEquals(newBook.getBookName(), libraryEventEntity.getBook().getBookName());
        assertEquals(newBook.getBookAuthor(), libraryEventEntity.getBook().getBookAuthor());
    }

    @Test
    public void publishUpdateLibraryEventWithoutLibraryEvent() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        String bookId = "123";
        String json = "{\"libraryEventId\":null,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":" + bookId + ",\"bookName\":\"Random Book\",\"bookAuthor\":\"Random Author\"}}";

        //when
        kafkaTemplate.sendDefault(json).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        //then
        verify(libraryEventsConsumerSpy, times(1)).consumeRecord(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        HashMap<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group2", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, deadLetterTopic);

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, deadLetterTopic);
        String receivedValue = consumerRecord.value();

        System.out.println("consumer Records is : " + receivedValue);
        assertEquals(json, receivedValue);
    }

    @Test
    public void publishUpdateLibraryEventWithoutLibraryEvent_FailureRecord() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        String bookId = "123";
        String json = "{\"libraryEventId\":null,\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":" + bookId + ",\"bookName\":\"Random Book\",\"bookAuthor\":\"Random Author\"}}";

        //when
        kafkaTemplate.sendDefault(json).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        //then
        verify(libraryEventsConsumerSpy, times(1)).consumeRecord(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        long count = failureRecordJpaRepository.count();
        assertEquals(1, count);
        failureRecordJpaRepository.findAll()
                .forEach(failureRecordEntity -> {
                    System.out.println("Failure record: " + failureRecordEntity);
                });
    }

    @Test
    public void publishUpdateLibraryEventWithWrongValue() throws JsonProcessingException, ExecutionException, InterruptedException {
        //given
        String libraryEventId = "999";
        String json = "{\"libraryEventId\":" + libraryEventId + ",\"libraryEventType\":\"UPDATE\",\"book\":{\"bookId\":123,\"bookName\":\"Random Book\",\"bookAuthor\":\"Random Author\"}}";

        //when
        kafkaTemplate.sendDefault(json).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(5, TimeUnit.SECONDS);

        //then
        verify(libraryEventsConsumerSpy, times(3)).consumeRecord(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(3)).processLibraryEvent(isA(ConsumerRecord.class));

        HashMap<String, Object> configs = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
        consumer = new DefaultKafkaConsumerFactory<>(configs, new IntegerDeserializer(), new StringDeserializer()).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, retryTopic);

        ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, retryTopic);
        String receivedValue = consumerRecord.value();

        System.out.println("consumer Records is : " + receivedValue);
        assertEquals(json, receivedValue);
    }
}