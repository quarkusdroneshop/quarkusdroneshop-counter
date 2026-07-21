package io.quarkusdroneshop.infrastructure;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkusdroneshop.counter.domain.*;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderTicket;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderUpdate;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestProfile(InfrastructureTestProfile.class)
public class OrderServiceFullTest {

    @Inject
    OrderService orderService;

    @Inject
    OrderRepository orderRepository;

    @Inject
    @Any
    InMemoryConnector connector;

    @Test
    @TestTransaction
    public void testOnOrderInTxWithQdca10() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        OrderEventResult result = orderService.onOrderInTx(cmd);

        assertNotNull(result);
        assertNotNull(result.getOrder());
        assertTrue(result.getQdca10Tickets().isPresent());
        assertEquals(1, result.getQdca10Tickets().get().size());
        assertFalse(result.getQdca10proTickets().isPresent());
        assertNotNull(result.getOrderUpdates());
    }

    @Test
    @TestTransaction
    public void testOnOrderInTxWithQdca10pro() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommandSingleQDCA10Pro();
        OrderEventResult result = orderService.onOrderInTx(cmd);

        assertNotNull(result);
        assertTrue(result.getQdca10proTickets().isPresent());
        assertFalse(result.getQdca10Tickets().isPresent());
    }

    @Test
    @TestTransaction
    public void testOnOrderInTxWithBoth() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommandQDCA10AndQDCA10Pro();
        OrderEventResult result = orderService.onOrderInTx(cmd);

        assertNotNull(result);
        assertTrue(result.getQdca10Tickets().isPresent());
        assertTrue(result.getQdca10proTickets().isPresent());
    }

    @Test
    @TestTransaction
    public void testOnOrderInTxWithLoyaltyMember() {
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        OrderEventResult result = orderService.onOrderInTx(cmd);

        assertNotNull(result.getOutboxEvents());
        // OrderCreatedEvent + LoyaltyMemberPurchaseEvent
        assertEquals(2, result.getOutboxEvents().size());
    }

    @Test
    @TestTransaction
    public void testOnOrderUpTxOrderNotFound() {
        TicketUp ticketUp = new TicketUp(
            UUID.randomUUID(), UUID.randomUUID(), Item.QDC_A101, "Taro", OrderStatus.FULFILLED, "Worker"
        );

        OrderEventResult result = orderService.onOrderUpTx(ticketUp);

        assertNotNull(result);
        assertNotNull(result.getOrderUpdates());
        assertEquals(0, result.getOrderUpdates().size());
    }

    @Test
    @TestTransaction
    public void testOnOrderUpTxOrderFound() {
        // 1. Place order
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        OrderEventResult placed = orderService.onOrderInTx(cmd);
        Order order = placed.getOrder();
        String orderId = order.getOrderId();

        // 2. Get the lineItem
        LineItem lineItem = order.getQdca10LineItems().get().get(0);

        // 3. Send TicketUp
        TicketUp ticketUp = new TicketUp(
            UUID.fromString(orderId),
            UUID.fromString(lineItem.getItemId()),
            lineItem.getItem(),
            lineItem.getName(),
            OrderStatus.FULFILLED,
            "Worker"
        );

        OrderEventResult result = orderService.onOrderUpTx(ticketUp);
        assertNotNull(result);
        assertFalse(result.getOrderUpdates().isEmpty());
        assertEquals(LineItemStatus.FULFILLED, result.getOrderUpdates().get(0).getStatus());
    }

    @Test
    @TestTransaction
    public void testOnOrderUpTxAllFulfilled() {
        // Place and then fulfill
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommand();
        OrderEventResult placed = orderService.onOrderInTx(cmd);
        String orderId = placed.getOrder().getOrderId();
        LineItem lineItem = placed.getOrder().getQdca10LineItems().get().get(0);

        TicketUp ticketUp = new TicketUp(
            UUID.fromString(orderId), UUID.fromString(lineItem.getItemId()),
            lineItem.getItem(), lineItem.getName(), OrderStatus.FULFILLED, "Worker"
        );
        orderService.onOrderUpTx(ticketUp);

        // Fulfill again (should produce 0 updates)
        OrderEventResult secondResult = orderService.onOrderUpTx(ticketUp);
        assertNotNull(secondResult);
        assertEquals(0, secondResult.getOrderUpdates().size());
    }

    @Test
    @TestTransaction
    public void testOnOrderUpTxWithQdca10proOrder() {
        // Place order with both qdca10 and qdca10pro
        PlaceOrderCommand cmd = TestUtil.stubPlaceOrderCommandQDCA10AndQDCA10Pro();
        OrderEventResult placed = orderService.onOrderInTx(cmd);
        Order order = placed.getOrder();
        String orderId = order.getOrderId();

        // Fulfill the qdca10pro item
        LineItem proItem = order.getQdca10proLineItems().get().get(0);
        TicketUp ticketUp = new TicketUp(
            UUID.fromString(orderId),
            UUID.fromString(proItem.getItemId()),
            proItem.getItem(),
            proItem.getName(),
            OrderStatus.FULFILLED,
            "Worker"
        );

        OrderEventResult result = orderService.onOrderUpTx(ticketUp);
        assertNotNull(result);
        assertFalse(result.getOrderUpdates().isEmpty());
    }

    @Test
    public void testSendOrderUpdate() {
        InMemorySink<Object> sink = connector.sink("web-updates");
        OrderUpdate update = new OrderUpdate("o1", "i1", "Taro", Item.QDC_A101, LineItemStatus.FULFILLED, "Worker");
        assertDoesNotThrow(() -> orderService.sendOrderUpdate(update));
    }

    @Test
    public void testSendQdca10() {
        InMemorySink<Object> sink = connector.sink("qdca10");
        OrderTicket ticket = new OrderTicket("o1", "l1", Item.QDC_A101, "Taro");
        assertDoesNotThrow(() -> orderService.sendQdca10(ticket));
    }

    @Test
    public void testSendQdca10pro() {
        InMemorySink<Object> sink = connector.sink("qdca10pro");
        OrderTicket ticket = new OrderTicket("o1", "l1", Item.QDC_A105_Pro01, "Hanako");
        assertDoesNotThrow(() -> orderService.sendQdca10pro(ticket));
    }

    @Test
    public void testToString() {
        String str = orderService.toString();
        assertNotNull(str);
        assertTrue(str.contains("OrderService"));
    }
}
