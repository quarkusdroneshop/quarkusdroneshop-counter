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

  /**
   * Each time a TicketUp is received the Order should be checked for completion.
   * An Order is complete when every LineItem is fulfilled.
   *
   * @param ticketUp
   * @return OrderEventResult
   */
  public OrderEventResult applyOrderTicketUp(final TicketUp ticketUp) {

    // set the LineItem's new status
    if (this.getQDCA10LineItems().isPresent()) {
      this.getQDCA10LineItems().get().stream().forEach(lineItem -> {
        if(lineItem.getItemId().equals(ticketUp.lineItemId)){
          lineItem.setLineItemStatus(LineItemStatus.FULFILLED);
        }
      });
    }
    if (this.getQDCA10ProLineItems().isPresent()) {
      this.getQDCA10ProLineItems().get().stream().forEach(lineItem -> {
        if(lineItem.getItemId().equals(ticketUp.lineItemId)){
          lineItem.setLineItemStatus(LineItemStatus.FULFILLED);
        }
      });
    }

    // if there are both QDCA10 and QDCA10Pro items concatenate them before checking status
    if (this.getQDCA10LineItems().isPresent() && this.getQDCA10ProLineItems().isPresent()) {
      // check the status of the Order itself and update if necessary
      if(Stream.concat(this.getQDCA10LineItems().get().stream(), this.getQDCA10ProLineItems().get().stream())
              .allMatch(lineItem -> {
                return lineItem.getLineItemStatus().equals(LineItemStatus.FULFILLED);
              })){
        this.setOrderStatus(OrderStatus.FULFILLED);
      };
    } else if (this.getQDCA10LineItems().isPresent()) {
      if(this.getQDCA10LineItems().get().stream()
              .allMatch(lineItem -> {
                return lineItem.getLineItemStatus().equals(LineItemStatus.FULFILLED);
              })){
        this.setOrderStatus(OrderStatus.FULFILLED);
      };
    }else if (this.getQDCA10ProLineItems().isPresent()) {
      if(this.getQDCA10ProLineItems().get().stream()
              .allMatch(lineItem -> {
                return lineItem.getLineItemStatus().equals(LineItemStatus.FULFILLED);
              })){
        this.setOrderStatus(OrderStatus.FULFILLED);
      };
    }

    // create the domain event
    OrderUpdatedEvent orderUpdatedEvent = OrderUpdatedEvent.of(this);

    // create the update value object
    OrderUpdate orderUpdate = new OrderUpdate(ticketUp.getOrderId(), ticketUp.getLineItemId(), ticketUp.getName(), ticketUp.getItem(), OrderStatus.FULFILLED, ticketUp.madeBy);

    OrderEventResult orderEventResult = new OrderEventResult();
    orderEventResult.setOrder(this);
    orderEventResult.addEvent(orderUpdatedEvent);
    orderEventResult.setOrderUpdates(new ArrayList<>() {{
      add(orderUpdate);
    }});
    return orderEventResult;
  }

  /**
   * Create a new Order from a PlaceOrderCommand
   *
   * @param placeOrderCommand
   * @return
   */
  protected static Order fromPlaceOrderCommand(final PlaceOrderCommand placeOrderCommand) {

    logger.debug("creating a new Order from: {}", placeOrderCommand);

    // build the order from the PlaceOrderCommand
    Order order = new Order(placeOrderCommand.getId());
    order.setOrderSource(placeOrderCommand.getOrderSource());
    order.setLocation(placeOrderCommand.getLocation());
    order.setTimestamp(placeOrderCommand.getTimestamp());
    order.setOrderStatus(OrderStatus.IN_PROGRESS);
    if (placeOrderCommand.getLoyaltyMemberId().isPresent()) {
      order.setLoyaltyMemberId(placeOrderCommand.getLoyaltyMemberId().get());
    }

    if (placeOrderCommand.getQDCA10LineItems().isPresent()) {
      placeOrderCommand.getQDCA10LineItems().get().forEach(commandItem -> {
        logger.debug("createOrderFromCommand adding QDCA10Item from {}", commandItem.toString());
        LineItem lineItem = new LineItem(commandItem.getItem(), commandItem.getName(), commandItem.getPrice(), LineItemStatus.IN_PROGRESS, order.getOrderRecord());
        order.getQDCA10LineItems(lineItem);
      });
    }

    if (placeOrderCommand.getQDCA10ProLineItems().isPresent()) {
      logger.debug("createOrderFromCommand adding QDCA10ProOrders {}", placeOrderCommand.getQDCA10ProLineItems().get().size());
      placeOrderCommand.getQDCA10ProLineItems().get().forEach(commandItem -> {
        LineItem lineItem = new LineItem(commandItem.getItem(), commandItem.getName(), commandItem.getPrice(), LineItemStatus.IN_PROGRESS, order.getOrderRecord());
        order.addQDCA10ProLineItem(lineItem);
      });
    }

    return order;
  }

  private static List<OrderUpdate> createOrderUpdates(Order order) {

    List<OrderUpdate> orderUpdates = new ArrayList<>();

    // create required QDCA10Ticket, QDCA10ProTicket, and OrderUpdate value objects
    if (order.getQDCA10LineItems().isPresent()) {
      order.getQDCA10LineItems().get().forEach(lineItem -> {
        orderUpdates.add(new OrderUpdate(order.getOrderId(), lineItem.getItemId(), lineItem.getName(), lineItem.getItem(), OrderStatus.IN_PROGRESS));
      });
    }
    if (order.getQDCA10ProLineItems().isPresent()) {
      order.getQDCA10ProLineItems().get().forEach(lineItem -> {
        orderUpdates.add(new OrderUpdate(order.getOrderId(), lineItem.getItemId(), lineItem.getName(), lineItem.getItem(), OrderStatus.IN_PROGRESS));
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
  /**
   * Creates and returns a new OrderEventResult containing the Order aggregate built from the PlaceOrderCommand
   * and an OrderCreatedEvent
   *
   * @param placeOrderCommand PlaceOrderCommand
   * @return OrderEventResult
   */
  public static OrderEventResult createFromCommand(final PlaceOrderCommand placeOrderCommand) {

    Order order = Order.fromPlaceOrderCommand(placeOrderCommand);

    // create the return value
    OrderEventResult orderEventResult = new OrderEventResult();
    orderEventResult.setOrder(order);

    // create required QDCA10Ticket, QDCA10ProTicket, and OrderUpdate value objects
    if (order.getQDCA10LineItems().isPresent()) {
      orderEventResult.setQDCA10Tickets(createOrderTickets(order.getOrderId(), order.getQDCA10LineItems().get()));
    }

    if (order.getQDCA10ProLineItems().isPresent()) {
      orderEventResult.setQDCA10ProTickets(createOrderTickets(order.getOrderId(), order.getQDCA10ProLineItems().get()));
    }

    // add updates
    orderEventResult.setOrderUpdates(createOrderUpdates(order));

    orderEventResult.addEvent(OrderCreatedEvent.of(order));

    // if this order was placed by a Loyalty Member add the appropriate event
    if (placeOrderCommand.getLoyaltyMemberId().isPresent()) {
      orderEventResult.addEvent(LoyaltyMemberPurchaseEvent.of(order));
    }

    logger.debug("returning {}", orderEventResult);
    return orderEventResult;
  }


  /**
   * Convenience method to prevent Null Pointer Exceptions
   *
   * @param lineItem
   */
  public void getQDCA10LineItems(LineItem lineItem) {
    if (getQDCA10LineItems().isPresent()) {
      lineItem.setOrder(this.orderRecord);
      this.getQDCA10LineItems().get().add(lineItem);
    }else{
      if (this.orderRecord.getQDCA10LineItems() == null) {
        this.orderRecord.setQDCA10LineItems(new ArrayList<LineItem>(){{ add(lineItem); }});
      }else{
        this.orderRecord.getQDCA10LineItems().add(lineItem);
      }
    }
  }

  /**
   * Convenience method to prevent Null Pointer Exceptions
   *
   * @param lineItem
   */
  public void addQDCA10ProLineItem(LineItem lineItem) {
    if (this.getQDCA10ProLineItems().isPresent()) {
      lineItem.setOrder(this.orderRecord);
      this.getQDCA10ProLineItems().get().add(lineItem);
    }else {
      if (this.orderRecord.getQDCA10ProLineItems() == null) {
        this.orderRecord.setQDCA10ProLineItems(new ArrayList<LineItem>(){{ add(lineItem); }});
      }else{
        this.orderRecord.getQDCA10ProLineItems().add(lineItem);
      }
    }
  }

  public Optional<List<LineItem>> getQDCA10LineItems() {
    return Optional.ofNullable(this.orderRecord.getQDCA10LineItems());
  }

  public void setQDCA10LineItems(List<LineItem> QDCA10LineItems) {
    this.orderRecord.setQDCA10LineItems(QDCA10LineItems);
  }

  public Optional<List<LineItem>> getQDCA10ProLineItems() {
    return Optional.ofNullable(this.orderRecord.getQDCA10ProLineItems());
  }

  public void setQDCA10ProLineItems(List<LineItem> QDCA10ProLineItems) {
    this.orderRecord.setQDCA10ProLineItems(QDCA10ProLineItems);
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

  public Order(final String orderId){
    this.orderRecord = new OrderRecord();
    this.orderRecord.setOrderId(orderId);
    this.orderRecord.setTimestamp(Instant.now());
  }

  public Order(final String orderId, final OrderSource orderSource, final Location location, final String loyaltyMemberId, final Instant timestamp, final OrderStatus orderStatus, final List<LineItem> QDCA10LineItems, final List<LineItem> QDCA10ProLineItems) {
    this.orderRecord.setOrderId(orderId);
    this.orderRecord.setOrderSource(orderSource);
    this.orderRecord.setLocation(location);
    this.orderRecord.setLoyaltyMemberId(loyaltyMemberId);
    this.orderRecord.setTimestamp(timestamp);
    this.orderRecord.setOrderStatus(orderStatus);
    this.orderRecord.setQDCA10LineItems(QDCA10LineItems);
    this.orderRecord.setQDCA10ProLineItems(QDCA10ProLineItems);
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
            .add("QDCA10LineItems=" + orderRecord.getQDCA10LineItems())
            .add("QDCA10ProLineItems=" + orderRecord.getQDCA10ProLineItems())
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
