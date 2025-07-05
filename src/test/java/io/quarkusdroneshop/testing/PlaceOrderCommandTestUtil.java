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

    private List<CommandItem> QDCA10LineItems;

    private List<CommandItem> QDCA10ProLineItems;

    private Instant timestamp;

    public PlaceOrderCommandTestUtil() {
        this.id = UUID.randomUUID().toString();
    }

    public PlaceOrderCommandTestUtil(String id, OrderSource orderSource, Location location, String loyaltyMemberId, List<CommandItem> QDCA10LineItems, List<CommandItem> QDCA10ProLineItems, Instant timestamp) {
        this.id = id;
        this.orderSource = orderSource;
        this.location = location;
        this.loyaltyMemberId = loyaltyMemberId;
        this.QDCA10LineItems = QDCA10LineItems;
        this.QDCA10ProLineItems = QDCA10ProLineItems;
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
        if (this.QDCA10LineItems == null) {
            this.QDCA10LineItems = new ArrayList<>();
        }
        this.QDCA10LineItems.add(new CommandItem(Item.QDC_A101, "Jerry", BigDecimal.valueOf(3.50)));
        return this;
    }

    public void withBlackCoffeeFor(final String name) {
        this.QDCA10LineItems.add(new CommandItem(Item.QDC_A101, name, BigDecimal.valueOf(3.50)));
    }

    public PlaceOrderCommand build() {
        return new PlaceOrderCommand(
            this.id,
            this.orderSource,
            this.location,
            this.loyaltyMemberId,
            Optional.ofNullable(this.QDCA10LineItems),
            Optional.ofNullable(this.QDCA10ProLineItems)
        );
    }
}
