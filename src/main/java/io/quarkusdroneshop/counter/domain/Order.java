package io.quarkusdroneshop.counter.domain;

import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.events.LoyaltyMemberPurchaseEvent;
import io.quarkusdroneshop.counter.domain.events.OrderCreatedEvent;
import io.quarkusdroneshop.counter.domain.events.OrderUpdatedEvent;
import io.quarkusdroneshop.counter.domain.valueobjects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Transient;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

public class Order {

  @Transient
  static Logger logger = LoggerFactory.getLogger(Order.class);

  private OrderRecord orderRecord;

  public Order() {
    this.orderRecord = new OrderRecord();
    this.orderRecord.setOrderId(UUID.randomUUID());
    this.orderRecord.setTimestamp(Instant.now());
  }

  public Order(final String orderId) {
    this.orderRecord = new OrderRecord();
    this.orderRecord.setOrderId(UUID.fromString(orderId));
    this.orderRecord.setTimestamp(Instant.now());
  }

  public static Order fromOrderRecord(OrderRecord orderRecord) {
    if (orderRecord == null) {
      throw new IllegalArgumentException("OrderRecord must not be null");
    }
    Order order = new Order();
    order.orderRecord = orderRecord;
    return order;
  }

  public static OrderEventResult createFromCommand(final PlaceOrderCommand placeOrderCommand) {
    Order order = fromPlaceOrderCommand(placeOrderCommand);
    OrderEventResult result = new OrderEventResult();
    result.setOrder(order);

    placeOrderCommand.getLoyaltyMemberId().ifPresent(id ->
        result.addEvent(LoyaltyMemberPurchaseEvent.of(order))
    );

    order.getQdca10LineItems().ifPresent(items ->
        result.setQdca10Tickets(createOrderTickets(order.getOrderId().toString(), items))
    );
    order.getQdca10proLineItems().ifPresent(items ->
        result.setQdca10proTickets(createOrderTickets(order.getOrderId().toString(), items))
    );

    result.setOrderUpdates(createOrderUpdates(order));
    result.addEvent(OrderCreatedEvent.of(order));

    logger.debug("returning {}", result);
    return result;
  }

  protected static Order fromPlaceOrderCommand(final PlaceOrderCommand placeOrderCommand) {
    logger.debug("creating a new Order from: {}", placeOrderCommand);
    Order order = new Order(placeOrderCommand.getId());
    order.setOrderSource(placeOrderCommand.getOrderSource());
    order.setLocation(placeOrderCommand.getLocation());
    order.setTimestamp(placeOrderCommand.getTimestamp());
    order.setOrderStatus(OrderStatus.IN_PROGRESS);

    placeOrderCommand.getLoyaltyMemberId().ifPresent(order::setLoyaltyMemberId);

    placeOrderCommand.getQdca10LineItems().ifPresent(items ->
      items.forEach(item -> {
        LineItem lineItem = new LineItem(item.getItem(), item.getName(), item.getPrice(), LineItemStatus.IN_PROGRESS, null);
        order.addQdca10LineItem(lineItem);
      })
    );

    placeOrderCommand.getQdca10proLineItems().ifPresent(items ->
      items.forEach(item -> {
        LineItem lineItem = new LineItem(item.getItem(), item.getName(), item.getPrice(), LineItemStatus.IN_PROGRESS, null);
        order.addQDCA10ProLineItem(lineItem);
      })
    );

    return order;
  }

  public OrderEventResult applyOrderTicketUp(final TicketUp ticketUp) {
    getQdca10LineItems().orElse(Collections.emptyList())
      .forEach(lineItem -> {
        if (lineItem.getItemId().equals(ticketUp.lineItemId)) {
          lineItem.setLineItemStatus(LineItemStatus.FULFILLED);
        }
      });

    getQdca10proLineItems().orElse(Collections.emptyList())
      .forEach(lineItem -> {
        if (lineItem.getItemId().equals(ticketUp.lineItemId)) {
          lineItem.setLineItemStatus(LineItemStatus.FULFILLED);
        }
      });

    boolean allFulfilled = Stream.concat(
        getQdca10LineItems().orElse(Collections.emptyList()).stream(),
        getQdca10proLineItems().orElse(Collections.emptyList()).stream()
    ).allMatch(item -> item.getLineItemStatus() == LineItemStatus.FULFILLED);

    if (allFulfilled) {
      setOrderStatus(OrderStatus.FULFILLED);
    }

    OrderEventResult result = new OrderEventResult();
    result.setOrder(this);
    result.addEvent(OrderUpdatedEvent.of(this));
    result.setOrderUpdates(Collections.singletonList(
        new OrderUpdate(
            ticketUp.getOrderId(),
            ticketUp.getLineItemId(),
            ticketUp.getName(),
            ticketUp.getItem(),
            getOrderStatus(),
            ticketUp.madeBy
        )
    ));
    return result;
  }

  private static List<OrderUpdate> createOrderUpdates(Order order) {
    List<OrderUpdate> updates = new ArrayList<>();
    order.getQdca10LineItems().orElse(Collections.emptyList()).forEach(lineItem ->
        updates.add(new OrderUpdate(
            order.getOrderId().toString(),
            lineItem.getItemId().toString(),
            lineItem.getName(),
            lineItem.getItem(),
            OrderStatus.IN_PROGRESS
        ))
    );
    order.getQdca10proLineItems().orElse(Collections.emptyList()).forEach(lineItem ->
        updates.add(new OrderUpdate(
            order.getOrderId().toString(),
            lineItem.getItemId().toString(),
            lineItem.getName(),
            lineItem.getItem(),
            OrderStatus.IN_PROGRESS
        ))
    );
    return updates;
  }

  private static List<OrderTicket> createOrderTickets(String orderId, List<LineItem> lineItems) {
    List<OrderTicket> tickets = new ArrayList<>(lineItems.size());
    lineItems.forEach(item -> tickets.add(new OrderTicket(
        orderId,
        item.getItemId().toString(),
        item.getItem(),
        item.getName()
    )));
    return tickets;
  }

  public void addQdca10LineItem(LineItem item) {
    item.setOrder(this.orderRecord);
    if (orderRecord.getQdca10LineItems() == null) {
      orderRecord.setQdca10LineItems(new ArrayList<>());
    }
    orderRecord.getQdca10LineItems().add(item);
  }

  public void addQDCA10ProLineItem(LineItem item) {
    item.setOrder(this.orderRecord);
    if (orderRecord.getQdca10proLineItems() == null) {
      orderRecord.setQdca10proLineItems(new ArrayList<>());
    }
    orderRecord.getQdca10proLineItems().add(item);
  }

  public UUID getOrderId() { return orderRecord.getOrderId(); }
  public OrderSource getOrderSource() { return orderRecord.getOrderSource(); }
  public void setOrderSource(OrderSource orderSource) { orderRecord.setOrderSource(orderSource); }
  public Location getLocation() { return orderRecord.getLocation(); }
  public void setLocation(Location location) { orderRecord.setLocation(location); }
  public OrderStatus getOrderStatus() { return orderRecord.getOrderStatus(); }
  public void setOrderStatus(OrderStatus status) { orderRecord.setOrderStatus(status); }
  public Instant getTimestamp() { return orderRecord.getTimestamp(); }
  public void setTimestamp(Instant timestamp) { orderRecord.setTimestamp(timestamp); }
  public Optional<String> getLoyaltyMemberId() { return Optional.ofNullable(orderRecord.getLoyaltyMemberId()); }
  public void setLoyaltyMemberId(String id) { orderRecord.setLoyaltyMemberId(id); }
  public Optional<List<LineItem>> getQdca10LineItems() { return Optional.ofNullable(orderRecord.getQdca10LineItems()); }
  public void setQdca10LineItems(List<LineItem> items) { orderRecord.setQdca10LineItems(items); }
  public Optional<List<LineItem>> getQdca10proLineItems() { return Optional.ofNullable(orderRecord.getQdca10proLineItems()); }
  public void setQdca10proLineItems(List<LineItem> items) { orderRecord.setQdca10proLineItems(items); }
  protected OrderRecord getOrderRecord() { return orderRecord; }

  @Override
  public String toString() {
    return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
        .add("orderId=" + orderRecord.getOrderId())
        .add("orderSource=" + orderRecord.getOrderSource())
        .add("loyaltyMemberId=" + orderRecord.getLoyaltyMemberId())
        .add("timestamp=" + orderRecord.getTimestamp())
        .add("orderStatus=" + orderRecord.getOrderStatus())
        .add("location=" + orderRecord.getLocation())
        .add("qdca10LineItems=" + orderRecord.getQdca10LineItems())
        .add("qdca10proLineItems=" + orderRecord.getQdca10proLineItems())
        .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Order)) return false;
    Order that = (Order) o;
    return Objects.equals(orderRecord, that.orderRecord);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderRecord);
  }
}