package com.serenditree.branch.seed.model.serde;

import com.serenditree.root.util.maple.MapleObjectId;
import io.quarkus.jsonb.JsonbConfigCustomizer;
import jakarta.inject.Singleton;
import jakarta.json.bind.JsonbConfig;

@Singleton
public class SeedJsonbConfig implements JsonbConfigCustomizer {

    public void customize(JsonbConfig jsonbConfig) {
        jsonbConfig.withAdapters(new MapleObjectId());
    }
}
