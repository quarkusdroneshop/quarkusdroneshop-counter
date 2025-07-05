package io.quarkusdroneshop.counter.domain.valueobjects;

import io.quarkusdroneshop.counter.domain.LineItem;

import java.util.List;

public record OrderRecord(String orderId, List<LineItem> lineItemList) {
}
