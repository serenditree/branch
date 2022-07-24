package com.serenditree.fence.authorization.repository.api;

import com.serenditree.fence.model.FenceRecord;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.List;


public interface AuthorizationRepositoryApi extends PanacheRepository<FenceRecord> {
    void createFenceRecord(FenceRecord fenceRecord);

    List<FenceRecord> retrieveFenceRecords(String userId, String entityId, String action);

    List<FenceRecord> retrieveFenceRecordsByEntity(String entityId);

    int deleteFenceRecordsByEntity(String entityId);
}
