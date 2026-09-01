package io.serenditree.fence.authentication.service;

import io.serenditree.fence.authentication.service.api.VerificationService;
import io.serenditree.fence.model.FenceIdRecord;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;

@Dependent
public class OidcVerificationService implements VerificationService {

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void createFenceIdRecord(final String subject) {
        this.persist(new FenceIdRecord(subject));
    }

    @Override
    @Transactional(Transactional.TxType.MANDATORY)
    public void deleteFenceIdRecord(final String subject) {
        this.delete(new FenceIdRecord(subject));
    }
}
