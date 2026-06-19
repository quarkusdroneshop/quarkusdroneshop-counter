package io.quarkusdroneshop.infrastructure;

import io.quarkus.test.junit.QuarkusTestProfile;
import io.quarkusdroneshop.counter.infrastructure.KafkaTestResource;

import java.util.Collections;
import java.util.List;

public class InfrastructureTestProfile implements QuarkusTestProfile {

    @Override
    public List<TestResourceEntry> testResources() {
        return Collections.singletonList(new TestResourceEntry(KafkaTestResource.class));
    }
}
