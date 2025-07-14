package io.quarkusdroneshop.counter.domain;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import io.quarkusdroneshop.counter.domain.Order;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<OrderRecord, String> {

    public Order findOrderById(final String orderId) {
        OrderRecord orderRecord = PanacheRepositoryBase.super.findById(orderId);
        return Order.fromOrderRecord(orderRecord);
    }

    public Optional<OrderRecord> findByIdOptional(String orderId) {
        return PanacheRepositoryBase.super.findByIdOptional(orderId);
    }

    public void persist(Order order) {
        persist(order.getOrderRecord());
    }

}
