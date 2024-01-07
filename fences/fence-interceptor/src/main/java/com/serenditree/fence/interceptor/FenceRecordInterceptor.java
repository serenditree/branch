package com.serenditree.fence.interceptor;

import com.serenditree.fence.annotation.Fenced;
import com.serenditree.fence.annotation.FencedContext;
import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.fence.model.api.FenceEntity;
import com.serenditree.fence.model.api.FencePrincipal;
import com.serenditree.fence.model.enums.FenceActionType;
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
import java.util.logging.Logger;

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

    public static final Logger LOGGER = Logger.getLogger(FenceRecordInterceptor.class.getName());

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
        LOGGER.fine(() ->
                        "Fenced resource:" +
                        " actionBased: " + fenced.actionBased() +
                        " recordRequired: " + fenced.recordRequired() +
                        " recordType: " + fenced.recordType().name()
        );

        Object result = invocationContext.proceed();
        Response response = (Response) result;

        if (response.getEntity() instanceof FenceEntity) {
            FenceEntity<?> entity = (FenceEntity) response.getEntity();
            if (invocationContext.getMethod().isAnnotationPresent(DELETE.class)) {
                this.authorizationRepository.deleteFenceRecordsByEntity(entity.getId().toString());
            } else if (fenced.recordType() == FenceActionType.METHOD) {
                this.createMethodFenceRecord(fenced, entity, invocationContext.getMethod().getName());
            } else {
                this.createTypedFenceRecord(fenced.recordType(), entity);
            }
        } else {
            throw new SecurityException(
                "Fenced endpoint tried to persist an entity which is not an instance of FenceEntity."
            );
        }


        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SUB
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
                this.principal.getId().toString(),
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
                this.principal.getId().toString(),
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
        LocalDateTime now = LocalDateTime.now();

        return fenced.expirationUnit() != ChronoUnit.FOREVER ?
            now.plus(fenced.expirationTime(), fenced.expirationUnit()) :
            null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // CDI
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public void setPrincipal(@FencedContext FencePrincipal principal) {
        this.principal = principal;
    }

    @Inject
    public void setAuthorizationRepository(AuthorizationRepositoryApi authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }
}
