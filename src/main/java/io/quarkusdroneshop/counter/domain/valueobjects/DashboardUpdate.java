package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class DashboardUpdate {
    public final String orderId;
    public final String itemId;
    public final String name;
    public final Item item;
    public final OrderStatus status;
    public final String madeBy;

    @JsonCreator
    public DashboardUpdate(
        @JsonProperty("orderId") String orderId,
        @JsonProperty("itemId") String itemId,
        @JsonProperty("name") String name,
        @JsonProperty("item") Item item,
        @JsonProperty("status") OrderStatus status,
        @JsonProperty("madeBy") String madeBy
    ) {
        this.orderId = orderId;
        this.itemId = itemId;
        this.name = name;
        this.item = item;
        this.status = status;
        this.madeBy = madeBy;
    }
}