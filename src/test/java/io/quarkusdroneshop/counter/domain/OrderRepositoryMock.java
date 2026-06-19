package io.quarkusdroneshop.counter.domain;

import io.quarkus.test.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.transaction.Transactional;

@Alternative
@ApplicationScoped
public class OrderRepositoryMock extends OrderRepository{

    private static final Logger logger = LoggerFactory.getLogger(OrderRepositoryMock.class);

    @Override
    public void persist(OrderRecord orderRecord) {
        logger.debug("mocking persist for {}", orderRecord);
    }
}
