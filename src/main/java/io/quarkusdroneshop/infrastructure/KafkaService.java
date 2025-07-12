package io.quarkusdroneshop.infrastructure;

import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;
import io.quarkusdroneshop.counter.domain.valueobjects.DashboardUpdate;
import io.smallrye.reactive.messaging.annotations.Blocking;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class KafkaService {

    Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Inject
    OrderService orderService;

    @Inject
    @Channel("web-updates")
    Emitter<DashboardUpdate> webUpdatesEmitter;

    @Incoming("orders-in")
    @Blocking
    public void orderIn(final PlaceOrderCommand placeOrderCommand) {
        logger.debug("PlaceOrderCommand received: {}", placeOrderCommand);

        // トランザクション内の処理を分離
        OrderEventResult result = orderService.onOrderInTx(placeOrderCommand);

        // トランザクション外でKafka送信
        result.getOrderUpdates().forEach(orderService::sendOrderUpdate);
        result.getQdca10Tickets().ifPresent(list -> list.forEach(orderService::sendQdca10));
        result.getQdca10proTickets().ifPresent(list -> list.forEach(orderService::sendQdca10pro));
    }

    @Incoming("orders-up")
    @Blocking
    public void orderUp(final TicketUp ticketUp) {
        logger.debug("TicketUp received: {}", ticketUp);

        OrderEventResult result = orderService.onOrderUpTx(ticketUp);

        result.getOrderUpdates().forEach(orderService::sendOrderUpdate);
    }
}