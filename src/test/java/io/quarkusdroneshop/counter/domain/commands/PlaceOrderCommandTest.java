package io.quarkusdroneshop.counter.domain.commands;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.Location;
import io.quarkusdroneshop.counter.domain.OrderSource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PlaceOrderCommandTest {

    @Test
    public void testConstructorWithAllFields() {
        String id = UUID.randomUUID().toString();
        List<CommandItem> qdca10 = Arrays.asList(new CommandItem(Item.QDC_A101, "Taro", BigDecimal.valueOf(135.50)));
        List<CommandItem> qdca10pro = Arrays.asList(new CommandItem(Item.QDC_A105_Pro01, "Hanako", BigDecimal.valueOf(553.00)));

        PlaceOrderCommand cmd = new PlaceOrderCommand(
            id, OrderSource.WEB, Location.TOKYO, "loyalty-123",
            Optional.of(qdca10), Optional.of(qdca10pro)
        );

        assertEquals(id, cmd.getId());
        assertEquals(OrderSource.WEB, cmd.getOrderSource());
        assertEquals(Location.TOKYO, cmd.getLocation());
        assertTrue(cmd.getLoyaltyMemberId().isPresent());
        assertEquals("loyalty-123", cmd.getLoyaltyMemberId().get());
        assertTrue(cmd.getQdca10LineItems().isPresent());
        assertTrue(cmd.getQdca10proLineItems().isPresent());
        assertNotNull(cmd.getTimestamp());
    }

    @Test
    public void testConstructorWithEmptyOptionals() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(), OrderSource.COUNTER, Location.ATLANTA, null,
            Optional.empty(), Optional.empty()
        );

        assertFalse(cmd.getQdca10LineItems().isPresent());
        assertFalse(cmd.getQdca10proLineItems().isPresent());
        assertFalse(cmd.getLoyaltyMemberId().isPresent());
    }

    @Test
    public void testEqualsAndHashCode() throws Exception {
        String id = UUID.randomUUID().toString();
        PlaceOrderCommand a = new PlaceOrderCommand(
            id, OrderSource.WEB, Location.ATLANTA, "m1", Optional.empty(), Optional.empty()
        );
        PlaceOrderCommand b = new PlaceOrderCommand(
            id, OrderSource.WEB, Location.ATLANTA, "m1", Optional.empty(), Optional.empty()
        );
        // Force same timestamp via reflection
        java.lang.reflect.Field ts = PlaceOrderCommand.class.getDeclaredField("timestamp");
        ts.setAccessible(true);
        ts.set(b, ts.get(a));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(a, a);
        assertNotEquals(a, null);
        assertNotEquals(a, "string");
    }

    @Test
    public void testEqualsFieldDifferences() throws Exception {
        String id = UUID.randomUUID().toString();
        PlaceOrderCommand base = new PlaceOrderCommand(
            id, OrderSource.WEB, Location.ATLANTA, "m1", Optional.empty(), Optional.empty()
        );
        // Different id
        assertNotEquals(base, new PlaceOrderCommand("other-id", OrderSource.WEB, Location.ATLANTA, "m1", Optional.empty(), Optional.empty()));
        // Different orderSource
        assertNotEquals(base, new PlaceOrderCommand(id, OrderSource.COUNTER, Location.ATLANTA, "m1", Optional.empty(), Optional.empty()));
        // Different location
        assertNotEquals(base, new PlaceOrderCommand(id, OrderSource.WEB, Location.TOKYO, "m1", Optional.empty(), Optional.empty()));
        // Different loyaltyMemberId
        assertNotEquals(base, new PlaceOrderCommand(id, OrderSource.WEB, Location.ATLANTA, "m2", Optional.empty(), Optional.empty()));
        // Different qdca10
        assertNotEquals(base, new PlaceOrderCommand(id, OrderSource.WEB, Location.ATLANTA, "m1", Optional.of(Arrays.asList(new CommandItem(Item.QDC_A101, "T", BigDecimal.ONE))), Optional.empty()));
    }

    @Test
    public void testHashCode() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            UUID.randomUUID().toString(), OrderSource.WEB, Location.RALEIGH, null,
            Optional.empty(), Optional.empty()
        );
        assertDoesNotThrow(() -> cmd.hashCode());
    }

    @Test
    public void testToString() {
        PlaceOrderCommand cmd = new PlaceOrderCommand(
            "id-123", OrderSource.PARTNER, Location.CHARLOTTE, "member-X",
            Optional.empty(), Optional.empty()
        );
        String str = cmd.toString();
        assertTrue(str.contains("id-123"));
        assertTrue(str.contains("PARTNER"));
        assertTrue(str.contains("CHARLOTTE"));
    }
}
