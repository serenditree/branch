package com.serenditree.leaf.seed.config;

import com.serenditree.fence.authorization.service.api.PolicyEnforcerApi;
import com.serenditree.fence.model.FenceRecordAssertion;
import com.serenditree.fence.model.api.FencePolicy;
import com.serenditree.fence.model.enums.FenceActionType;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public List<FencePolicy> getPolicies(String... policies) {
        return Arrays
                .stream(policies)
                .map(POLICIES::get)
                .collect(Collectors.toList());
    }
}
