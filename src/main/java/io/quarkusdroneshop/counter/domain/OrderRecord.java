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
    private List<LineItem> qdca10LineItems;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "order", cascade = CascadeType.ALL)
    private List<LineItem> qdca10proLineItems;

    public OrderRecord() {
    }

    public OrderRecord(String orderId, OrderSource orderSource, String loyaltyMemberId, Instant timestamp, OrderStatus orderStatus, Location location, List<LineItem> qdca10LineItems, List<LineItem> qdca10proLineItems) {
        this.orderId = orderId;
        this.orderSource = orderSource;
        this.loyaltyMemberId = loyaltyMemberId;
        this.timestamp = timestamp;
        this.orderStatus = orderStatus;
        this.location = location;
        this.qdca10LineItems = qdca10LineItems;
        this.qdca10proLineItems = qdca10proLineItems;
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
        sb.append(", Qdca10LineItems=").append(qdca10LineItems);
        sb.append(", Qdca10proLineItems=").append(qdca10proLineItems);
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
        if (qdca10LineItems != null ? !qdca10LineItems.equals(that.qdca10LineItems) : that.qdca10LineItems != null)
            return false;
        return qdca10proLineItems != null ? qdca10proLineItems.equals(that.qdca10proLineItems) : that.qdca10proLineItems == null;
    }

    @Override
    public int hashCode() {
        int result = orderId != null ? orderId.hashCode() : 0;
        result = 31 * result + (orderSource != null ? orderSource.hashCode() : 0);
        result = 31 * result + (loyaltyMemberId != null ? loyaltyMemberId.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (orderStatus != null ? orderStatus.hashCode() : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (qdca10LineItems != null ? qdca10LineItems.hashCode() : 0);
        result = 31 * result + (qdca10proLineItems != null ? qdca10proLineItems.hashCode() : 0);
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

    public List<LineItem> getQdca10LineItems() {
        return qdca10LineItems;
    }

    public void setQdca10LineItems(List<LineItem> qdca10LineItems) {
        this.qdca10LineItems = qdca10LineItems;
    }

    public List<LineItem> getQdca10proLineItems() {
        return qdca10proLineItems;
    }

    public void setQdca10proLineItems(List<LineItem> qdca10proLineItems) {
        this.qdca10proLineItems = qdca10proLineItems;
    }
}