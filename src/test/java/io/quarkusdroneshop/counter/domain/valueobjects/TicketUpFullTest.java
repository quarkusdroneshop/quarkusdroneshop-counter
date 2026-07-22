package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class TicketUpFullTest {

    @Test
    public void testJsonCreatorConstructorWithStringTimestamp() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();

        TicketUp ticketUp = new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", timestamp, OrderStatus.FULFILLED, "Worker");

        assertEquals(orderId, ticketUp.getOrderId());
        assertEquals(lineItemId, ticketUp.getLineItemId());
        assertEquals(Item.QDC_A101, ticketUp.getItem());
        assertEquals("Taro", ticketUp.getName());
        assertEquals(OrderStatus.FULFILLED, ticketUp.getStatus());
        assertEquals("Worker", ticketUp.getMadeBy());
        assertNotNull(ticketUp.getTimestamp());
    }

    @Test
    public void testJsonCreatorConstructorWithNumberTimestamp() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();
        long epochMilli = System.currentTimeMillis();

        TicketUp ticketUp = new TicketUp(orderId, lineItemId, Item.QDC_A102, "Hanako", epochMilli, OrderStatus.IN_PROGRESS, "Drone");

        assertEquals(Instant.ofEpochMilli(epochMilli), ticketUp.getTimestamp());
    }

    @Test
    public void testJsonCreatorConstructorWithNullTimestamp() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();

        TicketUp ticketUp = new TicketUp(orderId, lineItemId, Item.QDC_A103, "Ichiro", null, OrderStatus.PLACED, "Bot");

        assertNotNull(ticketUp.getTimestamp());
    }

    @Test
    public void testSecondConstructorWithMadeBy() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();

        TicketUp ticketUp = new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", "Drone");

        assertEquals(orderId, ticketUp.getOrderId());
        assertEquals(lineItemId, ticketUp.getLineItemId());
        assertEquals(Item.QDC_A101, ticketUp.getItem());
        assertEquals("Taro", ticketUp.getName());
        assertEquals("Drone", ticketUp.getMadeBy());
    }

    @Test
    public void testThirdConstructorWithStatus() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();

        TicketUp ticketUp = new TicketUp(orderId, lineItemId, Item.QDC_A105_Pro01, "Hanako", OrderStatus.FULFILLED, "Worker");

        assertEquals(orderId, ticketUp.getOrderId());
        assertEquals(lineItemId, ticketUp.getLineItemId());
        assertEquals(Item.QDC_A105_Pro01, ticketUp.getItem());
        assertEquals("Hanako", ticketUp.getName());
        assertEquals(OrderStatus.FULFILLED, ticketUp.getStatus());
        assertEquals("Worker", ticketUp.getMadeBy());
    }

    @Test
    public void testEquals() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();
        String ts = Instant.now().toString();

        TicketUp a = new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker");
        TicketUp b = new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker");

        assertEquals(a, b);
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    public void testEqualsDifferentFields() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();
        String ts = Instant.now().toString();

        TicketUp a = new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker");
        assertNotEquals(a, new TicketUp(UUID.randomUUID().toString(), lineItemId, Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker"));
        assertNotEquals(a, new TicketUp(orderId, UUID.randomUUID().toString(), Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker"));
        assertNotEquals(a, new TicketUp(orderId, lineItemId, Item.QDC_A102, "Taro", ts, OrderStatus.FULFILLED, "Worker"));
        assertNotEquals(a, new TicketUp(orderId, lineItemId, Item.QDC_A101, "Other", ts, OrderStatus.FULFILLED, "Worker"));
        assertNotEquals(a, new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Other"));
    }

    @Test
    public void testHashCode() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();
        String ts = Instant.now().toString();

        TicketUp a = new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker");
        TicketUp b = new TicketUp(orderId, lineItemId, Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker");

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsNullOnOneSide() {
        String ts = Instant.now().toString();
        TicketUp nonNull = new TicketUp(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker");
        // orderId null on one side
        TicketUp nullOrderId = new TicketUp(null, nonNull.getLineItemId(), Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, "Worker");
        assertNotEquals(nonNull, nullOrderId);
        assertNotEquals(nullOrderId, nonNull);
        // madeBy null on non-null side
        TicketUp nullMadeBy = new TicketUp(nonNull.getOrderId(), nonNull.getLineItemId(), Item.QDC_A101, "Taro", ts, OrderStatus.FULFILLED, null);
        assertNotEquals(nonNull, nullMadeBy);
    }

    @Test
    public void testHashCodeWithNullFields() {
        TicketUp ticketUp = new TicketUp(null, null, null, null, (String) null);
        assertDoesNotThrow(ticketUp::hashCode);
    }

    @Test
    public void testEqualsWithNullFields() {
        String ts = Instant.now().toString();
        TicketUp a = new TicketUp(null, null, null, null, ts, null, null);
        TicketUp b = new TicketUp(null, null, null, null, ts, null, null);
        assertEquals(a, b);
    }

    @Test
    public void testToString() {
        TicketUp ticketUp = new TicketUp(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.QDC_A101, "Taro", "Worker");
        String str = ticketUp.toString();
        assertTrue(str.contains("QDC_A101"));
        assertTrue(str.contains("Taro"));
        assertTrue(str.contains("Worker"));
    }
}
