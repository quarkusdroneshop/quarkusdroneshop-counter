package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.LineItemStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderUpdateTest {

    @Test
    public void testConstructorWithoutMadeBy() {
        OrderUpdate update = new OrderUpdate("order-1", "item-1", "Taro", Item.QDC_A101, LineItemStatus.IN_PROGRESS);
        assertEquals("order-1", update.getOrderId());
        assertEquals("item-1", update.getItemId());
        assertEquals("Taro", update.getName());
        assertEquals(Item.QDC_A101, update.getItem());
        assertEquals(LineItemStatus.IN_PROGRESS, update.getStatus());
        assertFalse(update.getMadeBy().isPresent());
    }

    @Test
    public void testConstructorWithMadeBy() {
        OrderUpdate update = new OrderUpdate("order-1", "item-1", "Taro", Item.QDC_A101, LineItemStatus.FULFILLED, "Worker");
        assertEquals("Worker", update.madeBy);
        assertTrue(update.getMadeBy().isPresent());
        assertEquals("Worker", update.getMadeBy().get());
    }

    @Test
    public void testPublicFields() {
        OrderUpdate update = new OrderUpdate("o1", "i1", "Name", Item.QDC_A102, LineItemStatus.PLACED);
        assertEquals("o1", update.orderId);
        assertEquals("i1", update.itemId);
        assertEquals("Name", update.name);
        assertEquals(Item.QDC_A102, update.item);
        assertNull(update.madeBy);
    }

    @Test
    public void testEquals() {
        OrderUpdate a = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS, "Worker");
        OrderUpdate b = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS, "Worker");
        assertEquals(a, b);
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    public void testEqualsDifferentFields() {
        OrderUpdate a = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS);
        assertNotEquals(a, new OrderUpdate("o2", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS));
        assertNotEquals(a, new OrderUpdate("o1", "i2", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS));
        assertNotEquals(a, new OrderUpdate("o1", "i1", "Other", Item.QDC_A101, LineItemStatus.IN_PROGRESS));
        assertNotEquals(a, new OrderUpdate("o1", "i1", "Name", Item.QDC_A102, LineItemStatus.IN_PROGRESS));
        assertNotEquals(a, new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.FULFILLED));
        assertNotEquals(a, new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS, "Worker"));
    }

    @Test
    public void testHashCode() {
        OrderUpdate a = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS, "W");
        OrderUpdate b = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS, "W");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeWithNullFields() {
        OrderUpdate update = new OrderUpdate(null, null, null, null, null);
        assertDoesNotThrow(update::hashCode);
    }

    @Test
    public void testEqualsWithMadeByOnOneSide() {
        OrderUpdate withMadeBy = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS, "Worker");
        OrderUpdate withoutMadeBy = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS);
        // madeBy != null, that.madeBy == null → not equal
        assertNotEquals(withMadeBy, withoutMadeBy);
    }

    @Test
    public void testEqualsWithBothNullMadeBy() {
        OrderUpdate a = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS);
        OrderUpdate b = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS);
        assertEquals(a, b);
    }

    @Test
    public void testEqualsWithNullOnOneSide() {
        OrderUpdate nonNull = new OrderUpdate("o1", "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS);
        // orderId null on one side
        assertNotEquals(new OrderUpdate(null, "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS), nonNull);
        assertNotEquals(nonNull, new OrderUpdate(null, "i1", "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS));
        // itemId null
        assertNotEquals(new OrderUpdate("o1", null, "Name", Item.QDC_A101, LineItemStatus.IN_PROGRESS), nonNull);
        // name null
        assertNotEquals(new OrderUpdate("o1", "i1", null, Item.QDC_A101, LineItemStatus.IN_PROGRESS), nonNull);
    }

    @Test
    public void testToString() {
        OrderUpdate update = new OrderUpdate("order-1", "item-1", "Taro", Item.QDC_A101, LineItemStatus.FULFILLED, "Worker");
        String str = update.toString();
        assertTrue(str.contains("order-1"));
        assertTrue(str.contains("QDC_A101"));
        assertTrue(str.contains("Worker"));
    }
}
