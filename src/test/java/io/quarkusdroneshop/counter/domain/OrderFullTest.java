package io.quarkusdroneshop.counter.domain;

import io.quarkusdroneshop.counter.domain.commands.CommandItem;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderFullTest {

    @Test
    public void testDefaultConstructor() {
        Order order = new Order();
        assertNotNull(order.getOrderId());
        assertNotNull(order.getTimestamp());
    }

    @Test
    public void testStringIdConstructor() {
        Order order = new Order(UUID.randomUUID().toString());
        assertNotNull(order.getOrderId());
    }

    @Test
    public void testFullConstructor() {
        Order order = new Order(
            UUID.randomUUID().toString(),
            OrderSource.WEB,
            Location.TOKYO,
            "loyalty-001",
            Instant.now(),
            OrderStatus.IN_PROGRESS,
            new ArrayList<>(),
            new ArrayList<>()
        );
        assertNotNull(order.getOrderId());
        assertEquals(OrderSource.WEB, order.getOrderSource());
        assertEquals(Location.TOKYO, order.getLocation());
        assertEquals(OrderStatus.IN_PROGRESS, order.getOrderStatus());
    }

    @Test
    public void testSettersAndGetters() {
        Order order = new Order();
        order.setOrderSource(OrderSource.COUNTER);
        order.setLocation(Location.RALEIGH);
        order.setOrderStatus(OrderStatus.FULFILLED);
        order.setTimestamp(Instant.now());
        order.setLoyaltyMemberId("member-abc");

        assertEquals(OrderSource.COUNTER, order.getOrderSource());
        assertEquals(Location.RALEIGH, order.getLocation());
        assertEquals(OrderStatus.FULFILLED, order.getOrderStatus());
        assertTrue(order.getLoyaltyMemberId().isPresent());
        assertEquals("member-abc", order.getLoyaltyMemberId().get());
    }

    @Test
    public void testGetLoyaltyMemberIdEmpty() {
        Order order = new Order();
        assertFalse(order.getLoyaltyMemberId().isPresent());
    }

    @Test
    public void testAddQdca10LineItem() {
        Order order = new Order();
        OrderRecord record = order.getOrderRecord();
        LineItem item = new LineItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, record);
        order.addQdca10LineItem(item);

        assertTrue(order.getQdca10LineItems().isPresent());
        assertEquals(1, order.getQdca10LineItems().get().size());
    }

    @Test
    public void testAddQDCA10ProLineItem() {
        Order order = new Order();
        OrderRecord record = order.getOrderRecord();
        LineItem item = new LineItem(Item.QDC_A105_Pro01, "Hanako", BigDecimal.valueOf(553.00), LineItemStatus.IN_PROGRESS, record);
        order.addQDCA10ProLineItem(item);

        assertTrue(order.getQdca10proLineItems().isPresent());
        assertEquals(1, order.getQdca10proLineItems().get().size());
    }

    @Test
    public void testAddQDCA10ProLineItemWhenListAlreadyExists() {
        Order order = new Order();
        OrderRecord record = order.getOrderRecord();
        LineItem first = new LineItem(Item.QDC_A105_Pro01, "First", BigDecimal.valueOf(553.00), LineItemStatus.IN_PROGRESS, record);
        order.addQDCA10ProLineItem(first);
        LineItem second = new LineItem(Item.QDC_A105_Pro02, "Second", BigDecimal.valueOf(633.25), LineItemStatus.IN_PROGRESS, record);
        order.addQDCA10ProLineItem(second);

        assertEquals(2, order.getQdca10proLineItems().get().size());
    }

    @Test
    public void testSetQdca10LineItems() {
        Order order = new Order();
        order.setQdca10LineItems(new ArrayList<>());
        assertTrue(order.getQdca10LineItems().isPresent());
    }

    @Test
    public void testSetQdca10proLineItems() {
        Order order = new Order();
        order.setQdca10proLineItems(new ArrayList<>());
        assertTrue(order.getQdca10proLineItems().isPresent());
    }

    @Test
    public void testFromOrderRecord() {
        OrderRecord record = new OrderRecord();
        record.setOrderId(UUID.randomUUID());
        record.setOrderSource(OrderSource.WEB);
        record.setOrderStatus(OrderStatus.PLACED);
        record.setLocation(Location.ATLANTA);
        record.setTimestamp(Instant.now());

        Order order = Order.fromOrderRecord(record);
        assertNotNull(order);
        assertEquals(record.getOrderId(), order.getOrderId());
    }

    @Test
    public void testFromOrderRecordNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> Order.fromOrderRecord(null));
    }

    @Test
    public void testEqualsAndHashCode() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        Order a = Order.fromPlaceOrderCommand(cmd);
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");

        Order b = new Order();
        assertNotEquals(a, b);
    }

    @Test
    public void testToString() {
        Order order = new Order();
        order.setOrderSource(OrderSource.WEB);
        order.setLocation(Location.ATLANTA);
        String str = order.toString();
        assertTrue(str.contains("WEB"));
        assertTrue(str.contains("ATLANTA"));
    }

    @Test
    public void testApplyOrderTicketUpMatchesQdca10() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        Order order = Order.fromPlaceOrderCommand(cmd);
        LineItem lineItem = order.getQdca10LineItems().get().get(0);

        TicketUp ticketUp = new TicketUp(
            order.getOrderId(),
            UUID.fromString(lineItem.getItemId()),
            lineItem.getItem(),
            lineItem.getName(),
            OrderStatus.FULFILLED,
            "Worker"
        );

        OrderEventResult result = order.applyOrderTicketUp(ticketUp);
        assertNotNull(result);
        assertEquals(LineItemStatus.FULFILLED, lineItem.getLineItemStatus());
        assertEquals(OrderStatus.FULFILLED, order.getOrderStatus());
    }

    @Test
    public void testApplyOrderTicketUpMatchesQdca10pro() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommandSingleQDCA10Pro();
        Order order = Order.fromPlaceOrderCommand(cmd);
        LineItem lineItem = order.getQdca10proLineItems().get().get(0);

        TicketUp ticketUp = new TicketUp(
            order.getOrderId(),
            UUID.fromString(lineItem.getItemId()),
            lineItem.getItem(),
            lineItem.getName(),
            OrderStatus.FULFILLED,
            "Worker"
        );

        OrderEventResult result = order.applyOrderTicketUp(ticketUp);
        assertNotNull(result);
        assertEquals(LineItemStatus.FULFILLED, lineItem.getLineItemStatus());
    }

    @Test
    public void testApplyOrderTicketUpNotMatchedThrows() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        Order order = Order.fromPlaceOrderCommand(cmd);

        TicketUp ticketUp = new TicketUp(
            order.getOrderId(),
            UUID.randomUUID(), // unknown lineItemId
            Item.QDC_A101,
            "Unknown",
            OrderStatus.FULFILLED,
            "Worker"
        );

        assertThrows(IllegalArgumentException.class, () -> order.applyOrderTicketUp(ticketUp));
    }

    @Test
    public void testApplyOrderTicketUpNotAllFulfilled() {
        // Order with 2 line items; only 1 fulfilled → order status stays IN_PROGRESS
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(),
            OrderSource.WEB,
            Location.ATLANTA,
            null,
            Optional.of(Arrays.asList(
                new CommandItem(Item.QDC_A101, "A", BigDecimal.valueOf(135.50)),
                new CommandItem(Item.QDC_A102, "B", BigDecimal.valueOf(155.50))
            )),
            Optional.empty()
        );
        Order order = Order.fromPlaceOrderCommand(cmd);
        List<LineItem> items = order.getQdca10LineItems().get();
        LineItem first = items.get(0);

        TicketUp ticketUp = new TicketUp(
            order.getOrderId(),
            UUID.fromString(first.getItemId()),
            first.getItem(),
            first.getName(),
            OrderStatus.FULFILLED,
            "Worker"
        );

        order.applyOrderTicketUp(ticketUp);
        // Second item still IN_PROGRESS → order should not be FULFILLED
        assertNotEquals(OrderStatus.FULFILLED, order.getOrderStatus());
    }

    @Test
    public void testCreateFromCommandWithLoyaltyMember() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        OrderEventResult result = Order.createFromCommand(cmd);
        assertNotNull(result.getOrder());
        assertTrue(result.getOrder().getLoyaltyMemberId().isPresent());
        // Contains LoyaltyMemberPurchaseEvent
        assertNotNull(result.getOutboxEvents());
        assertEquals(2, result.getOutboxEvents().size());
    }

    @Test
    public void testCreateFromCommandWithoutLoyaltyMember() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(),
            OrderSource.WEB,
            Location.ATLANTA,
            null,
            Optional.of(Arrays.asList(new CommandItem(Item.QDC_A101, "Foo", BigDecimal.valueOf(135.50)))),
            Optional.empty()
        );
        OrderEventResult result = Order.createFromCommand(cmd);
        assertNotNull(result.getOrder());
        // Only OrderCreatedEvent (no loyalty event)
        assertEquals(1, result.getOutboxEvents().size());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        Order a = new Order(UUID.randomUUID().toString());
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");

        // non-null orderRecord paths
        Order withRecord = Order.fromPlaceOrderCommand(TestUtil.stubPlaceOrderCommand());
        assertDoesNotThrow(withRecord::hashCode);
        // withRecord.orderRecord != null, a.orderRecord != null → non-null branch
        assertDoesNotThrow(() -> withRecord.equals(a));

        // Force null orderRecord to cover null branch in hashCode and equals
        java.lang.reflect.Field f = Order.class.getDeclaredField("orderRecord");
        f.setAccessible(true);
        Order nullRecord = new Order(UUID.randomUUID().toString());
        f.set(nullRecord, null);
        // Verify null was set
        assertNull(f.get(nullRecord));
        // hashCode with null orderRecord → returns 0
        assertEquals(0, nullRecord.hashCode());
        // null vs non-null
        assertFalse(nullRecord.equals(a));
        // null vs null
        Order nullRecord2 = new Order(UUID.randomUUID().toString());
        f.set(nullRecord2, null);
        assertTrue(nullRecord.equals(nullRecord2));
    }
}
