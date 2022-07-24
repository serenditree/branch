package com.serenditree.root.rest.cache;

import com.serenditree.root.rest.cache.annotation.CustomCacheControl;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.ws.rs.core.CacheControl;

@Dependent
public class CacheControlProducer {

    public static final int DAY_IN_SECONDS = 86400;

    @Produces
    @CustomCacheControl
    public CacheControl getCustomCacheControl() {
        return new CacheControl();
    }
}
