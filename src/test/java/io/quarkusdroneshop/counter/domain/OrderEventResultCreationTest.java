package io.quarkusdroneshop.counter.domain;

import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderEventResultCreationTest {

    @Test
    public void testOrderCreationWithSingleQDCA101() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommand();
        OrderEventResult orderEventResult = Order.createFromCommand(placeOrderCommand);
        assertNotNull(orderEventResult.getOrder());
        assertNotNull(orderEventResult.getOrder().getOrderId());
        assertEquals(1, orderEventResult.getOrder().getQDCA10LineItems().get().size());
        assertFalse(orderEventResult.getOrder().getQDCA10ProLineItems().isPresent());
    }

    @Test
    public void testOrderCreationWithSingleQDCA105Pro01() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommandSingleQDCA10Pro();
        OrderEventResult orderEventResult = Order.createFromCommand(placeOrderCommand);
        assertNotNull(orderEventResult.getOrder());
        assertNotNull(orderEventResult.getOrder().getOrderId());
        assertEquals(1, orderEventResult.getOrder().getQDCA10ProLineItems().get().size());
        assertFalse(orderEventResult.getOrder().getQDCA10LineItems().isPresent());
    }

    @Test
    public void testOrderCreationWithQDCA101AndQDCA105Pro01Protems() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommandQDCA10AndQDCA10Pro();
        OrderEventResult orderEventResult = Order.createFromCommand(placeOrderCommand);
        assertNotNull(orderEventResult.getOrder());
        assertNotNull(orderEventResult.getOrder().getOrderId());
        assertEquals(1, orderEventResult.getOrder().getQDCA10LineItems().get().size());
        assertEquals(1, orderEventResult.getOrder().getQDCA10ProLineItems().get().size());
    }

}
