package io.quarkusdroneshop.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;

/**
 * Jackson deserializer for TicketUp value object
 */
public class TicketUpDeserializer extends ObjectMapperDeserializer<TicketUp> {

    public TicketUpDeserializer() {
        super(TicketUp.class);
    }
}
