package io.quarkusdroneshop.counter.infrastructure.orderservice;

import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkusdroneshop.counter.infrastructure.KafkaTestResource;

import java.util.Collections;
import java.util.List;

public class OrderServiceTestProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return Collections.singletonList(new TestResourceEntry(KafkaTestResource.class));
    }
}
