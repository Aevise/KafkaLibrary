package pl.Aevise.Kafka_library_events_producer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.Aevise.Kafka_library_events_producer.domain.LibraryEvent;
import pl.Aevise.Kafka_library_events_producer.producer.LibraryEventsProducer;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.Aevise.Kafka_library_events_producer.controller.LibraryEventsController.LIBRARY_EVENT_ASYNC;
import static pl.Aevise.Kafka_library_events_producer.util.POJOFixtures.bookRecordWithInvalidValue;
import static pl.Aevise.Kafka_library_events_producer.util.POJOFixtures.libraryEventRecord;

@WebMvcTest(LibraryEventsController.class)
class LibraryEventsControllerMockMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventsProducer libraryEventsProducer;

    @Test
    void checkThatLibraryEventWithValidDataIsSentSuccessfullyAsync() throws Exception {
        //given
        String json = objectMapper.writeValueAsString(libraryEventRecord());

        //when
        when(libraryEventsProducer.asynchronousSendLibraryEventWithProducerRecord(isA(LibraryEvent.class)))
                .thenReturn(null);

        ResultActions result = mockMvc.
                perform(MockMvcRequestBuilders.post(LIBRARY_EVENT_ASYNC)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isCreated());
    }

    @Test
    void checkThatExceptionIsThrownWhenTryingToSendInvalidDataAsync() throws Exception {
        //given
        String json = objectMapper.writeValueAsString(bookRecordWithInvalidValue());
        String expectedErrorMessage = "book - must not be null";

        //when
        when(libraryEventsProducer.asynchronousSendLibraryEventWithProducerRecord(isA(LibraryEvent.class)))
                .thenReturn(null);

        ResultActions result = mockMvc.
                perform(MockMvcRequestBuilders.post(LIBRARY_EVENT_ASYNC)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().is4xxClientError());
        result.andExpect(content().string(expectedErrorMessage));
    }

    @Test
    void checkThatLibraryEventWithValidDataIsSentSuccessfullySync() throws Exception {
        //given
        String json = objectMapper.writeValueAsString(libraryEventRecord());

        //when
        when(libraryEventsProducer.synchronousSendLibraryEventWithProducerRecord(isA(LibraryEvent.class)))
                .thenReturn(null);

        ResultActions result = mockMvc.
                perform(MockMvcRequestBuilders.post(LIBRARY_EVENT_ASYNC)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isCreated());
    }

    @Test
    void checkThatExceptionIsThrownWhenTryingToSendInvalidDataSync() throws Exception {
        //given
        String json = objectMapper.writeValueAsString(bookRecordWithInvalidValue());
        String expectedErrorMessage = "book - must not be null";

        //when
        when(libraryEventsProducer.synchronousSendLibraryEventWithProducerRecord(isA(LibraryEvent.class)))
                .thenReturn(null);

        ResultActions result = mockMvc.
                perform(MockMvcRequestBuilders.post(LIBRARY_EVENT_ASYNC)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().is4xxClientError());
        result.andExpect(content().string(expectedErrorMessage));
    }
}