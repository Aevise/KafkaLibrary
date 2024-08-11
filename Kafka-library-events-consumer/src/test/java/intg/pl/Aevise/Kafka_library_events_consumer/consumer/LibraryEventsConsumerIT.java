package pl.Aevise.Kafka_library_events_consumer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import configuration.DefaultAbstractKafkaProducerITConfiguration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import pl.Aevise.Kafka_library_events_consumer.business.LibraryEventsService;
import pl.Aevise.Kafka_library_events_consumer.domain.LibraryEventType;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.BookEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.LibraryEventEntity;
import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.jpa.LibraryEventsJpaRepository;

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

class LibraryEventsConsumerIT extends DefaultAbstractKafkaProducerITConfiguration {

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumerSpy;
    @SpyBean
    LibraryEventsService libraryEventsServiceSpy;

    @Autowired
    LibraryEventsJpaRepository libraryEventsJpaRepository;
    @Autowired
    ObjectMapper objectMapper;

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
        verify(libraryEventsConsumerSpy, times(1)).consumeRecord(isA(ConsumerRecord.class));
        verify(libraryEventsServiceSpy, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

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
    }
}