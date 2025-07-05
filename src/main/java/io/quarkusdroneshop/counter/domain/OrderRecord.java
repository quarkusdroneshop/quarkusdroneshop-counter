package io.quarkusdroneshop.counter.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "Orders")
public class OrderRecord extends PanacheEntityBase {

    @Id
    @Column(nullable = false, unique = true, name = "order_id")
    private String orderId;

    @Enumerated(EnumType.STRING)
    private OrderSource orderSource;

    private String loyaltyMemberId;

    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    private Location location;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
    private List<LineItem> QDCA10LineItems;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
    private List<LineItem> QDCA10ProLineItems;

    public OrderRecord() {
    }

    public OrderRecord(String orderId, OrderSource orderSource, String loyaltyMemberId, Instant timestamp, OrderStatus orderStatus, Location location, List<LineItem> QDCA10LineItems, List<LineItem> QDCA10ProLineItems) {
        this.orderId = orderId;
        this.orderSource = orderSource;
        this.loyaltyMemberId = loyaltyMemberId;
        this.timestamp = timestamp;
        this.orderStatus = orderStatus;
        this.location = location;
        this.QDCA10LineItems = QDCA10LineItems;
        this.QDCA10ProLineItems = QDCA10ProLineItems;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OrderRecord{");
        sb.append("orderId='").append(orderId).append('\'');
        sb.append(", orderSource=").append(orderSource);
        sb.append(", loyaltyMemberId='").append(loyaltyMemberId).append('\'');
        sb.append(", timestamp=").append(timestamp);
        sb.append(", orderStatus=").append(orderStatus);
        sb.append(", location=").append(location);
        sb.append(", QDCA10LineItems=").append(QDCA10LineItems);
        sb.append(", QDCA10ProLineItems=").append(QDCA10ProLineItems);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderRecord that = (OrderRecord) o;

        if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null) return false;
        if (orderSource != that.orderSource) return false;
        if (loyaltyMemberId != null ? !loyaltyMemberId.equals(that.loyaltyMemberId) : that.loyaltyMemberId != null)
            return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        if (orderStatus != that.orderStatus) return false;
        if (location != that.location) return false;
        if (QDCA10LineItems != null ? !QDCA10LineItems.equals(that.QDCA10LineItems) : that.QDCA10LineItems != null)
            return false;
        return QDCA10ProLineItems != null ? QDCA10ProLineItems.equals(that.QDCA10ProLineItems) : that.QDCA10ProLineItems == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (orderSource != null ? orderSource.hashCode() : 0);
        result = 31 * result + (loyaltyMemberId != null ? loyaltyMemberId.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (orderStatus != null ? orderStatus.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (QDCA10LineItems != null ? QDCA10LineItems.hashCode() : 0);
        result = 31 * result + (QDCA10ProLineItems != null ? QDCA10ProLineItems.hashCode() : 0);
        return result;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public OrderSource getOrderSource() {
        return orderSource;
    }

    public void setOrderSource(OrderSource orderSource) {
        this.orderSource = orderSource;
    }

    public String getLoyaltyMemberId() {
        return loyaltyMemberId;
    }

    public void setLoyaltyMemberId(String loyaltyMemberId) {
        this.loyaltyMemberId = loyaltyMemberId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List<LineItem> getQDCA10LineItems() {
        return QDCA10LineItems;
    }

    public void setQDCA10LineItems(List<LineItem> QDCA10LineItems) {
        this.QDCA10LineItems = QDCA10LineItems;
    }

    public List<LineItem> getQDCA10ProLineItems() {
        return QDCA10ProLineItems;
    }

    public void setQDCA10ProLineItems(List<LineItem> QDCA10ProLineItems) {
        this.QDCA10ProLineItems = QDCA10ProLineItems;
    }
}
