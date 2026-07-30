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

    // dataproduct-order-events は ORDER_PLACED と LINE_ITEM_STATUS_CHANGED/ORDER_CANCELLED を
    // Flink側の別々の並列INSERT文から同一トピックの別パーティションに書くため、
    // パーティション間の順序保証が無く、注文作成より先にこのイベントが届くことがある。
    // ここで指数バックオフしながら再試行し、それでも見つからなければ例外を投げて
    // failure-strategy=dead-letter-queue に委ねる (メッセージを失わない)。
    private static final int ORDER_UP_MAX_RETRIES = 5;
    private static final long ORDER_UP_RETRY_BASE_DELAY_MS = 200;

    @Incoming("orders-up")
    @Blocking
    public void orderUp(final TicketUp ticketUp) {
        long startNanos = System.nanoTime();
        logger.debug("orderUp invoked (thread={})", Thread.currentThread().getName());
        try {
            if (ticketUp == null || ticketUp.getOrderId() == null) {
                logger.warn("Received null or invalid TicketUp message: " + ticketUp);
                return;
            }

            logger.debug("TicketUp received: {}", ticketUp);

            OrderEventResult result;
            try {
                result = processWithRetry(ticketUp);
            } catch (OrderNotFoundException e) {
                // リトライを使い切っても見つからない場合、ここで例外を伝播させると
                // channelが停止する。オフセットは進めてメッセージをスキップする。
                logger.error("Dropping TicketUp after exhausting retries: {}", ticketUp, e);
                return;
            }

            result.getOrderUpdates().forEach(orderService::sendOrderUpdate);
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            logger.debug("orderUp completed in {}ms", elapsedMs);
        }
    }

    private OrderEventResult processWithRetry(TicketUp ticketUp) {
        for (int attempt = 1; attempt <= ORDER_UP_MAX_RETRIES; attempt++) {
            try {
                return orderService.onOrderUpTx(ticketUp);
            } catch (OrderNotFoundException e) {
                if (attempt == ORDER_UP_MAX_RETRIES) {
                    logger.error("Giving up after {} attempts: {}", attempt, e.getMessage());
                    throw e;
                }
                long delayMs = ORDER_UP_RETRY_BASE_DELAY_MS * (1L << (attempt - 1));
                logger.warn("Attempt {}/{}: {} — retrying in {}ms",
                        attempt, ORDER_UP_MAX_RETRIES, e.getMessage(), delayMs);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw new IllegalStateException("unreachable");
    }
}