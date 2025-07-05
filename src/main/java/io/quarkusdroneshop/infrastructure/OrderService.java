package io.quarkusdroneshop.infrastructure;

import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.Order;
import io.quarkusdroneshop.counter.domain.OrderRepository;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderTicket;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderUpdate;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;
import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class OrderService {

    final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Inject
    ThreadContext threadContext;

    @Inject
    OrderRepository orderRepository;

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Channel("QDCA10")
    Emitter<OrderTicket> QDCA10Emitter;

    @Channel("QDCA10Pro")
    Emitter<OrderTicket> QDCA10ProEmitter;

    @Channel("web-updates")
    Emitter<OrderUpdate> orderUpdateEmitter;

    
    public void onOrderIn(final PlaceOrderCommand placeOrderCommand) {

        logger.debug("onOrderIn {}", placeOrderCommand);

        OrderEventResult orderEventResult = Order.createFromCommand(placeOrderCommand);

        logger.debug("OrderEventResult returned: {}", orderEventResult);

        orderRepository.persist(orderEventResult.getOrder());

        orderEventResult.getOutboxEvents().forEach(exportedEvent -> {
            logger.debug("Firing event: {}", exportedEvent);
            event.fire(exportedEvent);
        });

        if (orderEventResult.getQDCA10Tickets().isPresent()) {
            orderEventResult.getQDCA10Tickets().get().forEach(QDCA10Ticket -> {
                logger.debug("Sending Ticket to QDCA10 Service: {}", QDCA10Ticket);
                QDCA10Emitter.send(QDCA10Ticket);
            });
        }

        if (orderEventResult.getQDCA10ProTickets().isPresent()) {
            orderEventResult.getQDCA10ProTickets().get().forEach(QDCA10ProTicket -> {
                QDCA10ProEmitter.send(QDCA10ProTicket);
            });
        }

        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            orderUpdateEmitter.send(orderUpdate);
        });

    }

    @Transactional
    public void onOrderUp(final TicketUp ticketUp) {

        logger.debug("onOrderUp: {}", ticketUp);
        Order order = orderRepository.findById(ticketUp.getOrderId());

        // null のときはSkip
        if (order == null) {
            logger.error("Order not found for ID: {}", ticketUp.getOrderId());
            throw new NotFoundException("Order not found for ID: " + ticketUp.getOrderId());
        }

        OrderEventResult orderEventResult = order.applyOrderTicketUp(ticketUp);
        logger.debug("OrderEventResult returned: {}", orderEventResult);
        orderRepository.persist(orderEventResult.getOrder());
        orderEventResult.getOrderUpdates().forEach(orderUpdate -> {
            orderUpdateEmitter.send(orderUpdate);
        });
        orderEventResult.getOutboxEvents().forEach(exportedEvent -> {
            event.fire(exportedEvent);
        });
    }

    @Override
    public String toString() {
        return "OrderService{" +
                "threadContext=" + threadContext +
                ", orderRepository=" + orderRepository +
                ", event=" + event +
                ", QDCA10Emitter=" + QDCA10Emitter +
                ", QDCA10ProEmitter=" + QDCA10ProEmitter +
                ", orderUpdateEmitter=" + orderUpdateEmitter +
                '}';
    }

}
