package io.quarkusdroneshop.counter.domain;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import io.quarkusdroneshop.counter.domain.Order;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;


@ApplicationScoped
public class OrderRepository implements PanacheRepositoryBase<OrderRecord, UUID> {
}