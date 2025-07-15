package io.quarkusdroneshop.counter.domain.events;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkusdroneshop.counter.domain.OrderRecord;
import io.quarkusdroneshop.counter.domain.OrderStatus;
import io.debezium.outbox.quarkus.ExportedEvent;
import io.quarkusdroneshop.counter.domain.LineItem;
import io.quarkusdroneshop.counter.domain.Order;

import java.time.Instant;

public class OrderCreatedEvent implements ExportedEvent<String, JsonNode> {

    private static ObjectMapper mapper = new ObjectMapper();

    private static final String TYPE = "Order";
    private static final String EVENT_TYPE = "OrderCreated";

    private final String orderId;
    private final JsonNode jsonNode;
    private final Instant timestamp;

    private OrderCreatedEvent(String orderId, JsonNode jsonNode, Instant timestamp) {
        this.orderId = orderId;
        this.jsonNode = jsonNode;
        this.timestamp = timestamp;
    }

    public static OrderCreatedEvent of(final Order order) {

        OrderRecord orderRecord = new OrderRecord();
        orderRecord.setOrderId(order.getOrderId());
        orderRecord.setOrderSource(order.getOrderSource());
        orderRecord.setLoyaltyMemberId(order.getLoyaltyMemberId().orElse(null));
        orderRecord.setTimestamp(order.getTimestamp());
        orderRecord.setOrderStatus(OrderStatus.PLACED); // 初期ステータスなど
        orderRecord.setLocation(order.getLocation());
        
        ObjectNode asJson = mapper.createObjectNode()
                .put("orderId", order.getOrderId().toString())
                .put("orderSource", order.getOrderSource().toString())
                .put("timestamp", order.getTimestamp().toString());

        if (order.getQdca10LineItems().isPresent()) {
            ArrayNode Qdca10LineItems = asJson.putArray("Qdca10LineItems") ;
            for (LineItem lineItem : order.getQdca10LineItems().get()) {
                lineItem.setOrder(orderRecord);
                ObjectNode lineAsJon = mapper.createObjectNode()
                        .put("item", lineItem.getItem().toString())
                        .put("name", lineItem.getName());
                Qdca10LineItems.add(lineAsJon);
            }
        }

        if (order.getQdca10proLineItems().isPresent()) {
            ArrayNode Qdca10proLineItems = asJson.putArray("Qdca10proLineItems") ;
            for (LineItem lineItem : order.getQdca10proLineItems().get()) {
                lineItem.setOrder(orderRecord);
                ObjectNode lineAsJon = mapper.createObjectNode()
                        .put("item", lineItem.getItem().toString())
                        .put("name", lineItem.getName());
                Qdca10proLineItems.add(lineAsJon);
            }
        }

        return new OrderCreatedEvent(
                order.getOrderId().toString(),
                asJson,
                order.getTimestamp());
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }

    @Override
    public String getAggregateType() {
        return TYPE;
    }

    @Override
    public JsonNode getPayload() {
        return jsonNode;
    }

    @Override
    public String getType() {
        return EVENT_TYPE;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
}