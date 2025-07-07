package io.quarkusdroneshop.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.Location;
import io.quarkusdroneshop.counter.domain.OrderSource;
import io.quarkusdroneshop.counter.domain.commands.CommandItem;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PlaceOrderCommandTestUtil {

    private String id;

    private OrderSource orderSource;

    private Location location;

    private String loyaltyMemberId;

    private List<CommandItem> Qdca10LineItems;

    private List<CommandItem> Qdca10proLineItems;

    private Instant timestamp;

    public PlaceOrderCommandTestUtil() {
        this.id = UUID.randomUUID().toString();
    }

    public PlaceOrderCommandTestUtil(String id, OrderSource orderSource, Location location, String loyaltyMemberId, List<CommandItem> Qdca10LineItems, List<CommandItem> Qdca10proLineItems, Instant timestamp) {
        this.id = id;
        this.orderSource = orderSource;
        this.location = location;
        this.loyaltyMemberId = loyaltyMemberId;
        this.Qdca10LineItems = Qdca10LineItems;
        this.Qdca10proLineItems = Qdca10proLineItems;
        this.timestamp = timestamp;
    }

    public PlaceOrderCommandTestUtil create() {
        this.id = UUID.randomUUID().toString();
        return this;
    }

    public void withId(final String id) {
        this.id = id;
    }

    public PlaceOrderCommandTestUtil withBlackCoffee() {
        if (this.Qdca10LineItems == null) {
            this.Qdca10LineItems = new ArrayList<>();
        }
        this.Qdca10LineItems.add(new CommandItem(Item.QDC_A101, "Jerry", BigDecimal.valueOf(3.50)));
        return this;
    }

    public void withBlackCoffeeFor(final String name) {
        this.Qdca10LineItems.add(new CommandItem(Item.QDC_A101, name, BigDecimal.valueOf(3.50)));
    }

    public PlaceOrderCommand build() {
        return new PlaceOrderCommand(
            this.id,
            this.orderSource,
            this.location,
            this.loyaltyMemberId,
            Optional.ofNullable(this.Qdca10LineItems),
            Optional.ofNullable(this.Qdca10proLineItems)
        );
    }
}
