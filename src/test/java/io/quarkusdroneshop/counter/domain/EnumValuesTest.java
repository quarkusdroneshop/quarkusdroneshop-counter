package io.quarkusdroneshop.counter.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnumValuesTest {

    @Test
    public void testLineItemStatusValues() {
        assertEquals(3, LineItemStatus.values().length);
        assertEquals(LineItemStatus.PLACED, LineItemStatus.valueOf("PLACED"));
        assertEquals(LineItemStatus.IN_PROGRESS, LineItemStatus.valueOf("IN_PROGRESS"));
        assertEquals(LineItemStatus.FULFILLED, LineItemStatus.valueOf("FULFILLED"));
    }

    @Test
    public void testLocationValues() {
        assertEquals(4, Location.values().length);
        assertEquals(Location.ATLANTA, Location.valueOf("ATLANTA"));
        assertEquals(Location.CHARLOTTE, Location.valueOf("CHARLOTTE"));
        assertEquals(Location.RALEIGH, Location.valueOf("RALEIGH"));
        assertEquals(Location.TOKYO, Location.valueOf("TOKYO"));
    }

    @Test
    public void testOrderSourceValues() {
        assertEquals(3, OrderSource.values().length);
        assertEquals(OrderSource.COUNTER, OrderSource.valueOf("COUNTER"));
        assertEquals(OrderSource.WEB, OrderSource.valueOf("WEB"));
        assertEquals(OrderSource.PARTNER, OrderSource.valueOf("PARTNER"));
    }

    @Test
    public void testOrderStatusValues() {
        assertEquals(3, OrderStatus.values().length);
        assertEquals(OrderStatus.PLACED, OrderStatus.valueOf("PLACED"));
        assertEquals(OrderStatus.IN_PROGRESS, OrderStatus.valueOf("IN_PROGRESS"));
        assertEquals(OrderStatus.FULFILLED, OrderStatus.valueOf("FULFILLED"));
    }
}
