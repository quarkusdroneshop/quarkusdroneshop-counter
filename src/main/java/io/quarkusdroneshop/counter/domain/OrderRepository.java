package io.quarkusdroneshop.counter.domain;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

import io.quarkusdroneshop.counter.domain.Order;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderRepository implements PanacheRepository<OrderRecord> {
}