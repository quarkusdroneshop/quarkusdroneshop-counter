package io.quarkusdroneshop.counter.domain.valueobjects;

import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.Order;
import io.quarkusdroneshop.infrastructure.OrderService;

import java.util.ArrayList;
import java.util.List;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Value object returned from an Order.  Contains the Order aggregate and a List ExportedEvent
 */
public class OrderEventResult {

  final Logger logger = LoggerFactory.getLogger(OrderEventResult.class);

  private Order order;

  private List<ExportedEvent> outboxEvents;

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

  @Override
  public String toString() {
    return "OrderEventResult{" +
      "order=" + order +
      ", outboxEvents=" + outboxEvents +
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
    return orderUpdates != null ? orderUpdates.equals(that.orderUpdates) : that.orderUpdates == null;
  }

  @Override
  public int hashCode() {
    int result = getOrder() != null ? getOrder().hashCode() : 0;
    result = 31 * result + (outboxEvents != null ? outboxEvents.hashCode() : 0);
    result = 31 * result + (orderUpdates != null ? orderUpdates.hashCode() : 0);
    return result;
  }

  public List<ExportedEvent> getOutboxEvents() {
    return outboxEvents;
  }

  public void setOutboxEvents(List<ExportedEvent> outboxEvents) {
    this.outboxEvents = outboxEvents;
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
