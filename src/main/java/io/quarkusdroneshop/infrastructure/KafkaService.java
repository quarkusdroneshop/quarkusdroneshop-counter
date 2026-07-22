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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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
        if (placeOrderCommand == null || placeOrderCommand.getId() == null) {
            logger.warn("Received null or invalid PlaceOrderCommand message (missing id): " + placeOrderCommand);
            return;
        }

        logger.debug("PlaceOrderCommand received: {}", placeOrderCommand);

        // トランザクション内の処理を分離
        OrderEventResult result = orderService.onOrderInTx(placeOrderCommand);

        // トランザクション外でKafka送信
        result.getOrderUpdates().forEach(orderService::sendOrderUpdate);
    }

    @Incoming("orders-up")
    @Blocking
    public void orderUp(final TicketUp ticketUp) {
        if (ticketUp == null || ticketUp.getOrderId() == null) {
            logger.warn("Received null or invalid TicketUp message: " + ticketUp);
            return;
        }

        logger.debug("TicketUp received: {}", ticketUp);

        OrderEventResult result = orderService.onOrderUpTx(ticketUp);

        result.getOrderUpdates().forEach(orderService::sendOrderUpdate);
    }
}