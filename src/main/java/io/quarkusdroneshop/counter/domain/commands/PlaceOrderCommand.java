package io.quarkusdroneshop.counter.domain.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.counter.domain.LineItem;
import io.quarkusdroneshop.counter.domain.Location;
import io.quarkusdroneshop.counter.domain.OrderSource;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RegisterForReflection
public class PlaceOrderCommand {

  private final String id;

  private final OrderSource orderSource;

  private final Location location;

  private final String loyaltyMemberId;

  private final List<CommandItem> QDCA10LineItems;

  private final List<CommandItem> QDCA10ProLineItems;

  private final Instant timestamp;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PlaceOrderCommand(
          @JsonProperty("id") final String id,
          @JsonProperty("orderSource") final OrderSource orderSource,
          @JsonProperty("location") final Location location,
          @JsonProperty("loyaltyMemberId") final String loyaltyMemberId,
          @JsonProperty("QDCA10LineItems") Optional<List<CommandItem>> QDCA10LineItems,
          @JsonProperty("QDCA10ProLineItems") Optional<List<CommandItem>> QDCA10ProLineItems) {
    this.id = id;
    this.orderSource = orderSource;
    this.location = location;
    this.loyaltyMemberId = loyaltyMemberId;
    if (QDCA10LineItems.isPresent()) {
      this.QDCA10LineItems = QDCA10LineItems.get();
    }else{
      this.QDCA10LineItems = null;
    }
    if (QDCA10ProLineItems.isPresent()) {
      this.QDCA10ProLineItems = QDCA10ProLineItems.get();
    }else{
      this.QDCA10ProLineItems = null;
    }
    this.timestamp = Instant.now();
  }

  @Override
  public String toString() {
    return "PlaceOrderCommand{" +
            "id='" + id + '\'' +
            ", orderSource=" + orderSource +
            ", location=" + location +
            ", loyaltyMemberId='" + loyaltyMemberId + '\'' +
            ", QDCA10LineItems=" + QDCA10LineItems +
            ", QDCA10ProLineItems=" + QDCA10ProLineItems +
            ", timestamp=" + timestamp +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PlaceOrderCommand that = (PlaceOrderCommand) o;
    return Objects.equals(id, that.id) && orderSource == that.orderSource && location == that.location && Objects.equals(loyaltyMemberId, that.loyaltyMemberId) && Objects.equals(QDCA10LineItems, that.QDCA10LineItems) && Objects.equals(QDCA10ProLineItems, that.QDCA10ProLineItems) && Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, orderSource, location, loyaltyMemberId, QDCA10LineItems, QDCA10ProLineItems, timestamp);
  }

  public Optional<List<CommandItem>> getQDCA10LineItems() {
    return Optional.ofNullable(QDCA10LineItems);
  }

  public Optional<List<CommandItem>> getQDCA10ProLineItems() {
    return Optional.ofNullable(QDCA10ProLineItems);
  }

  public Optional<String> getLoyaltyMemberId() {
    return Optional.ofNullable(loyaltyMemberId);
  }

  public String getId() {
    return id;
  }

  public OrderSource getOrderSource() {
    return orderSource;
  }

  public Location getLocation() {
    return location;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

}
