package io.quarkusdroneshop.infrastructure;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.counter.domain.LineItem;
import io.quarkusdroneshop.counter.domain.OrderRecord;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Homeoffice の OrderRecordDeserializer が期待する JSON 形式にマッピングするメッセージ。
 * Counter から shop-asite.orders-in トピックに発行する。
 */
@RegisterForReflection
public class HomeofficeOrderMessage {

    @JsonProperty("orderId")
    public String orderId;

    @JsonProperty("orderSource")
    public String orderSource;

    @JsonProperty("location")
    public String location;

    @JsonProperty("loyaltyMemberId")
    public String loyaltyMemberId;

    @JsonProperty("orderPlacedTimestamp")
    public Instant orderPlacedTimestamp;

    @JsonProperty("orderCompletedTimestamp")
    public Instant orderCompletedTimestamp;

    @JsonProperty("qdca10LineItems")
    public List<LineItemMessage> qdca10LineItems;

    @JsonProperty("qdca10proLineItems")
    public List<LineItemMessage> qdca10proLineItems;

    public HomeofficeOrderMessage() {}

    public static HomeofficeOrderMessage from(OrderRecord record) {
        HomeofficeOrderMessage msg = new HomeofficeOrderMessage();
        msg.orderId = record.getOrderId() != null ? record.getOrderId().toString() : null;
        msg.orderSource = record.getOrderSource() != null ? record.getOrderSource().name() : null;
        msg.location = record.getLocation() != null ? record.getLocation().name() : null;
        msg.loyaltyMemberId = record.getLoyaltyMemberId();
        msg.orderPlacedTimestamp = record.getTimestamp();
        msg.orderCompletedTimestamp = null;

        if (record.getQdca10LineItems() != null) {
            msg.qdca10LineItems = record.getQdca10LineItems().stream()
                .map(LineItemMessage::from)
                .collect(Collectors.toList());
        }

        if (record.getQdca10proLineItems() != null) {
            msg.qdca10proLineItems = record.getQdca10proLineItems().stream()
                .map(LineItemMessage::from)
                .collect(Collectors.toList());
        }

        return msg;
    }

    @RegisterForReflection
    public static class LineItemMessage {
        @JsonProperty("id")
        public String id;

        @JsonProperty("item")
        public String item;

        @JsonProperty("name")
        public String name;

        @JsonProperty("price")
        public double price;

        @JsonProperty("preparedBy")
        public String preparedBy;

        public LineItemMessage() {}

        public static LineItemMessage from(LineItem li) {
            LineItemMessage m = new LineItemMessage();
            m.id = li.getItemId();
            m.item = li.getItem() != null ? li.getItem().name() : null;
            m.name = li.getName();
            m.price = li.getPrice() != null ? li.getPrice().doubleValue() : 0.0;
            return m;
        }
    }
}
