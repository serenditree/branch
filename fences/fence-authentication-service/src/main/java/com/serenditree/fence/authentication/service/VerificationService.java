package com.serenditree.fence.authentication.service;

import com.serenditree.fence.authentication.service.api.VerificationServiceApi;
import com.serenditree.fence.model.FenceIdRecord;

import javax.enterprise.context.Dependent;
import javax.transaction.Transactional;

@Dependent
public class VerificationService implements VerificationServiceApi {

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
