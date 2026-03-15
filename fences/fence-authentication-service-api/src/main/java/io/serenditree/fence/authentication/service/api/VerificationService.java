package io.serenditree.fence.authentication.service.api;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.serenditree.fence.model.FenceIdRecord;


public interface VerificationService extends PanacheRepository<FenceIdRecord> {
    void createFenceIdRecord(String subject);

    void deleteFenceIdRecord(String subject);
}
