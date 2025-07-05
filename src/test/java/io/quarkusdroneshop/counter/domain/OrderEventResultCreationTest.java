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
        assertEquals(1, orderEventResult.getOrder().getQDCA101LineItems().get().size());
        assertFalse(orderEventResult.getOrder().getQDCA101Pro01LineItems().isPresent());
    }

    @Test
    public void testOrderCreationWithSingleQDCA105Pro01() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommandSingleQDCA105Pro01();
        OrderEventResult orderEventResult = Order.createFromCommand(placeOrderCommand);
        assertNotNull(orderEventResult.getOrder());
        assertNotNull(orderEventResult.getOrder().getOrderId());
        assertEquals(1, orderEventResult.getOrder().getQDCA101Pro01Items().get().size());
        assertFalse(orderEventResult.getOrder().getQDCA101LineItems().isPresent());
    }

    @Test
    public void testOrderCreationWithQDCA101AndQDCA105Pro01tems() {

        PlaceOrderCommand placeOrderCommand = TestUtil.stubPlaceOrderCommandBlackCoffeeAndQDCA105Pro01();
        OrderEventResult orderEventResult = Order.createFromCommand(placeOrderCommand);
        assertNotNull(orderEventResult.getOrder());
        assertNotNull(orderEventResult.getOrder().getOrderId());
        assertEquals(1, orderEventResult.getOrder().getQDCA101LineItems().get().size());
        assertEquals(1, orderEventResult.getOrder().getQDCA105Pro01LineItems().get().size());
    }

}
