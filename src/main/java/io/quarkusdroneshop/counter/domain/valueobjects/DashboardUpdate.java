package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.Item;
import io.quarkusdroneshop.counter.domain.OrderStatus;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.UUID;

@RegisterForReflection
public class DashboardUpdate {
    public final UUID orderId;
    public final UUID itemId;
    public final String name;
    public final Item item;
    public final OrderStatus status;
    public final String madeBy;

    @JsonCreator
    public DashboardUpdate(
        @JsonProperty("orderId") UUID orderId,
        @JsonProperty("itemId") UUID itemId,
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