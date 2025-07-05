package io.quarkusdroneshop.counter.domain.valueobjects;

import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Value object returned from an Order.  Contains the Order aggregate and a List ExportedEvent
 */
public class OrderEventResult {

  private Order order;

  private List<ExportedEvent> outboxEvents;

  private List<OrderTicket> QDCA10Tickets;

  private List<OrderTicket> QDCA10ProTickets;

  private List<OrderUpdate> orderUpdates;

  public OrderEventResult() {
  }

  public Order getOrder() {
    return order;
  }

  public void addEvent(final ExportedEvent event) {
    if (this.outboxEvents == null) {
      this.outboxEvents = new ArrayList<>();
    }
    this.outboxEvents.add(event);
  }

  public void addUpdate(final OrderUpdate orderUpdate) {
    if (this.orderUpdates == null) {
      this.orderUpdates = new ArrayList<>();
    }
    this.orderUpdates.add(orderUpdate);
  }

  public void addQDCA10Ticket(final OrderTicket orderTicket) {
    if (this.QDCA10Tickets == null) {
      this.QDCA10Tickets = new ArrayList<>();
    }
    this.QDCA10Tickets.add(orderTicket);
  }

  public void addQDCA10ProTicket(final OrderTicket orderTicket) {
    if (this.QDCA10ProTickets == null) {
      this.QDCA10ProTickets = new ArrayList<>();
    }
    this.QDCA10ProTickets.add(orderTicket);
  }

  public Optional<List<OrderTicket>> getQDCA10Tickets() {
    return Optional.ofNullable(this.QDCA10Tickets);
  }

  public Optional<List<OrderTicket>> getQDCA10ProTickets() {
    return Optional.ofNullable(this.QDCA10ProTickets);
  }



  @Override
  public String toString() {
    return "OrderEventResult{" +
      "order=" + order +
      ", outboxEvents=" + outboxEvents +
      ", QDCA10Tickets=" + QDCA10Tickets +
      ", QDCA10ProTickets=" + QDCA10ProTickets +
      ", orderUpdates=" + orderUpdates +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OrderEventResult)) return false;

    OrderEventResult that = (OrderEventResult) o;

    if (getOrder() != null ? !getOrder().equals(that.getOrder()) : that.getOrder() != null) return false;
    if (outboxEvents != null ? !outboxEvents.equals(that.outboxEvents) : that.outboxEvents != null) return false;
    if (QDCA10Tickets != null ? !QDCA10Tickets.equals(that.QDCA10Tickets) : that.QDCA10Tickets != null)
      return false;
    if (QDCA10ProTickets != null ? !QDCA10ProTickets.equals(that.QDCA10ProTickets) : that.QDCA10ProTickets != null)
      return false;
    return orderUpdates != null ? orderUpdates.equals(that.orderUpdates) : that.orderUpdates == null;
  }

  @Override
  public int hashCode() {
    int result = getOrder() != null ? getOrder().hashCode() : 0;
    result = 31 * result + (outboxEvents != null ? outboxEvents.hashCode() : 0);
    result = 31 * result + (QDCA10Tickets != null ? QDCA10Tickets.hashCode() : 0);
    result = 31 * result + (QDCA10ProTickets != null ? QDCA10ProTickets.hashCode() : 0);
    result = 31 * result + (orderUpdates != null ? orderUpdates.hashCode() : 0);
    return result;
  }

  public List<ExportedEvent> getOutboxEvents() {
    return outboxEvents;
  }

  public void setOutboxEvents(List<ExportedEvent> outboxEvents) {
    this.outboxEvents = outboxEvents;
  }

  public void setQDCA10Tickets(List<OrderTicket> QDCA10Tickets) {
    this.QDCA10Tickets = QDCA10Tickets;
  }

  public void setQDCA10ProTickets(List<OrderTicket> QDCA10ProTickets) {
    this.QDCA10ProTickets = QDCA10ProTickets;
  }

  public List<OrderUpdate> getOrderUpdates() {
    return orderUpdates;
  }

  public void setOrderUpdates(List<OrderUpdate> orderUpdates) {
    this.orderUpdates = orderUpdates;
  }

  public void setOrder(final Order order) {
    this.order = order;
  }
}
