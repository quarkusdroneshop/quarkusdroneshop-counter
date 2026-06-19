package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTicketTest {

    @Test
    public void testConstructorAndGetters() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();
        OrderTicket ticket = new OrderTicket(orderId, lineItemId, Item.QDC_A101, "Taro");

        assertEquals(orderId, ticket.getOrderId());
        assertEquals(lineItemId, ticket.getLineItemId());
        assertEquals(Item.QDC_A101, ticket.getItem());
        assertEquals("Taro", ticket.getName());
        assertNotNull(ticket.getTimestamp());
    }

    @Test
    public void testEqualsAndHashCode() {
        String orderId = UUID.randomUUID().toString();
        String lineItemId = UUID.randomUUID().toString();
        OrderTicket a = new OrderTicket(orderId, lineItemId, Item.QDC_A101, "Taro");
        // same reference → equal
        assertEquals(a, a);
        // different item → not equal
        OrderTicket b = new OrderTicket(orderId, lineItemId, Item.QDC_A105_Pro01, "Taro");
        assertNotEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    public void testHashCode() {
        OrderTicket ticket = new OrderTicket("o1", "l1", Item.QDC_A101, "Taro");
        assertDoesNotThrow(ticket::hashCode);
    }

    @Test
    public void testEqualsWithEqualObjects() throws Exception {
        OrderTicket a = new OrderTicket("orderId", "lineId", Item.QDC_A101, "Taro");
        OrderTicket b = new OrderTicket("orderId2", "lineId2", Item.QDC_A101, "Taro");
        // Force same orderId, lineItemId, timestamp
        java.lang.reflect.Field fLineId = OrderTicket.class.getDeclaredField("lineItemId");
        fLineId.setAccessible(true);
        java.lang.reflect.Field fOrderId = OrderTicket.class.getDeclaredField("orderId");
        fOrderId.setAccessible(true);
        java.lang.reflect.Field fTs = OrderTicket.class.getDeclaredField("timestamp");
        fTs.setAccessible(true);
        fOrderId.set(b, "orderId");
        fLineId.set(b, "lineId");
        fTs.set(b, fTs.get(a));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsNullOnOneSide() {
        OrderTicket nonNull = new OrderTicket("o1", "l1", Item.QDC_A101, "Taro");
        // orderId null on one side
        assertNotEquals(new OrderTicket(null, "l1", Item.QDC_A101, "Taro"), nonNull);
        assertNotEquals(nonNull, new OrderTicket(null, "l1", Item.QDC_A101, "Taro"));
        // lineItemId null on one side
        assertNotEquals(new OrderTicket("o1", null, Item.QDC_A101, "Taro"), nonNull);
        // name null
        assertNotEquals(new OrderTicket("o1", "l1", Item.QDC_A101, null), nonNull);
    }

    @Test
    public void testHashCodeDoesNotThrow() {
        OrderTicket t = new OrderTicket("o1", "l1", Item.QDC_A101, "Taro");
        assertDoesNotThrow(t::hashCode);
    }

    @Test
    public void testHashCodeAndEqualsWithNullFields() throws Exception {
        OrderTicket a = new OrderTicket(null, null, null, null);
        OrderTicket b = new OrderTicket(null, null, null, null);
        // Force same timestamp via reflection
        java.lang.reflect.Field fTs = OrderTicket.class.getDeclaredField("timestamp");
        fTs.setAccessible(true);
        fTs.set(b, fTs.get(a));
        // All fields null except timestamp → equal
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        // hashCode with non-null timestamp only
        assertDoesNotThrow(a::hashCode);
    }

    @Test
    public void testToString() {
        OrderTicket ticket = new OrderTicket("order-123", "line-456", Item.QDC_A105_Pro01, "Hanako");
        String str = ticket.toString();
        assertTrue(str.contains("order-123"));
        assertTrue(str.contains("QDC_A105_Pro01"));
        assertTrue(str.contains("Hanako"));
    }
}
