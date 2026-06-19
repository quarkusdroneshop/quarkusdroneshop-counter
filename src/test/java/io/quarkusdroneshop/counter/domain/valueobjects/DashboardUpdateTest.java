package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.LineItemStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardUpdateTest {

    @Test
    public void testConstructorAndFields() {
        DashboardUpdate update = new DashboardUpdate(
            "order-1", "item-1", "Taro", Item.QDC_A101, LineItemStatus.FULFILLED, "Worker"
        );

        assertEquals("order-1", update.orderId);
        assertEquals("item-1", update.itemId);
        assertEquals("Taro", update.name);
        assertEquals(Item.QDC_A101, update.item);
        assertEquals(LineItemStatus.FULFILLED, update.status);
        assertEquals("Worker", update.madeBy);
    }

    @Test
    public void testNullMadeBy() {
        DashboardUpdate update = new DashboardUpdate(
            "order-1", "item-1", "Taro", Item.QDC_A102, LineItemStatus.IN_PROGRESS, null
        );
        assertNull(update.madeBy);
    }
}
