package io.quarkusdroneshop.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeserializersTest {

    @Test
    public void testOrderTicketDeserializerInstantiation() {
        OrderTicketDeserializer deserializer = new OrderTicketDeserializer();
        assertNotNull(deserializer);
    }

    @Test
    public void testPlaceOrderCommandDeserializerInstantiation() {
        PlaceOrderCommandDeserializer deserializer = new PlaceOrderCommandDeserializer();
        assertNotNull(deserializer);
    }

    @Test
    public void testTicketUpDeserializerInstantiation() {
        TicketUpDeserializer deserializer = new TicketUpDeserializer();
        assertNotNull(deserializer);
    }
}
