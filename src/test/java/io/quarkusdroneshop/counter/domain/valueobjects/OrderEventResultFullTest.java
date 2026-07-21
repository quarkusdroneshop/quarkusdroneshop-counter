package io.quarkusdroneshop.counter.domain.valueobjects;

import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.*;
import io.quarkusdroneshop.counter.domain.events.OrderCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderEventResultFullTest {

    private Order buildOrder() {
        OrderRecord record = new OrderRecord();
        record.setOrderId(UUID.randomUUID().toString());
        record.setOrderSource(OrderSource.WEB);
        record.setLocation(Location.ATLANTA);
        return Order.fromOrderRecord(record);
    }

    @Test
    public void testDefaultConstructor() {
        OrderEventResult result = new OrderEventResult();
        assertNull(result.getOrder());
        assertNull(result.getOutboxEvents());
        assertNull(result.getOrderUpdates());
        assertFalse(result.getQdca10Tickets().isPresent());
        assertFalse(result.getQdca10proTickets().isPresent());
    }

    @Test
    public void testConstructorWithOrderUpdates() {
        List<OrderUpdate> updates = Arrays.asList(
            new OrderUpdate("order-1", "item-1", "Taro", Item.QDC_A101, LineItemStatus.FULFILLED)
        );
        OrderEventResult result = new OrderEventResult(updates);
        assertEquals(updates, result.getOrderUpdates());
    }

    @Test
    public void testSetAndGetOrder() {
        OrderEventResult result = new OrderEventResult();
        Order order = buildOrder();
        result.setOrder(order);
        assertSame(order, result.getOrder());
    }

    @Test
    public void testAddEvent() {
        OrderEventResult result = new OrderEventResult();
        Order order = buildOrder();
        order.setOrderSource(OrderSource.WEB);
        order.setTimestamp(java.time.Instant.now());
        ExportedEvent event = OrderCreatedEvent.of(order);
        result.addEvent(event);
        assertNotNull(result.getOutboxEvents());
        assertEquals(1, result.getOutboxEvents().size());

        // add second event
        result.addEvent(event);
        assertEquals(2, result.getOutboxEvents().size());
    }

    @Test
    public void testAddUpdate() {
        OrderEventResult result = new OrderEventResult();
        OrderUpdate update = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS);
        result.addUpdate(update);
        assertNotNull(result.getOrderUpdates());
        assertEquals(1, result.getOrderUpdates().size());
    }

    @Test
    public void testAddQdca10Ticket() {
        OrderEventResult result = new OrderEventResult();
        OrderTicket ticket = new OrderTicket("o1", "l1", Item.QDC_A101, "Taro");
        result.addQdca10Ticket(ticket);
        assertTrue(result.getQdca10Tickets().isPresent());
        assertEquals(1, result.getQdca10Tickets().get().size());

        // add second
        result.addQdca10Ticket(ticket);
        assertEquals(2, result.getQdca10Tickets().get().size());
    }

    @Test
    public void testAddQDdca10proTicket() {
        OrderEventResult result = new OrderEventResult();
        OrderTicket ticket = new OrderTicket("o1", "l1", Item.QDC_A105_Pro01, "Hanako");
        result.addQDdca10proTicket(ticket);
        assertTrue(result.getQdca10proTickets().isPresent());
        assertEquals(1, result.getQdca10proTickets().get().size());

        result.addQDdca10proTicket(ticket);
        assertEquals(2, result.getQdca10proTickets().get().size());
    }

    @Test
    public void testSetters() {
        OrderEventResult result = new OrderEventResult();
        result.setOutboxEvents(Arrays.asList());
        result.setQdca10Tickets(Arrays.asList());
        result.setQdca10proTickets(Arrays.asList());
        result.setOrderUpdates(Arrays.asList());

        assertNotNull(result.getOutboxEvents());
        assertTrue(result.getQdca10Tickets().isPresent());
        assertTrue(result.getQdca10proTickets().isPresent());
        assertNotNull(result.getOrderUpdates());
    }

    @Test
    public void testEqualsAndHashCode() {
        OrderEventResult a = new OrderEventResult();
        OrderEventResult b = new OrderEventResult();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    public void testEqualsDifferentOrder() {
        OrderEventResult a = new OrderEventResult();
        a.setOrder(buildOrder());
        OrderEventResult b = new OrderEventResult();
        assertNotEquals(a, b);
    }

    @Test
    public void testHashCodeWithNonNullFields() {
        OrderEventResult a = new OrderEventResult();
        a.setOrder(buildOrder());
        a.setOutboxEvents(Arrays.asList());
        a.setQdca10Tickets(Arrays.asList());
        a.setQdca10proTickets(Arrays.asList());
        a.setOrderUpdates(Arrays.asList());
        assertDoesNotThrow(a::hashCode);
    }

    @Test
    public void testEqualsWithNonNullEqualFields() {
        OrderEventResult a = new OrderEventResult();
        a.setQdca10Tickets(Arrays.asList(new OrderTicket("o1","l1",Item.QDC_A101,"Taro")));
        a.setQdca10proTickets(Arrays.asList(new OrderTicket("o1","l1",Item.QDC_A105_Pro01,"Hanako")));
        a.setOrderUpdates(Arrays.asList(new OrderUpdate("o1","i1","Taro",Item.QDC_A101,LineItemStatus.FULFILLED)));
        a.setOutboxEvents(Arrays.asList());

        OrderEventResult b = new OrderEventResult();
        b.setQdca10Tickets(a.getQdca10Tickets().get());
        b.setQdca10proTickets(a.getQdca10proTickets().get());
        b.setOrderUpdates(a.getOrderUpdates());
        b.setOutboxEvents(a.getOutboxEvents());

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsNullOnOneSide() {
        OrderEventResult withOrder = new OrderEventResult();
        withOrder.setOrder(buildOrder());

        // order non-null vs null
        assertNotEquals(withOrder, new OrderEventResult());
        // outboxEvents null vs non-null
        OrderEventResult withEvents = new OrderEventResult();
        withEvents.setOutboxEvents(java.util.Collections.emptyList());
        assertNotEquals(withEvents, new OrderEventResult());
        // orderUpdates non-null vs null
        OrderEventResult withUpdates = new OrderEventResult();
        withUpdates.setOrderUpdates(java.util.Collections.emptyList());
        assertNotEquals(withUpdates, new OrderEventResult());
    }

    @Test
    public void testEqualsWithDifferentTickets() {
        OrderEventResult a = new OrderEventResult();
        a.setQdca10Tickets(Arrays.asList(new OrderTicket("o1","l1",Item.QDC_A101,"Taro")));

        OrderEventResult b = new OrderEventResult();
        b.setQdca10Tickets(Arrays.asList(new OrderTicket("o2","l2",Item.QDC_A102,"Jiro")));

        assertNotEquals(a, b);
    }

    @Test
    public void testToString() {
        OrderEventResult result = new OrderEventResult();
        String str = result.toString();
        assertTrue(str.contains("OrderEventResult"));
    }
}
