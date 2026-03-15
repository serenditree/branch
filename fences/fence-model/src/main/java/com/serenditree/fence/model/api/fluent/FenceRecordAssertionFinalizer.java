package com.serenditree.fence.model.api.fluent;

import com.serenditree.fence.model.FenceRecordAssertion;

public interface FenceRecordAssertionFinalizer {
    FenceRecordAssertionFinalizer setRecordCount(int recordCount);

    FenceRecordAssertion build();
}
