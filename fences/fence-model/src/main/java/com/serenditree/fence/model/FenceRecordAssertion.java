package com.serenditree.fence.model;

import com.serenditree.fence.model.api.fluent.*;
import com.serenditree.fence.model.enums.FenceActionType;
import org.apache.commons.lang3.StringUtils;

/**
 * Class that encapsulates security information for assertion against available {@link FenceRecord}s
 */
public class FenceRecordAssertion {
    private final String userId;
    private final String entityId;
    private final FenceActionType actionType;
    private final boolean recordRequired;
    private final int recordCount;

    /**
     * Private constructor.
     *
     * @param userId         ID of the authenticated user.
     * @param entityId       ID of the targeted entity.
     * @param actionType     Requested action to perform.
     * @param recordRequired Flag that indicates if the presence of absence of the defined record is required.
     * @param recordCount    Checks the number of found records. For example an action might be allowed if 2 not-expired
     *                       records are found.
     */
    private FenceRecordAssertion(String userId,
                                 String entityId,
                                 FenceActionType actionType,
                                 boolean recordRequired,
                                 int recordCount) {
        this.userId = userId;
        this.entityId = entityId;
        this.actionType = actionType;
        this.recordRequired = recordRequired;
        this.recordCount = recordCount;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a fluent builder which has to be completed at once.
     *
     * @return Fluent builder.
     */
    public static SetUserId fluentBuilder() {
        return new Builder();
    }

    /**
     * Returns a build which can be used to build an assertion step by step.
     *
     * @return Sequential builder for step by step creation.
     */
    public static FenceRecordAssertionBuilder sequentialBuilder() {
        return new Builder();
    }

    public String getUserId() {
        return userId;
    }

    public String getEntityId() {
        return entityId;
    }

    public FenceActionType getActionType() {
        return actionType;
    }

    public boolean isRecordRequired() {
        return recordRequired;
    }

    public int getRecordCount() {
        return recordCount;
    }

    @Override
    public String toString() {
        return "{userId: \"" + userId +
                "\", entityId: \"" + entityId +
                "\", action: \"" + actionType +
                "\", recordRequired. " + recordRequired +
                ", recordCount: " + recordCount + "}";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // BUILDER
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * A builder that asserts the creation of a valid assertion.
     */
    private static class Builder implements FenceRecordAssertionBuilder {

        private String userId = null;
        private String entityId = null;
        private FenceActionType actionType = null;
        private Boolean recordRequired = null;
        private int recordCount = -1;

        @Override
        public SetEntityId setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        @Override
        public SetActionType setEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        @Override
        public SetRecordRequired setActionType(FenceActionType actionType) {
            this.actionType = actionType;
            return this;
        }

        @Override
        public FenceRecordAssertionFinalizer setRecordRequired(boolean recordRequired) {
            this.recordRequired = recordRequired;
            return this;
        }

        @Override
        public FenceRecordAssertionFinalizer setRecordCount(int recordCount) {
            this.recordCount = recordCount;
            return this;
        }

        @Override
        public FenceRecordAssertion build() {
            if (StringUtils.isBlank(this.userId)) {
                throw new FenceRecordException(FenceRecordException.Reason.INVALID_USER_ID);
            }
            if (StringUtils.isBlank(this.entityId)) {
                throw new FenceRecordException(FenceRecordException.Reason.INVALID_ENTITY_ID);
            }
            if (this.actionType == null) {
                throw new FenceRecordException(FenceRecordException.Reason.INVALID_ACTION_TYPE);
            }
            if (this.recordRequired == null) {
                throw new FenceRecordException(FenceRecordException.Reason.INVALID_REQUIREMENT);
            }

            return new FenceRecordAssertion(
                    this.userId,
                    this.entityId,
                    this.actionType,
                    this.recordRequired,
                    this.recordCount
            );
        }
    }
}
