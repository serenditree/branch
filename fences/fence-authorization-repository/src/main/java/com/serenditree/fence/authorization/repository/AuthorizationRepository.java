package com.serenditree.fence.authorization.repository;


import com.serenditree.fence.authorization.repository.api.AuthorizationRepositoryApi;
import com.serenditree.fence.model.FenceRecord;

import javax.enterprise.context.Dependent;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repository for {@link FenceRecord} persistence.
 */
@Dependent
public class AuthorizationRepository implements AuthorizationRepositoryApi {

    private static final Logger LOGGER = Logger.getLogger(AuthorizationRepository.class.getName());

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
     * @param entityId Id of the targeted entity.
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
     * @param entityId Id of the targeted entity.
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
     * @param entityId Id of the entity to delete.
     * @return Number of deleted records.
     */
    @Transactional
    public int deleteFenceRecordsByEntity(String entityId) {
        LOGGER.fine(() -> "Deleted fence records for entity " + entityId);
        return this.getEntityManager()
                .createNamedQuery(FenceRecord.DELETE_BY_ENTITY)
                .setParameter(FenceRecord.ENTITY_REFERENCE, entityId)
                .executeUpdate();
    }

    /**
     * Deletes expired {@link FenceRecord}s.
     *
     * @return Number of deleted records.
     */
    @Transactional
    public int deleteFenceRecordsByExpiration() {
        return this.getEntityManager()
                .createNamedQuery(FenceRecord.DELETE_BY_EXPIRATION)
                .setParameter(FenceRecord.EXPIRATION_REFERENCE, LocalDateTime.now())
                .executeUpdate();
    }
}
