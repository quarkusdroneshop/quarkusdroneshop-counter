package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.OrderStatus;

import java.time.Instant;
import java.util.StringJoiner;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TicketUp {

    public UUID orderId;

    public UUID lineItemId;

    public Item item;

    public String name;

    public OrderStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant timestamp;

    public String madeBy;

    @JsonCreator
    public TicketUp(
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("lineItemId") UUID lineItemId,
        @JsonProperty("item") Item item,
        @JsonProperty("name") String name,
        @JsonProperty("timestamp") Object timestamp,
        @JsonProperty("status") OrderStatus status,
        @JsonProperty("madeBy") String madeBy
    ) {
        this.orderId = orderId;
        this.lineItemId = lineItemId;
        this.item = item;
        this.name = name;
        this.status = status; // ここを追加
        if (timestamp instanceof String) {
            this.timestamp = Instant.parse((String) timestamp);
        } else if (timestamp instanceof Number) {
            this.timestamp = Instant.ofEpochMilli(((Number) timestamp).longValue());
        } else {
            this.timestamp = Instant.now();
        }
        this.madeBy = madeBy;
    }

    public TicketUp(UUID orderId, UUID lineItemId, Item item, String name, String madeBy) {
        this.orderId = orderId;
        this.lineItemId = lineItemId;
        this.item = item;
        this.name = name;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.madeBy = madeBy;
    }

    public TicketUp(UUID orderId, UUID lineItemId, Item item, String name, OrderStatus status, String madeBy) {
        this.orderId = orderId;
        this.lineItemId = lineItemId;
        this.item = item;
        this.name = name;
        this.status = status;
        this.madeBy = madeBy;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", TicketUp.class.getSimpleName() + "[", "]")
                .add("orderId='" + orderId + "'")
                .add("lineItemId='" + lineItemId + "'")
                .add("item=" + item)
                .add("name='" + name + "'")
                .add("timestamp=" + timestamp)
                .add("status=" + status)
                .add("madeBy='" + madeBy + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TicketUp ticketUp = (TicketUp) o;

        if (orderId != null ? !orderId.equals(ticketUp.orderId) : ticketUp.orderId != null) return false;
        if (lineItemId != null ? !lineItemId.equals(ticketUp.lineItemId) : ticketUp.lineItemId != null) return false;
        if (item != ticketUp.item) return false;
        if (name != null ? !name.equals(ticketUp.name) : ticketUp.name != null) return false;
        if (timestamp != null ? !timestamp.equals(ticketUp.timestamp) : ticketUp.timestamp != null) return false;
        return madeBy != null ? madeBy.equals(ticketUp.madeBy) : ticketUp.madeBy == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (lineItemId != null ? lineItemId.hashCode() : 0);
        result = 31 * result + (item != null ? item.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (madeBy != null ? madeBy.hashCode() : 0);
        return result;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getLineItemId() {
        return lineItemId;
    }

    public Item getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMadeBy() {
        return madeBy;
    }

    public OrderStatus getStatus() {
        return status;
    }
}