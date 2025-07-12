package io.quarkusdroneshop.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class CustomObjectMapperSerializer<T> extends ObjectMapperSerializer<T> {

    public CustomObjectMapperSerializer() {
        super(createObjectMapper());
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // for java.time.Instant
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ⬅️ ISO-8601 にする
        return mapper;
    }
}