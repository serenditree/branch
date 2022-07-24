package com.serenditree.fence.model.api;

import com.serenditree.fence.model.FenceRecordAssertion;
import io.restassured.path.json.JsonPath;

import java.util.Optional;

public interface FencePolicy {
    Optional<FenceRecordAssertion> apply(JsonPath body, String userId);
}
