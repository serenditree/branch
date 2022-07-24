package com.serenditree.branch.seed.model.serializer;

import com.serenditree.branch.seed.model.entities.Seed;
import io.quarkus.kafka.client.serialization.JsonbDeserializer;

public class SeedDeserializer extends JsonbDeserializer<Seed> {

    public SeedDeserializer() {
        super(Seed.class);
    }
}
