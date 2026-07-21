package io.quarkusdroneshop.counter.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderRecordFullTest {

    @Test
    public void testDefaultConstructor() {
        OrderRecord record = new OrderRecord();
        assertNotNull(record);
    }

    @Test
    public void testFullConstructor() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        List<LineItem> qdca10Items = new ArrayList<>();
        List<LineItem> qdca10proItems = new ArrayList<>();

        OrderRecord record = new OrderRecord(id, OrderSource.WEB, "member123", now, OrderStatus.PLACED, Location.TOKYO, qdca10Items, qdca10proItems);

        assertEquals(id.toString(), record.getOrderId());
        assertEquals(OrderSource.WEB, record.getOrderSource());
        assertEquals("member123", record.getLoyaltyMemberId());
        assertEquals(now, record.getTimestamp());
        assertEquals(OrderStatus.PLACED, record.getOrderStatus());
        assertEquals(Location.TOKYO, record.getLocation());
        assertSame(qdca10Items, record.getQdca10LineItems());
        assertSame(qdca10proItems, record.getQdca10proLineItems());
    }

    @Test
    public void testSetters() {
        OrderRecord record = new OrderRecord();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        record.setOrderId(id.toString());
        record.setOrderSource(OrderSource.COUNTER);
        record.setLoyaltyMemberId("loyalty-xyz");
        record.setTimestamp(now);
        record.setOrderStatus(OrderStatus.FULFILLED);
        record.setLocation(Location.CHARLOTTE);
        record.setQdca10LineItems(new ArrayList<>());
        record.setQdca10proLineItems(new ArrayList<>());

        assertEquals(id.toString(), record.getOrderId());
        assertEquals(OrderSource.COUNTER, record.getOrderSource());
        assertEquals("loyalty-xyz", record.getLoyaltyMemberId());
        assertEquals(now, record.getTimestamp());
        assertEquals(OrderStatus.FULFILLED, record.getOrderStatus());
        assertEquals(Location.CHARLOTTE, record.getLocation());
        assertNotNull(record.getQdca10LineItems());
        assertNotNull(record.getQdca10proLineItems());
    }

    @Test
    public void testGetLineItemsBothPresent() {
        OrderRecord record = new OrderRecord();
        record.setOrderId(UUID.randomUUID().toString());

        LineItem item1 = new LineItem(Item.QDC_A101, "Alice", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, record);
        LineItem item2 = new LineItem(Item.QDC_A105_Pro01, "Bob", BigDecimal.valueOf(553.00), LineItemStatus.IN_PROGRESS, record);

        List<LineItem> qdca10 = new ArrayList<>();
        qdca10.add(item1);
        List<LineItem> qdca10pro = new ArrayList<>();
        qdca10pro.add(item2);

        record.setQdca10LineItems(qdca10);
        record.setQdca10proLineItems(qdca10pro);

        List<LineItem> all = record.getLineItems();
        assertEquals(2, all.size());
    }

    @Test
    public void testGetLineItemsOnlyQdca10() {
        OrderRecord record = new OrderRecord();
        record.setOrderId(UUID.randomUUID().toString());

        LineItem item1 = new LineItem(Item.QDC_A101, "Alice", BigDecimal.valueOf(135.50), LineItemStatus.IN_PROGRESS, record);
        List<LineItem> qdca10 = new ArrayList<>();
        qdca10.add(item1);
        record.setQdca10LineItems(qdca10);

        List<LineItem> all = record.getLineItems();
        assertEquals(1, all.size());
    }

    @Test
    public void testGetLineItemsEmpty() {
        OrderRecord record = new OrderRecord();
        record.setOrderId(UUID.randomUUID().toString());

        List<LineItem> all = record.getLineItems();
        assertEquals(0, all.size());
    }

    @Test
    public void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        OrderRecord a = new OrderRecord(id, OrderSource.WEB, "m1", now, OrderStatus.PLACED, Location.ATLANTA, null, null);
        OrderRecord b = new OrderRecord(id, OrderSource.WEB, "m1", now, OrderStatus.PLACED, Location.ATLANTA, null, null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    public void testEqualsDifferentId() {
        OrderRecord a = new OrderRecord(UUID.randomUUID(), OrderSource.WEB, null, Instant.now(), OrderStatus.PLACED, Location.ATLANTA, null, null);
        OrderRecord b = new OrderRecord(UUID.randomUUID(), OrderSource.WEB, null, Instant.now(), OrderStatus.PLACED, Location.ATLANTA, null, null);
        assertNotEquals(a, b);
    }

    @Test
    public void testHashCodeWithNullFields() {
        OrderRecord record = new OrderRecord();
        assertDoesNotThrow(record::hashCode);
    }

    @Test
    public void testEqualsWithNullFields() {
        OrderRecord a = new OrderRecord();
        OrderRecord b = new OrderRecord();
        // Both have null orderId → equal
        assertEquals(a, b);
    }

    @Test
    public void testEqualsWithLineItems() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        LineItem li = new LineItem(Item.QDC_A101, "Taro", java.math.BigDecimal.valueOf(135.50), LineItemStatus.PLACED, null);
        OrderRecord a = new OrderRecord(id, OrderSource.WEB, null, now, OrderStatus.PLACED, Location.ATLANTA, Arrays.asList(li), null);
        OrderRecord b = new OrderRecord(id, OrderSource.WEB, null, now, OrderStatus.PLACED, Location.ATLANTA, Arrays.asList(li), null);
        assertEquals(a, b);
        assertNotEquals(a, new OrderRecord(id, OrderSource.WEB, null, now, OrderStatus.PLACED, Location.ATLANTA, new java.util.ArrayList<>(), null));
    }

    @Test
    public void testEqualsFieldDifferences() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        OrderRecord base = new OrderRecord(id, OrderSource.WEB, "member-1", now, OrderStatus.PLACED, Location.ATLANTA, null, null);

        // orderSource differs
        assertNotEquals(base, new OrderRecord(id, OrderSource.COUNTER, "member-1", now, OrderStatus.PLACED, Location.ATLANTA, null, null));
        // loyaltyMemberId differs
        assertNotEquals(base, new OrderRecord(id, OrderSource.WEB, "member-2", now, OrderStatus.PLACED, Location.ATLANTA, null, null));
        // loyaltyMemberId on one side null
        assertNotEquals(base, new OrderRecord(id, OrderSource.WEB, null, now, OrderStatus.PLACED, Location.ATLANTA, null, null));
        // timestamp differs
        assertNotEquals(base, new OrderRecord(id, OrderSource.WEB, "member-1", Instant.now().plusSeconds(1), OrderStatus.PLACED, Location.ATLANTA, null, null));
        // orderStatus differs
        assertNotEquals(base, new OrderRecord(id, OrderSource.WEB, "member-1", now, OrderStatus.FULFILLED, Location.ATLANTA, null, null));
        // location differs
        assertNotEquals(base, new OrderRecord(id, OrderSource.WEB, "member-1", now, OrderStatus.PLACED, Location.TOKYO, null, null));
        // qdca10proLineItems differs
        LineItem li = new LineItem(Item.QDC_A105_Pro01, "Hanako", java.math.BigDecimal.valueOf(553.00), LineItemStatus.PLACED, null);
        assertNotEquals(base, new OrderRecord(id, OrderSource.WEB, "member-1", now, OrderStatus.PLACED, Location.ATLANTA, null, Arrays.asList(li)));
    }

    @Test
    public void testToString() {
        OrderRecord record = new OrderRecord();
        UUID id = UUID.randomUUID();
        record.setOrderId(id.toString());
        record.setOrderSource(OrderSource.WEB);
        String str = record.toString();
        assertTrue(str.contains(id.toString()));
        assertTrue(str.contains("WEB"));
    }
}
