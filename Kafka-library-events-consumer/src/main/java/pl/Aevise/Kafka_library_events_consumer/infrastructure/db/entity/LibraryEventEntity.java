package pl.Aevise.Kafka_library_events_consumer.infrastructure.db.entity;

import jakarta.persistence.*;
import lombok.*;
import pl.Aevise.Kafka_library_events_consumer.domain.LibraryEventType;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LibraryEventEntity {

    @Id
    @GeneratedValue
    private Integer libraryEventId;

    @Enumerated(EnumType.STRING)
    private LibraryEventType libraryEventType;

    @OneToOne(
            mappedBy = "libraryEventEntity",
            cascade = CascadeType.ALL
    )
    @ToString.Exclude
    private BookEntity book;
}
