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

  protected static Order fromOrderRecord(OrderRecord orderRecord) {
    if (orderRecord == null) {
      throw new IllegalArgumentException("OrderRecord must not be null");
    }
    Order order = new Order();
    order.orderRecord = orderRecord;
    return order;
  }

  public OrderEventResult applyOrderTicketUp(final TicketUp ticketUp) {
    if (this.getQdca10LineItems().isPresent()) {
      this.getQdca10LineItems().get().stream().forEach(lineItem -> {
        if (lineItem.getItemId().equals(ticketUp.lineItemId)) {
          lineItem.setLineItemStatus(LineItemStatus.FULFILLED);
        }
      });
    }
    if (this.getQdca10proLineItems().isPresent()) {
      this.getQdca10proLineItems().get().stream().forEach(lineItem -> {
        if (lineItem.getItemId().equals(ticketUp.lineItemId)) {
          lineItem.setLineItemStatus(LineItemStatus.FULFILLED);
        }
      });
    }

    if (this.getQdca10LineItems().isPresent() && this.getQdca10proLineItems().isPresent()) {
      if (Stream.concat(this.getQdca10LineItems().get().stream(), this.getQdca10proLineItems().get().stream())
              .allMatch(lineItem -> lineItem.getLineItemStatus().equals(LineItemStatus.FULFILLED))) {
        this.setOrderStatus(OrderStatus.FULFILLED);
      }
    } else if (this.getQdca10LineItems().isPresent()) {
      if (this.getQdca10LineItems().get().stream()
              .allMatch(lineItem -> lineItem.getLineItemStatus().equals(LineItemStatus.FULFILLED))) {
        this.setOrderStatus(OrderStatus.FULFILLED);
      }
    } else if (this.getQdca10proLineItems().isPresent()) {
      if (this.getQdca10proLineItems().get().stream()
              .allMatch(lineItem -> lineItem.getLineItemStatus().equals(LineItemStatus.FULFILLED))) {
        this.setOrderStatus(OrderStatus.FULFILLED);
      }
    }

    OrderUpdatedEvent orderUpdatedEvent = OrderUpdatedEvent.of(this);

    OrderUpdate orderUpdate = new OrderUpdate(ticketUp.getOrderId(), ticketUp.getLineItemId(), ticketUp.getName(),
            ticketUp.getItem(), OrderStatus.FULFILLED, ticketUp.madeBy);

    OrderEventResult orderEventResult = new OrderEventResult();
    orderEventResult.setOrder(this);
    orderEventResult.addEvent(orderUpdatedEvent);
    orderEventResult.setOrderUpdates(new ArrayList<>() {{
      add(orderUpdate);
    }});
    return orderEventResult;
  }

  protected static Order fromPlaceOrderCommand(final PlaceOrderCommand placeOrderCommand) {
    logger.debug("creating a new Order from: {}", placeOrderCommand);
    Order order = new Order(placeOrderCommand.getId());
    order.setOrderSource(placeOrderCommand.getOrderSource());
    order.setLocation(placeOrderCommand.getLocation());
    order.setTimestamp(placeOrderCommand.getTimestamp());
    order.setOrderStatus(OrderStatus.IN_PROGRESS);

    if (placeOrderCommand.getLoyaltyMemberId().isPresent()) {
      order.setLoyaltyMemberId(placeOrderCommand.getLoyaltyMemberId().get());
    }

    if (placeOrderCommand.getQdca10LineItems().isPresent()) {
      placeOrderCommand.getQdca10LineItems().get().forEach(commandItem -> {
        logger.info("createOrderFromCommand adding QDCA10Item from {}", commandItem.toString());
        LineItem lineItem = new LineItem(commandItem.getItem(), commandItem.getName(), commandItem.getPrice(),
                LineItemStatus.IN_PROGRESS, order.getOrderRecord());
        order.addQdca10LineItem(lineItem);
      });
    }

    if (placeOrderCommand.getQdca10proLineItems().isPresent()) {
      logger.info("createOrderFromCommand adding QDCA10ProOrders {}",
              placeOrderCommand.getQdca10proLineItems().get().size());
      placeOrderCommand.getQdca10proLineItems().get().forEach(commandItem -> {
        LineItem lineItem = new LineItem(commandItem.getItem(), commandItem.getName(), commandItem.getPrice(),
                LineItemStatus.IN_PROGRESS, order.getOrderRecord());
        order.addQDCA10ProLineItem(lineItem);
      });
    }
    return order;
  }

  private static List<OrderUpdate> createOrderUpdates(Order order) {
    List<OrderUpdate> orderUpdates = new ArrayList<>();
    if (order.getQdca10LineItems().isPresent()) {
      order.getQdca10LineItems().get().forEach(lineItem -> {
        orderUpdates.add(new OrderUpdate(order.getOrderId(), lineItem.getItemId(), lineItem.getName(),
                lineItem.getItem(), OrderStatus.IN_PROGRESS));
      });
    }
    if (order.getQdca10proLineItems().isPresent()) {
      order.getQdca10proLineItems().get().forEach(lineItem -> {
        orderUpdates.add(new OrderUpdate(order.getOrderId(), lineItem.getItemId(), lineItem.getName(),
                lineItem.getItem(), OrderStatus.IN_PROGRESS));
      });
    }
    return orderUpdates;
  }

  private static List<OrderTicket> createOrderTickets(String orderId, List<LineItem> lineItems) {
    List<OrderTicket> orderTickets = new ArrayList<>(lineItems.size());
    lineItems.forEach(lineItem -> {
      orderTickets.add(new OrderTicket(orderId, lineItem.getItemId(), lineItem.getItem(), lineItem.getName()));
    });
    return orderTickets;
  }

  public static OrderEventResult createFromCommand(final PlaceOrderCommand placeOrderCommand) {
    Order order = Order.fromPlaceOrderCommand(placeOrderCommand);
    OrderEventResult orderEventResult = new OrderEventResult();
    orderEventResult.setOrder(order);
    if (order.getQdca10LineItems().isPresent()) {
      orderEventResult.setQdca10Tickets(createOrderTickets(order.getOrderId(), order.getQdca10LineItems().get()));
    }
    if (order.getQdca10proLineItems().isPresent()) {
      orderEventResult.setQdca10proTickets(createOrderTickets(order.getOrderId(), order.getQdca10proLineItems().get()));
    }
    orderEventResult.setOrderUpdates(createOrderUpdates(order));
    orderEventResult.addEvent(OrderCreatedEvent.of(order));
    if (placeOrderCommand.getLoyaltyMemberId().isPresent()) {
      orderEventResult.addEvent(LoyaltyMemberPurchaseEvent.of(order));
    }
    logger.debug("returning {}", orderEventResult);
    return orderEventResult;
  }

  public void addQdca10LineItem(LineItem lineItem) {
    lineItem.setOrder(this.orderRecord);
    if (this.orderRecord.getQdca10LineItems() == null) {
      this.orderRecord.setQdca10LineItems(new ArrayList<>());
    }
    this.orderRecord.getQdca10LineItems().add(lineItem);
  }

  public void addQDCA10ProLineItem(LineItem lineItem) {
    lineItem.setOrder(this.orderRecord);
    if (this.getQdca10proLineItems().isPresent()) {
      this.getQdca10proLineItems().get().add(lineItem);
    } else {
      if (this.orderRecord.getQdca10proLineItems() == null) {
        this.orderRecord.setQdca10proLineItems(new ArrayList<>());
      }
      this.orderRecord.getQdca10proLineItems().add(lineItem);
    }
  }

  public Optional<List<LineItem>> getQdca10LineItems() {
    return Optional.ofNullable(this.orderRecord.getQdca10LineItems());
  }

  public void setQdca10LineItems(List<LineItem> Qdca10LineItems) {
    this.orderRecord.setQdca10LineItems(Qdca10LineItems);
  }

  public Optional<List<LineItem>> getQdca10proLineItems() {
    return Optional.ofNullable(this.orderRecord.getQdca10proLineItems());
  }

  public void setQdca10proLineItems(List<LineItem> Qdca10proLineItems) {
    this.orderRecord.setQdca10proLineItems(Qdca10proLineItems);
  }

  public Optional<String> getLoyaltyMemberId() {
    return Optional.ofNullable(this.orderRecord.getLoyaltyMemberId());
  }

  public void setLoyaltyMemberId(String loyaltyMemberId) {
    this.orderRecord.setLoyaltyMemberId(loyaltyMemberId);
  }

  public Order() {
    this.orderRecord = new OrderRecord();
    this.orderRecord.setOrderId(UUID.randomUUID().toString());
    this.orderRecord.setTimestamp(Instant.now());
  }

  public Order(final String orderId) {
    this.orderRecord = new OrderRecord();
    this.orderRecord.setOrderId(orderId);
    this.orderRecord.setTimestamp(Instant.now());
  }

  public Order(final String orderId, final OrderSource orderSource, final Location location,
               final String loyaltyMemberId, final Instant timestamp, final OrderStatus orderStatus,
               final List<LineItem> Qdca10LineItems, final List<LineItem> Qdca10proLineItems) {
    this.orderRecord.setOrderId(orderId);
    this.orderRecord.setOrderSource(orderSource);
    this.orderRecord.setLocation(location);
    this.orderRecord.setLoyaltyMemberId(loyaltyMemberId);
    this.orderRecord.setTimestamp(timestamp);
    this.orderRecord.setOrderStatus(orderStatus);
    this.orderRecord.setQdca10LineItems(Qdca10LineItems);
    this.orderRecord.setQdca10proLineItems(Qdca10proLineItems);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
            .add("orderId='" + orderRecord.getOrderId() + "'")
            .add("orderSource=" + orderRecord.getOrderSource())
            .add("loyaltyMemberId='" + orderRecord.getLoyaltyMemberId() + "'")
            .add("timestamp=" + orderRecord.getTimestamp())
            .add("orderStatus=" + orderRecord.getOrderStatus())
            .add("location=" + orderRecord.getLocation())
            .add("Qdca10LineItems=" + orderRecord.getQdca10LineItems())
            .add("Qdca10proLineItems=" + orderRecord.getQdca10proLineItems())
            .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Order order = (Order) o;
    return orderRecord != null ? orderRecord.equals(order.orderRecord) : order.orderRecord == null;
  }

  @Override
  public int hashCode() {
    return orderRecord != null ? orderRecord.hashCode() : 0;
  }

  public String getOrderId() {
    return this.orderRecord.getOrderId();
  }

  public OrderSource getOrderSource() {
    return this.orderRecord.getOrderSource();
  }

  public void setOrderSource(OrderSource orderSource) {
    this.orderRecord.setOrderSource(orderSource);
  }

  public Location getLocation() {
    return this.orderRecord.getLocation();
  }

  public void setLocation(Location location) {
    this.orderRecord.setLocation(location);
  }

  public OrderStatus getOrderStatus() {
    return this.orderRecord.getOrderStatus();
  }

  public void setOrderStatus(OrderStatus orderStatus) {
    this.orderRecord.setOrderStatus(orderStatus);
  }

  public Instant getTimestamp() {
    return this.orderRecord.getTimestamp();
  }

  public void setTimestamp(Instant timestamp) {
    this.orderRecord.setTimestamp(timestamp);
  }

  protected OrderRecord getOrderRecord() {
    return this.orderRecord;
  }
}