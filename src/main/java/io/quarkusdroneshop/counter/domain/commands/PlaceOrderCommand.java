package io.quarkusdroneshop.counter.domain.commands;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
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

  private final List<CommandItem> qdca10LineItems;

  private final List<CommandItem> qdca10proLineItems;

  private final Instant timestamp;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PlaceOrderCommand(
          @JsonProperty("id") final String id,
          @JsonProperty("orderSource") final OrderSource orderSource,
          @JsonProperty("location") final Location location,
          @JsonProperty("loyaltyMemberId") final String loyaltyMemberId,
          @JsonProperty("qdca10LineItems") Optional<List<CommandItem>> qdca10LineItems,
          @JsonProperty("qdca10proLineItems") Optional<List<CommandItem>> qdca10proLineItems) {
    this.id = id;
    this.orderSource = orderSource;
    this.location = location;
    this.loyaltyMemberId = loyaltyMemberId;
    if (qdca10LineItems.isPresent()) {
      this.qdca10LineItems = qdca10LineItems.get();
    } else {
      this.qdca10LineItems = null;
    }
    if (qdca10proLineItems.isPresent()) {
      this.qdca10proLineItems = qdca10proLineItems.get();
    } else {
      this.qdca10proLineItems = null;
    }
    this.timestamp = Instant.now();
  }

  @Override
  public String toString() {
    return "PlaceOrderCommand{"
            + "id='" + id + '\''
            + ", orderSource=" + orderSource
            + ", location=" + location
            + ", loyaltyMemberId='" + loyaltyMemberId + '\''
            + ", qdca10LineItems=" + qdca10LineItems
            + ", qdca10proLineItems=" + qdca10proLineItems
            + ", timestamp=" + timestamp
            + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlaceOrderCommand that = (PlaceOrderCommand) o;
    return Objects.equals(id, that.id)
            && orderSource == that.orderSource
            && location == that.location
            && Objects.equals(loyaltyMemberId, that.loyaltyMemberId)
            && Objects.equals(qdca10LineItems, that.qdca10LineItems)
            && Objects.equals(qdca10proLineItems, that.qdca10proLineItems)
            && Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, orderSource, location, loyaltyMemberId, qdca10LineItems, qdca10proLineItems, timestamp);
  }

  public Optional<List<CommandItem>> getQdca10LineItems() {
    return Optional.ofNullable(qdca10LineItems);
  }

  public Optional<List<CommandItem>> getQdca10proLineItems() {
    return Optional.ofNullable(qdca10proLineItems);
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
