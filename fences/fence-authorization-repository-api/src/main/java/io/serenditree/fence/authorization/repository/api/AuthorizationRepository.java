package io.serenditree.fence.authorization.repository.api;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.serenditree.fence.model.FenceRecord;

import java.util.List;


public interface AuthorizationRepository extends PanacheRepository<FenceRecord> {
    void createFenceRecord(FenceRecord fenceRecord);

    List<FenceRecord> retrieveFenceRecords(String userId, String entityId, String action);

    List<FenceRecord> retrieveFenceRecordsByEntity(String entityId);

    int deleteFenceRecordsByEntity(String entityId);

    int deleteFenceRecordsByUser(String userId);

    int deleteFenceRecordsByExpiration();
}
