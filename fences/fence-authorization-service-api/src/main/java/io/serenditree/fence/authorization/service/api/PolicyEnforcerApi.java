package io.serenditree.fence.authorization.service.api;

import io.serenditree.fence.model.api.FencePolicy;

import java.util.List;

public interface PolicyEnforcerApi {
    List<FencePolicy> getPolicies(String... policies);
}
