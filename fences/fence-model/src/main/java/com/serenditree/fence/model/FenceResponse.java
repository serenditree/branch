package com.serenditree.fence.model;

import com.serenditree.fence.model.api.FenceEntity;

/**
 * A response that can be used to "crud" {@link FenceRecord}s for an entity after an endpoint returns.
 */
public class FenceResponse implements FenceEntity<Object> {

    private Object id;

    public FenceResponse(Object id) {
        this.id = id;
    }

    @Override
    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }
}
