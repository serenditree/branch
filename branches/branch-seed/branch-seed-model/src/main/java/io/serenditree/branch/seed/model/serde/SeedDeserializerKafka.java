package io.serenditree.branch.seed.model.serde;

import io.quarkus.kafka.client.serialization.JsonbDeserializer;
import io.serenditree.branch.seed.model.entities.Seed;

public class SeedDeserializerKafka extends JsonbDeserializer<Seed> {

    public SeedDeserializerKafka() {
        super(Seed.class);
    }
}
