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

  private final List<CommandItem> Qdca10LineItems;

  private final List<CommandItem> Qdca10proLineItems;

  private final Instant timestamp;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PlaceOrderCommand(
          @JsonProperty("id") final String id,
          @JsonProperty("orderSource") final OrderSource orderSource,
          @JsonProperty("location") final Location location,
          @JsonProperty("loyaltyMemberId") final String loyaltyMemberId,
          @JsonProperty("qdca10LineItems") Optional<List<CommandItem>> Qdca10LineItems,
          @JsonProperty("qdca10proLineItems") Optional<List<CommandItem>> Qdca10proLineItems) {
    this.id = id;
    this.orderSource = orderSource;
    this.location = location;
    this.loyaltyMemberId = loyaltyMemberId;
    if (Qdca10LineItems.isPresent()) {
      this.Qdca10LineItems = Qdca10LineItems.get();
    }else{
      this.Qdca10LineItems = null;
    }
    if (Qdca10proLineItems.isPresent()) {
      this.Qdca10proLineItems = Qdca10proLineItems.get();
    }else{
      this.Qdca10proLineItems = null;
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
            ", Qdca10LineItems=" + Qdca10LineItems +
            ", Qdca10proLineItems=" + Qdca10proLineItems +
            ", timestamp=" + timestamp +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PlaceOrderCommand that = (PlaceOrderCommand) o;
    return Objects.equals(id, that.id) && orderSource == that.orderSource && location == that.location && Objects.equals(loyaltyMemberId, that.loyaltyMemberId) && Objects.equals(Qdca10LineItems, that.Qdca10LineItems) && Objects.equals(Qdca10proLineItems, that.Qdca10proLineItems) && Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, orderSource, location, loyaltyMemberId, Qdca10LineItems, Qdca10proLineItems, timestamp);
  }

  public Optional<List<CommandItem>> getQdca10LineItems() {
    return Optional.ofNullable(Qdca10LineItems);
  }

  public Optional<List<CommandItem>> getQdca10proLineItems() {
    return Optional.ofNullable(Qdca10proLineItems);
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
