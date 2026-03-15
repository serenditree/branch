package com.serenditree.fence.model;

/**
 * Exception that signalizes the creation of an invalid {@link FenceRecordAssertion}
 */
public class FenceRecordException extends SecurityException {

    /**
     * Reasons for an invalid {@link FenceRecordAssertion}.
     */
    public enum Reason {
        INVALID_USER_ID,
        INVALID_ENTITY_ID,
        INVALID_ACTION_TYPE,
        INVALID_REQUIREMENT,
    }

    private final Reason reason;

    /**
     * Package-private constructor. This exception is not supposed to be thrown in another package.
     *
     * @param reason Reason for this exception.
     */
    FenceRecordException(Reason reason) {
        this.reason = reason;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Reason getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "FenceRecordException{" +
               "reason=" + reason +
               '}';
    }
}
