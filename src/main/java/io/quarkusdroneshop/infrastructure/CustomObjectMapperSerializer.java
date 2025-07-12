package io.quarkusdroneshop.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class CustomObjectMapperSerializer<T> extends ObjectMapperSerializer<T> {
    public CustomObjectMapperSerializer() {
        super(createCustomObjectMapper());
    }

    private static ObjectMapper createCustomObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules(); // JavaTimeModule を登録
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 形式にする
        return mapper;
    }
}