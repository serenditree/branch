package io.serenditree.fence.interceptor;

import io.serenditree.fence.annotation.Cleanup;
import io.serenditree.fence.annotation.Fenced;
import io.serenditree.fence.annotation.FencedContext;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import io.serenditree.fence.model.FenceRecord;
import io.serenditree.fence.model.api.FenceEntity;
import io.serenditree.fence.model.api.FencePrincipal;
import io.serenditree.fence.model.enums.FenceActionType;
import io.serenditree.root.util.oak.OakDate;
import io.quarkus.logging.Log;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Intercepts "fenced" methods with {@link Fenced#createOrDeleteRecord()} set to true and creates a
 * {@link FenceRecord}s for
 * returned entities.
 */
@Dependent
@Interceptor
@Fenced(createOrDeleteRecord = true)
@Priority(Interceptor.Priority.APPLICATION + 100)
public class FenceRecordInterceptor {

    private FencePrincipal principal;

    private AuthorizationRepositoryApi authorizationRepository;

    /**
     * Extracts the ID of the returned entity and creates or deletes a {@link FenceRecord}.
     *
     * @param invocationContext Invocation context.
     * @return Unmodified return value of the intercepted method.
     * @throws Exception Handled by framework.
     */
    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {

        Fenced fenced = invocationContext.getMethod().getAnnotation(Fenced.class);
        Log.debugv(
            "Fenced resource: actionBased: {0} recordRequired: {1} recordType: {2}",
            fenced.actionBased(),
            fenced.recordRequired(),
            fenced.recordType().name()
        );

        Object result = invocationContext.proceed();
        Response response = (Response) result;

        if (response.getEntity() instanceof FenceEntity<?> fenceEntity) {
            if (invocationContext.getMethod().isAnnotationPresent(DELETE.class)) {
                this.authorizationRepository.deleteFenceRecordsByEntity(fenceEntity.getId().toString());
                if (invocationContext.getMethod().isAnnotationPresent(Cleanup.class)) {
                    this.authorizationRepository.deleteFenceRecordsByUser(fenceEntity.getId().toString());
                }
            } else if (fenced.recordType() == FenceActionType.METHOD) {
                this.createMethodFenceRecord(fenced, fenceEntity, invocationContext.getMethod().getName());
            } else {
                this.createTypedFenceRecord(fenced.recordType(), fenceEntity);
            }
        } else {
            throw new SecurityException(
                "Fenced endpoint tried to persist an entity which is not an instance of FenceEntity."
            );
        }


        return result;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a record of type "method name".
     *
     * @param fenced Record details.
     * @param entity Target entity
     * @param type   Record type i.e. method name.
     */
    private void createMethodFenceRecord(Fenced fenced, FenceEntity<?> entity, String type) {
        this.authorizationRepository.createFenceRecord(
            new FenceRecord(
                entity.getId().toString(),
                this.principal.getId(),
                type,
                this.buildExpiration(fenced)
            )
        );
    }

    /**
     * Creates a record of predefined type.
     *
     * @param fenceActionType Record type.
     * @param entity          Target entity.
     */
    private void createTypedFenceRecord(FenceActionType fenceActionType, FenceEntity<?> entity) {
        this.authorizationRepository.createFenceRecord(
            new FenceRecord(
                entity.getId().toString(),
                this.principal.getId(),
                fenceActionType.name()
            )
        );
    }

    /**
     * Returns an expiration date based on a relative declaration.
     *
     * @param fenced Annotation containing the expiry information.
     * @return Expiration date.
     */
    private LocalDateTime buildExpiration(Fenced fenced) {

        return fenced.expirationUnit() != ChronoUnit.FOREVER ?
            OakDate.now().plus(fenced.expirationTime(), fenced.expirationUnit()) :
            OakDate.POSITIVE_INFINITY;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setPrincipal(@FencedContext FencePrincipal principal) {
        this.principal = principal;
    }

    @Inject
    public void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }
}
