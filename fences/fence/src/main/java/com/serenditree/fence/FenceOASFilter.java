package com.serenditree.fence;

import com.serenditree.fence.model.FenceContext;
import io.smallrye.openapi.internal.models.security.SecurityRequirement;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.Operation;
import org.eclipse.microprofile.openapi.models.PathItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FenceOASFilter implements OASFilter {

    public static final String FENCED = "fenced";
    public static final String SORTED = "sorted";

    private static final String X_FENCED = "x-" + FENCED;
    private static final String X_SORTED = "x-" + SORTED;

    @Override
    public void filterOpenAPI(OpenAPI openAPI) {
        openAPI.getPaths().setPathItems(
            openAPI.getPaths().getPathItems().entrySet().stream().sorted(
                (entrySet1, entrySet2) -> this.compareOperation(
                    this.getOperationsExtensions(entrySet1.getValue()),
                    this.getOperationsExtensions(entrySet2.getValue())
                )
            ).collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (v1, v2) -> v1,
                    LinkedHashMap::new
                )
            )
        );
    }

    @Override
    public Operation filterOperation(Operation operation) {
        if (operation.getExtensions() != null && operation.getExtensions().containsKey(X_FENCED)) {
            operation.setSecurity(List.of(new SecurityRequirement().addScheme(FenceContext.AUTHENTICATION_SCHEME)));
        }

        return operation;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB

    /// /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Map<String, Object> getOperationsExtensions(final PathItem pathItem) {
        return pathItem
            .getOperations()
            .values()
            .iterator()
            .next()
            .getExtensions();
    }

    private int compareOperation(final Map<String, Object> extensions1, final Map<String, Object> extensions2) {
        int compare = 0;

        if (extensions1 != null &&
            extensions2 != null &&
            extensions1.containsKey(X_SORTED) &&
            extensions2.containsKey(X_SORTED)) {
            compare = StringUtils.compare(
                extensions1.get(X_SORTED).toString(),
                extensions2.get(X_SORTED).toString()
            );
        }

        return compare;
    }
}
