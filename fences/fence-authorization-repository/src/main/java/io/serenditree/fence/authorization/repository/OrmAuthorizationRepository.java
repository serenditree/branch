package io.serenditree.fence.authorization.repository;


import io.quarkus.logging.Log;
import io.serenditree.fence.authorization.repository.api.AuthorizationRepository;
import io.serenditree.fence.model.FenceRecord;
import io.serenditree.root.util.oak.OakDate;
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
public class OrmAuthorizationRepository implements AuthorizationRepository {

    /**
     * Creates a new {@link FenceRecord}.
     *
     * @param fenceRecord {@link FenceRecord}
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
     * Removes {@link FenceRecord}s after user-deletion.
     *
     * @param userId ID of the deleted user.
     * @return Number of deleted records.
     */
    @Override
    @Transactional
    public int deleteFenceRecordsByUser(String userId) {
        int deleted = this.getEntityManager()
            .createNamedQuery(FenceRecord.DELETE_BY_USER)
            .setParameter(FenceRecord.USER_REFERENCE, userId)
            .executeUpdate();
        Log.debugv("Deleted {0} fence record(s) for user {1}", deleted, userId);

        return deleted;
    }

    /**
     * Deletes expired {@link FenceRecord}s.
     *
     * @return Number of deleted records.
     */
    @Override
    @Transactional
    public int deleteFenceRecordsByExpiration() {
        int deleted = this.getEntityManager()
            .createNamedQuery(FenceRecord.DELETE_BY_EXPIRATION)
            .setParameter(FenceRecord.EXPIRATION_REFERENCE, OakDate.now())
            .executeUpdate();
        Log.infov("Deleted {0} expired fence record(s)", deleted);

        return deleted;
    }
}
