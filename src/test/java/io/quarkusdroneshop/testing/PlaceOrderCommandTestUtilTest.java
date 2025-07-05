package io.quarkusdroneshop.testing;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlaceOrderCommandTestUtilTest {


    @Test
    public void testIt() {

        PlaceOrderCommand placeOrderCommand = new PlaceOrderCommandTestUtil().withBlackCoffee().build();
        assertNotNull(placeOrderCommand);
        assertNotNull(placeOrderCommand.getId());
        assertEquals(1, placeOrderCommand.getQDCA10LineItems().get().size());
        assertEquals(Item.QDC_A101, placeOrderCommand.getQDCA10LineItems().get().get(0).item);
    }
}
