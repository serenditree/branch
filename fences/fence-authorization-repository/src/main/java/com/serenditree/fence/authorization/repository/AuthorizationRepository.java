package com.serenditree.fence.authorization.repository;


import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.model.FenceRecord;
import com.serenditree.root.util.oak.OakDate;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.Dependent;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.faulttolerance.Retry;

import java.util.List;

/**
 * Repository for {@link FenceRecord} persistence.
 */
@Dependent
@Retry(
    abortOn = {
        EntityExistsException.class,
        EntityNotFoundException.class,
        NonUniqueResultException.class,
        NoResultException.class
    }
)
public class AuthorizationRepository implements AuthorizationRepositoryApi {

    /**
     * Creates a new {@link FenceRecord}.
     *
     * @param fenceRecord {@link FenceRecord}
     */
    @Transactional
    public void createFenceRecord(FenceRecord fenceRecord) {
        this.persist(fenceRecord);
    }

    /**
     * Retrieves all {@link FenceRecord}s for the given user/entity/action-triplet.
     *
     * @param userId   ID of the user.
     * @param entityId ID of the targeted entity.
     * @param action   Type of action.
     * @return List of {@link FenceRecord}s.
     */
    public List<FenceRecord> retrieveFenceRecords(String userId, String entityId, String action) {
        return this.getEntityManager()
            .createNamedQuery(FenceRecord.RETRIEVE, FenceRecord.class)
            .setParameter(FenceRecord.USER_REFERENCE, userId)
            .setParameter(FenceRecord.ENTITY_REFERENCE, entityId)
            .setParameter(FenceRecord.ACTION_REFERENCE, action)
            .getResultList();
    }

    /**
     * Retrieves all {@link FenceRecord}s for the given entity.
     *
     * @param entityId ID of the targeted entity.
     * @return List of {@link FenceRecord}s.
     */
    public List<FenceRecord> retrieveFenceRecordsByEntity(String entityId) {
        return this.getEntityManager()
            .createNamedQuery(FenceRecord.RETRIEVE_BY_ENTITY, FenceRecord.class)
            .setParameter(FenceRecord.ENTITY_REFERENCE, entityId)
            .getResultList();
    }

    /**
     * Cascades deletion to {@link FenceRecord}s.
     *
     * @param entityId ID of the entity to delete.
     * @return Number of deleted records.
     */
    @Transactional
    public int deleteFenceRecordsByEntity(String entityId) {
        int deleted = this.getEntityManager()
            .createNamedQuery(FenceRecord.DELETE_BY_ENTITY)
            .setParameter(FenceRecord.ENTITY_REFERENCE, entityId)
            .executeUpdate();
        Log.debugv("Deleted {0} fence record(s) for entity {1}", deleted, entityId);

        return deleted;
    }

    /**
     * Deletes expired {@link FenceRecord}s.
     *
     * @return Number of deleted records.
     */
    @Transactional
    public int deleteFenceRecordsByExpiration() {
        int deleted = this.getEntityManager()
            .createNamedQuery(FenceRecord.DELETE_BY_EXPIRATION)
            .setParameter(FenceRecord.EXPIRATION_REFERENCE, OakDate.now())
            .executeUpdate();
        Log.debugv("Deleted {0} expired fence record(s)", deleted);

        return deleted;
    }
}
