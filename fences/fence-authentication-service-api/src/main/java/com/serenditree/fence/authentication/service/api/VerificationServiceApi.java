package com.serenditree.fence.authentication.service.api;

import com.serenditree.fence.model.FenceIdRecord;
import io.quarkus.hibernate.orm.panache.PanacheRepository;


public interface VerificationServiceApi extends PanacheRepository<FenceIdRecord> {
    void createFenceIdRecord(String subject);

    void deleteFenceIdRecord(String subject);
}
