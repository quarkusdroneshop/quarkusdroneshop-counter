package io.quarkusdroneshop.infrastructure;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderTicket;

public class OrderTicketDeserializer  extends ObjectMapperDeserializer<OrderTicket> {

    public OrderTicketDeserializer() {
        super(OrderTicket.class);
    }
}
