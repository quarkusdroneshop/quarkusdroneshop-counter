package io.quarkusdroneshop.counter.domain.events;

import io.quarkusdroneshop.counter.domain.*;
import io.quarkusdroneshop.counter.domain.commands.CommandItem;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderEventsTest {

    private Order buildOrderWithQdca10() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(), OrderSource.WEB, Location.ATLANTA, null,
            Optional.of(Arrays.asList(new CommandItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50)))),
            Optional.empty()
        );
        return Order.fromPlaceOrderCommand(cmd);
    }

    private Order buildOrderWithBoth() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(), OrderSource.WEB, Location.TOKYO, "member-1",
            Optional.of(Arrays.asList(new CommandItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50)))),
            Optional.of(Arrays.asList(new CommandItem(Item.QDC_A105_Pro01, "Hanako", BigDecimal.valueOf(553.00))))
        );
        return Order.fromPlaceOrderCommand(cmd);
    }

    private Order buildOrderWithOnlyQdca10pro() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(), OrderSource.COUNTER, Location.RALEIGH, null,
            Optional.empty(),
            Optional.of(Arrays.asList(new CommandItem(Item.QDC_A105_Pro01, "Hanako", BigDecimal.valueOf(553.00))))
        );
        return Order.fromPlaceOrderCommand(cmd);
    }

    // OrderCreatedEvent
    @Test
    public void testOrderCreatedEventWithQdca10() {
        Order order = buildOrderWithQdca10();
        OrderCreatedEvent event = OrderCreatedEvent.of(order);

        assertEquals(order.getOrderId().toString(), event.getAggregateId());
        assertEquals("Order", event.getAggregateType());
        assertEquals("OrderCreated", event.getType());
        assertNotNull(event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    @Test
    public void testOrderCreatedEventWithBoth() {
        Order order = buildOrderWithBoth();
        OrderCreatedEvent event = OrderCreatedEvent.of(order);
        assertNotNull(event.getPayload());
    }

    @Test
    public void testOrderCreatedEventWithOnlyQdca10pro() {
        Order order = buildOrderWithOnlyQdca10pro();
        OrderCreatedEvent event = OrderCreatedEvent.of(order);
        assertNotNull(event.getPayload());
    }

    // OrderUpdatedEvent
    @Test
    public void testOrderUpdatedEventWithQdca10() {
        Order order = buildOrderWithQdca10();
        OrderUpdatedEvent event = OrderUpdatedEvent.of(order);

        assertEquals(order.getOrderId().toString(), event.getAggregateId());
        assertEquals("Order", event.getAggregateType());
        assertEquals("OrderUpdated", event.getType());
        assertNotNull(event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    @Test
    public void testOrderUpdatedEventWithBoth() {
        Order order = buildOrderWithBoth();
        OrderUpdatedEvent event = OrderUpdatedEvent.of(order);
        assertNotNull(event.getPayload());
    }

    @Test
    public void testOrderUpdatedEventWithOnlyQdca10pro() {
        Order order = buildOrderWithOnlyQdca10pro();
        OrderUpdatedEvent event = OrderUpdatedEvent.of(order);
        assertNotNull(event.getPayload());
    }

    // LoyaltyMemberPurchaseEvent
    @Test
    public void testLoyaltyMemberPurchaseEventWithBoth() {
        Order order = buildOrderWithBoth();
        LoyaltyMemberPurchaseEvent event = LoyaltyMemberPurchaseEvent.of(order);

        assertEquals(order.getOrderId().toString(), event.getAggregateId());
        assertEquals("Order", event.getAggregateType());
        assertEquals("LoyaltyMemberPurchaseEvent", event.getType());
        assertNotNull(event.getPayload());
        assertNotNull(event.getTimestamp());
    }

    @Test
    public void testLoyaltyMemberPurchaseEventWithOnlyQdca10() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(), OrderSource.WEB, Location.ATLANTA, "member-2",
            Optional.of(Arrays.asList(new CommandItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50)))),
            Optional.empty()
        );
        Order order = Order.fromPlaceOrderCommand(cmd);
        LoyaltyMemberPurchaseEvent event = LoyaltyMemberPurchaseEvent.of(order);
        assertNotNull(event.getPayload());
    }

    @Test
    public void testLoyaltyMemberPurchaseEventWithOnlyQdca10pro() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(), OrderSource.WEB, Location.CHARLOTTE, "member-3",
            Optional.empty(),
            Optional.of(Arrays.asList(new CommandItem(Item.QDC_A105_Pro01, "Hanako", BigDecimal.valueOf(553.00))))
        );
        Order order = Order.fromPlaceOrderCommand(cmd);
        LoyaltyMemberPurchaseEvent event = LoyaltyMemberPurchaseEvent.of(order);
        assertNotNull(event.getPayload());
    }
}
