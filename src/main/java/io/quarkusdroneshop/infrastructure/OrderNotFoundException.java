package io.quarkusdroneshop.infrastructure;

/**
 * dataproduct-order-events は ORDER_PLACED (orders-in由来) と
 * LINE_ITEM_STATUS_CHANGED/ORDER_CANCELLED (orders-up/eighty-six由来) を
 * Flinkジョブの別々の並列INSERT文から同一トピックの別パーティションに書き込むため、
 * パーティション間の順序保証が無い。そのため後者が先に届き、対象注文がまだ
 * 永続化されていないケースが発生しうる。呼び出し元でリトライ可能にするための例外。
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("Order not found for ID: " + orderId);
    }
}
