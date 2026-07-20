package io.quarkusdroneshop.counter.domain.commands;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkusdroneshop.counter.domain.Item;

import java.math.BigDecimal;
import java.util.StringJoiner;
import java.util.UUID;

@RegisterForReflection
public class CommandItem {

    // Web 側 (OrderLineItem) で採番された itemId をそのまま引き継ぐ。
    // dataproduct-order-events (orders-in 由来) と orders-up (QDCA10/QDCA10pro
    // 発行) を同じ itemId で突合するために必要。未指定 (テスト等) の場合のみ
    // ここで新規採番する。
    public final String itemId;

    public final Item item;

    public final String name;

    public final BigDecimal price;

    public CommandItem(String itemId, Item item, String name, BigDecimal price) {
        this.itemId = itemId != null ? itemId : UUID.randomUUID().toString();
        this.item = item;
        this.name = name;
        this.price = price;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", CommandItem.class.getSimpleName() + "[", "]")
                .add("itemId='" + itemId + "'")
                .add("item=" + item)
                .add("name='" + name + "'")
                .add("price=" + price)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandItem that = (CommandItem) o;

        if (item != that.item) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return price != null ? price.equals(that.price) : that.price == null;
    }

    @Override
    public int hashCode() {
        int result = item != null ? item.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (price != null ? price.hashCode() : 0);
        return result;
    }

    public String getItemId() {
        return itemId;
    }

    public Item getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
