package io.serenditree.branch.seed.model.filter;

import io.quarkus.runtime.annotations.RegisterForReflection;
import io.serenditree.branch.seed.model.entities.Seed;
import io.serenditree.branch.seed.model.types.SortingType;
import io.serenditree.root.data.geo.model.LngLatBounds;

@RegisterForReflection
public class SeedFilter extends Seed {
    private LngLatBounds bounds = null;
    private SortingType sort = null;
    private int skip = 0;
    private int limit = 10;

    public LngLatBounds getBounds() {
        return bounds;
    }

    public void setBounds(LngLatBounds bounds) {
        this.bounds = bounds;
    }

    public SortingType getSort() {
        return sort;
    }

    public void setSort(SortingType sort) {
        this.sort = sort;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
