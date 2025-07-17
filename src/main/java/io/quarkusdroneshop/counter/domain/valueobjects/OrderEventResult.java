package io.quarkusdroneshop.counter.domain.valueobjects;

import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.Order;
import io.quarkusdroneshop.infrastructure.OrderService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value object returned from an Order.  Contains the Order aggregate and a List ExportedEvent
 */
public class OrderEventResult {

  final Logger logger = LoggerFactory.getLogger(OrderEventResult.class);

  private Order order;

  private List<ExportedEvent> outboxEvents;

  private List<OrderTicket> Qdca10Tickets;

  private List<OrderTicket> Qdca10proTickets;

  private List<OrderUpdate> orderUpdates;

  public OrderEventResult() {
  }

  public Order getOrder() {
    return order;
  }

  public OrderEventResult(List<OrderUpdate> orderUpdates) {
    this.orderUpdates = orderUpdates;
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

  public void addQdca10Ticket(final OrderTicket orderTicket) {
    if (this.Qdca10Tickets == null) {
      this.Qdca10Tickets = new ArrayList<>();
    }
    this.Qdca10Tickets.add(orderTicket);
  }

  public void addQDdca10proTicket(final OrderTicket orderTicket) {
    if (this.Qdca10proTickets == null) {
      this.Qdca10proTickets = new ArrayList<>();
    }
    this.Qdca10proTickets.add(orderTicket);
  }

  public Optional<List<OrderTicket>> getQdca10Tickets() {
    return Optional.ofNullable(this.Qdca10Tickets);
  }

  public Optional<List<OrderTicket>> getQdca10proTickets() {
    return Optional.ofNullable(this.Qdca10proTickets);
  }

  @Override
  public String toString() {
    return "OrderEventResult{" +
      "order=" + order +
      ", outboxEvents=" + outboxEvents +
      ", Qdca10Tickets=" + Qdca10Tickets +
      ", Qdca10proTickets=" + Qdca10proTickets +
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
    if (Qdca10Tickets != null ? !Qdca10Tickets.equals(that.Qdca10Tickets) : that.Qdca10Tickets != null)
      return false;
    if (Qdca10proTickets != null ? !Qdca10proTickets.equals(that.Qdca10proTickets) : that.Qdca10proTickets != null)
      return false;
    return orderUpdates != null ? orderUpdates.equals(that.orderUpdates) : that.orderUpdates == null;
  }

  @Override
  public int hashCode() {
    int result = getOrder() != null ? getOrder().hashCode() : 0;
    result = 31 * result + (outboxEvents != null ? outboxEvents.hashCode() : 0);
    result = 31 * result + (Qdca10Tickets != null ? Qdca10Tickets.hashCode() : 0);
    result = 31 * result + (Qdca10proTickets != null ? Qdca10proTickets.hashCode() : 0);
    result = 31 * result + (orderUpdates != null ? orderUpdates.hashCode() : 0);
    return result;
  }

  public List<ExportedEvent> getOutboxEvents() {
    return outboxEvents;
  }

  public void setOutboxEvents(List<ExportedEvent> outboxEvents) {
    this.outboxEvents = outboxEvents;
  }

  public void setQdca10Tickets(List<OrderTicket> Qdca10Tickets) {
    this.Qdca10Tickets = Qdca10Tickets;
  }

  public void setQdca10proTickets(List<OrderTicket> Qdca10proTickets) {
    this.Qdca10proTickets = Qdca10proTickets;
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
