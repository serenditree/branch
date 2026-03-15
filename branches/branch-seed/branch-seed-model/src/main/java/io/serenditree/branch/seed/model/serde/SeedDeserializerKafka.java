package io.serenditree.branch.seed.model.serde;

import io.serenditree.branch.seed.model.entities.Seed;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class SeedDeserializerKafka extends JsonbDeserializer<Seed> {

    public SeedDeserializerKafka() {
        super(Seed.class);
    }
}
