package util;

import pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity.BookEntity;

public class EntityFixtures {

    public static BookEntity bookEntity1() {
        return BookEntity.builder()
                .bookAuthor("Test Author 1")
                .bookName("Test Book 1")
                .build();
    }
}
