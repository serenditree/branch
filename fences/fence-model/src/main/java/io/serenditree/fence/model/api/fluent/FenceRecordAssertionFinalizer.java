package io.serenditree.fence.model.api.fluent;

import io.serenditree.fence.model.FenceRecordAssertion;

public interface FenceRecordAssertionFinalizer {
    FenceRecordAssertionFinalizer setRecordCount(int recordCount);

    FenceRecordAssertion build();
}
