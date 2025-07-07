package io.quarkusdroneshop.counter.domain;

import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrderCreationTest {

    @Test
    public void testOrderCreationWithSingleQDCA10() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommand();
        Order order = Order.fromPlaceOrderCommand(placeOrderCommand);
        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals(1, order.getQdca10LineItems().get().size());
        assertFalse(order.getQdca10proLineItems().isPresent());
    }

    @Test
    public void testOrderCreationWithSingleQDCA105Pro01() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommandSingleQDCA10Pro();
        Order order = Order.fromPlaceOrderCommand(placeOrderCommand);
        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals(1, order.getQdca10proLineItems().get().size());
        assertFalse(order.getQdca10LineItems().isPresent());
    }

    @Test
    public void testOrderCreationWithQdca10AndQdcA10proItems() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommandQDCA10AndQDCA10Pro();
        Order order = Order.fromPlaceOrderCommand(placeOrderCommand);
        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals(1, order.getQdca10LineItems().get().size());
        assertEquals(1, order.getQdca10proLineItems().get().size());
    }
}
