package pl.Aevise.Kafka_library_events_producer.util;

import pl.Aevise.Kafka_library_events_producer.domain.Book;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEventType;

public class POJOFixtures {

    public static Book bookRecord() {
        return new Book(123, "Test Book", "Test Author");
    }

    public static Book bookRecordWithInvalidValue() {
        return new Book(null, "", "Test Author");
    }

    public static LibraryEvent libraryEventRecord() {
        return new LibraryEvent(
                null,
                LibraryEventType.NEW,
                bookRecord()
        );
    }

    public static LibraryEvent libraryEventRecordWithLibraryEventId() {
        return new LibraryEvent(
                123,
                LibraryEventType.NEW,
                bookRecord()
        );
    }

    public static LibraryEvent libraryEventUpdate() {
        return new LibraryEvent(
                123,
                LibraryEventType.UPDATE,
                bookRecord()
        );
    }

    public static LibraryEvent libraryEventUpdateWithNullLibraryEventId() {
        return new LibraryEvent(
                null,
                LibraryEventType.UPDATE,
                bookRecord()
        );
    }

    public static LibraryEvent libraryEventUpdateWithInvalidBook() {
        return new LibraryEvent(
                null,
                LibraryEventType.UPDATE,
                bookRecordWithInvalidValue()
        );
    }
}
