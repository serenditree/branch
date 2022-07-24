package com.serenditree.branch.seed.model.filter;

import com.serenditree.branch.seed.model.entities.Seed;
import com.serenditree.root.data.geo.model.LngLatBounds;

public class SeedFilter extends Seed {
    private LngLatBounds bounds = null;
    private SortingType sort = null;
    private Integer skip = null;
    private Integer limit = null;

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

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
