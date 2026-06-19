package io.quarkusdroneshop.counter.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class LineItemTest {

    private OrderRecord buildOrderRecord() {
        OrderRecord record = new OrderRecord();
        record.setOrderId(UUID.randomUUID());
        return record;
    }

    @Test
    public void testDefaultConstructor() {
        LineItem lineItem = new LineItem();
        assertNotNull(lineItem.getItemId());
    }

    @Test
    public void testFullConstructor() {
        OrderRecord record = buildOrderRecord();
        LineItem lineItem = new LineItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, record);

        assertNotNull(lineItem.getItemId());
        assertEquals(Item.QDC_A101, lineItem.getItem());
        assertEquals("Taro", lineItem.getName());
        assertEquals(BigDecimal.valueOf(135.50), lineItem.getPrice());
        assertEquals(LineItemStatus.IN_PROGRESS, lineItem.getLineItemStatus());
    }

    @Test
    public void testGetPriceDelegatesToItem() {
        OrderRecord record = buildOrderRecord();
        LineItem lineItem = new LineItem(Item.QDC_A105_Pro01, "Hanako", BigDecimal.valueOf(553.00), LineItemStatus.PLACED, record);
        assertEquals(Item.QDC_A105_Pro01.getPrice(), lineItem.getPrice());
    }

    @Test
    public void testSetters() {
        OrderRecord record = buildOrderRecord();
        LineItem lineItem = new LineItem();
        lineItem.setItem(Item.QDC_A102);
        lineItem.setName("Updated");
        lineItem.setLineItemStatus(LineItemStatus.FULFILLED);
        lineItem.setOrder(record);

        assertEquals(Item.QDC_A102, lineItem.getItem());
        assertEquals("Updated", lineItem.getName());
        assertEquals(LineItemStatus.FULFILLED, lineItem.getLineItemStatus());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        OrderRecord record = buildOrderRecord();
        LineItem a = new LineItem(Item.QDC_A101, "Foo", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, record);
        LineItem b = new LineItem(Item.QDC_A101, "Foo", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, record);

        // Same reference
        assertEquals(a, a);
        // Different itemId (UUID) → not equal
        assertNotEquals(a, b);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");

        // Force same itemId to test full equals path
        java.lang.reflect.Field f = LineItem.class.getDeclaredField("itemId");
        f.setAccessible(true);
        f.set(b, f.get(a));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsFieldDifferences() throws Exception {
        OrderRecord rec = buildOrderRecord();
        LineItem base = new LineItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, rec);
        // Force same itemId
        java.lang.reflect.Field f = LineItem.class.getDeclaredField("itemId");
        f.setAccessible(true);
        String baseItemId = (String) f.get(base);

        // item differs (same order, same itemId)
        LineItem diffItem = new LineItem(Item.QDC_A102, "Taro", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, rec);
        f.set(diffItem, baseItemId);
        assertNotEquals(base, diffItem);

        // name differs
        LineItem diffName = new LineItem(Item.QDC_A101, "Jiro", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, rec);
        f.set(diffName, baseItemId);
        assertNotEquals(base, diffName);

        // lineItemStatus differs
        LineItem diffStatus = new LineItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50), LineItemStatus.FULFILLED, rec);
        f.set(diffStatus, baseItemId);
        assertNotEquals(base, diffStatus);
    }

    @Test
    public void testHashCodeWithNullFields() {
        LineItem lineItem = new LineItem();
        // itemId is set by default constructor; other fields are null → should not throw
        assertDoesNotThrow(lineItem::hashCode);
    }

    @Test
    public void testEqualsWithNullItemId() throws Exception {
        LineItem a = new LineItem();
        LineItem b = new LineItem();
        // Force null itemId in both
        java.lang.reflect.Field f = LineItem.class.getDeclaredField("itemId");
        f.setAccessible(true);
        f.set(a, null);
        f.set(b, null);
        // Both null itemId → still not equal (order/name/item etc. may differ or be null)
        // Either way, should not throw
        assertDoesNotThrow(() -> a.equals(b));
        assertDoesNotThrow(a::hashCode);
    }

    @Test
    public void testToString() {
        OrderRecord record = buildOrderRecord();
        LineItem lineItem = new LineItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, record);
        String str = lineItem.toString();
        assertTrue(str.contains("QDC_A101"));
        assertTrue(str.contains("Taro"));
        assertTrue(str.contains("IN_PROGRESS"));
    }
}
