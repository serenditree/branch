package io.serenditree.leaf.seed.config;

import io.serenditree.fence.authorization.service.api.PolicyEnforcerApi;
import io.serenditree.fence.model.FenceRecordAssertion;
import io.serenditree.fence.model.api.FencePolicy;
import io.serenditree.fence.model.enums.FenceActionType;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Dependent
@Alternative
public class SeedPolicies implements PolicyEnforcerApi {

    public static final String TEST = "testPolicy";

    private static final Map<String, FencePolicy> POLICIES = Map.of(
        SeedPolicies.TEST,
        (body, userId) -> {
            Optional<FenceRecordAssertion> assertion = Optional.empty();

            if (body.get("parent") != null && body.get("trail") != null && body.getBoolean("trail")) {
                assertion = Optional.of(
                    FenceRecordAssertion.fluentBuilder()
                        .setUserId(userId)
                        .setEntityId(body.get("parent"))
                        .setActionType(FenceActionType.CRUD)
                        .setRecordRequired(true)
                        .build()
                );
            }

            return assertion;
        }
    );

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<FencePolicy> getPolicies(String... policies) {
        return Arrays
            .stream(policies)
            .map(POLICIES::get)
            .toList();
    }
}
