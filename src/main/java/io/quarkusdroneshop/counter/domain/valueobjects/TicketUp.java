package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;

import java.time.Instant;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TicketUp {

    public String orderId;

    public String lineItemId;

    public Item item;

    public String name;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Instant timestamp;

    public String madeBy;

    @JsonCreator
    public TicketUp(
        @JsonProperty("orderId") String orderId,
        @JsonProperty("lineItemId") String lineItemId,
        @JsonProperty("item") Item item,
        @JsonProperty("name") String name,
        @JsonProperty("timestamp") Object timestamp,
        @JsonProperty("madeBy") String madeBy
    ) {
        this.orderId = orderId;
        this.lineItemId = lineItemId;
        this.item = item;
        this.name = name;
    
        if (timestamp instanceof String) {
            this.timestamp = Instant.parse((String) timestamp);
        } else if (timestamp instanceof Number) {
            this.timestamp = Instant.ofEpochMilli(((Number) timestamp).longValue());
        } else {
            this.timestamp = Instant.now(); // fallback
        }
    
        this.madeBy = madeBy;
    }

    public TicketUp(String orderId, String lineItemId, Item item, String name, String madeBy) {
        this.orderId = orderId;
        this.lineItemId = lineItemId;
        this.item = item;
        this.name = name;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
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

    public String getOrderId() {
        return orderId;
    }

    public String getLineItemId() {
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
}
