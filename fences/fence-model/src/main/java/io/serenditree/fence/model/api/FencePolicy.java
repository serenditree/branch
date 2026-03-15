package io.serenditree.fence.model.api;

import io.serenditree.fence.model.FenceRecordAssertion;
import io.restassured.path.json.JsonPath;

import java.util.Optional;

public interface FencePolicy {
    Optional<FenceRecordAssertion> apply(JsonPath body, String userId);
}
