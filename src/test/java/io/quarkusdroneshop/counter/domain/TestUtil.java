package io.quarkusdroneshop.counter.domain;

import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.commands.CommandItem;
import io.quarkusdroneshop.counter.domain.commands.PlaceOrderCommand;
import io.quarkusdroneshop.counter.domain.events.OrderCreatedEvent;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderEventResult;
import io.quarkusdroneshop.counter.domain.valueobjects.OrderTicket;
import io.quarkusdroneshop.counter.domain.valueobjects.TicketUp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TestUtil {

    public static PlaceOrderCommand stubPlaceOrderCommand(final String id) {
        return new PlaceOrderCommand(
                id,
                OrderSource.WEB,
                Location.ATLANTA,
                UUID.randomUUID().toString(),
                Optional.of(stubSingleQDCA10Item()),
                Optional.empty());
    }

    public static PlaceOrderCommand stubPlaceOrderCommand() {
        return stubPlaceOrderCommand(UUID.randomUUID().toString());
    };

    private static List<CommandItem> stubSingleQDCA10Item() {
        return Arrays.asList(new CommandItem(Item.QDC_A101, "Foo", BigDecimal.valueOf(135.50)));
    }

    private static List<CommandItem> stubSingleQDCA10ProItem() {
        return Arrays.asList(new CommandItem(Item.QDC_A105_Pro01, "Foo", BigDecimal.valueOf(553.00)));
    }

    public static Order stubOrder() {
        OrderRecord orderRecord = new OrderRecord(
                UUID.randomUUID().toString(),
                OrderSource.COUNTER,
                null,
                Instant.now(),
                OrderStatus.PLACED,
                Location.ATLANTA,
                null,
                null);

        Order order = Order.fromOrderRecord(orderRecord);

        order.getQDCA10LineItems(new LineItem(Item.QDC_A101, "Rocky", BigDecimal.valueOf(3.00), LineItemStatus.PLACED, orderRecord));
        return order;
    }

    public static OrderEventResult stubOrderEventResult() {

        // create the return value
        OrderEventResult orderEventResult = new OrderEventResult();

        // build the order from the PlaceOrderCommand
        Order order = new Order(UUID.randomUUID().toString());
        order.setOrderSource(OrderSource.WEB);
        order.setLocation(Location.ATLANTA);
        order.setTimestamp(Instant.now());
        order.setOrderStatus(OrderStatus.IN_PROGRESS);

        orderEventResult.setOrder(order);
        orderEventResult.setQDCA10Tickets(TestUtil.stubQDCA10Tickets());
        orderEventResult.setOutboxEvents(mockOrderInEvent());
        return orderEventResult;
    }

    private static List<ExportedEvent> mockOrderInEvent() {
        return Arrays.asList(OrderCreatedEvent.of(stubOrder()));
    }

    private static List<OrderTicket> stubQDCA10Tickets() {
        return Arrays.asList(new OrderTicket(UUID.randomUUID().toString(), UUID.randomUUID().toString(), Item.QDC_A101, "Rocky"));
    }

    public static TicketUp stubOrderTicketUp() {

        return new TicketUp(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                Item.QDC_A101,
                "Capt. Kirk",
                "Mr. Spock"
        );
    }

    public static PlaceOrderCommand stubPlaceOrderCommandSingleQDCA10Pro() {

        return new PlaceOrderCommand(
                UUID.randomUUID().toString(),
                OrderSource.WEB,
                Location.ATLANTA,
                UUID.randomUUID().toString(),
                Optional.empty(),
                Optional.of(stubSingleQDCA10ProItem()));

    }

    public static PlaceOrderCommand stubPlaceOrderCommandQDCA10AndQDCA10Pro() {

        return new PlaceOrderCommand(
                UUID.randomUUID().toString(),
                OrderSource.WEB,
                Location.ATLANTA,
                UUID.randomUUID().toString(),
                Optional.of(stubSingleQDCA10Item()),
                Optional.of(stubSingleQDCA10ProItem()));
    }
}
