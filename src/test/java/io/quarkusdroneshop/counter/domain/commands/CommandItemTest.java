package io.quarkusdroneshop.counter.domain.commands;

import io.quarkusdroneshop.counter.domain.Item;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class CommandItemTest {

    @Test
    public void testConstructorAndGetters() {
        CommandItem item = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        assertEquals(Item.QDC_A101, item.getItem());
        assertEquals("Taro", item.getName());
        assertEquals(BigDecimal.valueOf(135.50), item.getPrice());
    }

    @Test
    public void testPublicFields() {
        CommandItem item = new CommandItem(null, Item.QDC_A102, "Hanako", BigDecimal.valueOf(155.50));
        assertEquals(Item.QDC_A102, item.item);
        assertEquals("Hanako", item.name);
        assertEquals(BigDecimal.valueOf(155.50), item.price);
    }

    @Test
    public void testEquals() {
        CommandItem a = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        CommandItem b = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        assertEquals(a, b);
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    public void testEqualsDifferentFields() {
        CommandItem a = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        CommandItem b = new CommandItem(null, Item.QDC_A102, "Taro", BigDecimal.valueOf(135.50));
        assertNotEquals(a, b);

        CommandItem c = new CommandItem(null, Item.QDC_A101, "Other", BigDecimal.valueOf(135.50));
        assertNotEquals(a, c);

        CommandItem d = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(999.99));
        assertNotEquals(a, d);
    }

    @Test
    public void testHashCode() {
        CommandItem a = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        CommandItem b = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testToString() {
        CommandItem item = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        String str = item.toString();
        assertTrue(str.contains("QDC_A101"));
        assertTrue(str.contains("Taro"));
    }

    @Test
    public void testNullFields() {
        CommandItem a = new CommandItem(null, null, null, null);
        CommandItem b = new CommandItem(null, null, null, null);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testEqualsNullOnOneSide() {
        CommandItem nonNull = new CommandItem(null, Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50));
        // name null on one side
        assertNotEquals(new CommandItem(null, Item.QDC_A101, null, BigDecimal.valueOf(135.50)), nonNull);
        assertNotEquals(nonNull, new CommandItem(null, Item.QDC_A101, null, BigDecimal.valueOf(135.50)));
    }
}
