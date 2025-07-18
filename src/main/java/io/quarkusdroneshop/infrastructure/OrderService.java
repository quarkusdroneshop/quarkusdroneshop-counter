package io.quarkusdroneshop.infrastructure;

import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.LineItem;
import io.quarkusdroneshop.counter.domain.LineItemStatus;
import io.quarkusdroneshop.counter.domain.Order;
import io.quarkusdroneshop.counter.domain.OrderRepository;
import io.quarkusdroneshop.counter.domain.OrderStatus;
import io.quarkusdroneshop.counter.domain.OrderRecord;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.valueobjects.DashboardUpdate;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderTicket;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderUpdate;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;
import org.eclipse.microprofile.context.ThreadContext;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;
import java.util.Optional;
import java.util.Collections;

@ApplicationScoped
public class OrderService {

    final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Inject
    ThreadContext threadContext;

    @Inject
    OrderRepository orderRepository;

    @Inject
    Event<ExportedEvent<?, ?>> event;

    @Channel("qdca10")
    Emitter<OrderTicket> qdca10Emitter;

    @Channel("qdca10pro")
    Emitter<OrderTicket> qdca10proEmitter;

    @Channel("web-updates")
    Emitter<DashboardUpdate> dashboardUpdateEmitter;

    @Transactional
    public OrderEventResult onOrderInTx(final PlaceOrderCommand placeOrderCommand) {
        logger.debug("onOrderInTx: {}", placeOrderCommand);

        OrderEventResult result = Order.createFromCommand(placeOrderCommand);
        Order order = result.getOrder();

        OrderRecord orderRecord = new OrderRecord();
        orderRecord.setOrderId(order.getOrderId());
        orderRecord.setOrderSource(order.getOrderSource());
        orderRecord.setLoyaltyMemberId(order.getLoyaltyMemberId().orElse(null));
        orderRecord.setTimestamp(order.getTimestamp());
        orderRecord.setOrderStatus(OrderStatus.PLACED);
        orderRecord.setLocation(order.getLocation());

        if (order.getQdca10LineItems().isPresent()) {
            List<LineItem> lineItems = order.getQdca10LineItems().get();
            for (LineItem item : lineItems) {
                item.setOrder(orderRecord);
            }
            orderRecord.setQdca10LineItems(lineItems);
        }

        if (order.getQdca10proLineItems().isPresent()) {
            List<LineItem> lineItems = order.getQdca10proLineItems().get();
            for (LineItem item : lineItems) {
                item.setOrder(orderRecord);
            }
            orderRecord.setQdca10proLineItems(lineItems);
        }

        orderRepository.persist(orderRecord);
        
        result.getOutboxEvents().forEach(exportedEvent -> {
            logger.debug("Firing event: {}", exportedEvent);
            event.fire(exportedEvent);
        });

        return result;
    }

    @Transactional
    public OrderEventResult onOrderUpTx(TicketUp ticketUp) {
        logger.debug("Handling TicketUp: {}", ticketUp);

        // 該当注文を取得
        OrderRecord orderRecord = orderRepository.findById(ticketUp.getOrderId());
        if (orderRecord == null) {
            logger.warn("Order not found for ID: {}", ticketUp.getOrderId());
            return new OrderEventResult(Collections.emptyList());
        }

        // ステータス更新
        orderRecord.setOrderStatus(OrderStatus.FULFILLED);
        logger.debug("Order status updated to FULFILLED for ID: {}", orderRecord.getOrderId());

        // persistは本来不要なはずだが、明示的に保存
        orderRepository.persist(orderRecord);
        logger.debug("Order persisted after status update");

        // Update通知を作成
        List<OrderUpdate> updates = orderRecord.getLineItems().stream()
            .map(li -> new OrderUpdate(
                orderRecord.getOrderId().toString(),
                li.getItemId().toString(),
                li.getName(),
                li.getItem(),
                li.getLineItemStatus() == LineItemStatus.FULFILLED
                ? LineItemStatus.FULFILLED
                : LineItemStatus.IN_PROGRESS, // ← これなら OK
                (String) null)) // ← String にキャスト
            .collect(Collectors.toList());
    

        return new OrderEventResult(updates);
    }

    public void sendOrderUpdate(OrderUpdate update) {
        logger.debug("Sending DashboardUpdate: {}", update);

        DashboardUpdate dashboardUpdate = new DashboardUpdate(
            update.getOrderId(),
            update.getItemId(),
            update.getName(),
            update.getItem(),
            update.getStatus(),
            update.getMadeBy().orElse("")
        );
        dashboardUpdateEmitter.send(dashboardUpdate);
    }

    public void sendQdca10(OrderTicket ticket) {
        logger.debug("Sending QDCA10 Ticket: {}", ticket);
        qdca10Emitter.send(ticket);
    }

    public void sendQdca10pro(OrderTicket ticket) {
        logger.debug("Sending QDCA10Pro Ticket: {}", ticket);
        qdca10proEmitter.send(ticket);
    }

    @Override
    public String toString() {
        return "OrderService{" +
                "threadContext=" + threadContext +
                ", orderRepository=" + orderRepository +
                ", event=" + event +
                ", qdca10Emitter=" + qdca10Emitter +
                ", qdca10proEmitter=" + qdca10proEmitter +
                ", orderUpdateEmitter=" + dashboardUpdateEmitter +
                '}';
    }
}