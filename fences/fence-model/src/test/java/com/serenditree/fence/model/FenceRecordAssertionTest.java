package com.serenditree.fence.model;

import com.serenditree.fence.model.api.fluent.FenceRecordAssertionBuilder;
import com.serenditree.fence.model.enums.FenceActionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FenceRecordAssertionTest {

    @Test
    void buildValidAssertion() {
        FenceRecordAssertion assertion1 = FenceRecordAssertion.fluentBuilder()
                .setUserId("test1")
                .setEntityId("test2")
                .setActionType(FenceActionType.CRUD)
                .setRecordRequired(true)
                .build();

        assertEquals("test1", assertion1.getUserId());
        assertEquals("test2", assertion1.getEntityId());
        assertEquals(FenceActionType.CRUD, assertion1.getActionType());
        assertTrue(assertion1.isRecordRequired());
        assertEquals(-1, assertion1.getRecordCount());

        FenceRecordAssertion assertion2 = FenceRecordAssertion.fluentBuilder()
                .setUserId("test3")
                .setEntityId("test4")
                .setActionType(FenceActionType.METHOD)
                .setRecordRequired(false)
                .setRecordCount(2)
                .build();

        assertEquals("test3", assertion2.getUserId());
        assertEquals("test4", assertion2.getEntityId());
        assertEquals(FenceActionType.METHOD, assertion2.getActionType());
        assertFalse(assertion2.isRecordRequired());
        assertEquals(2, assertion2.getRecordCount());
    }

    @Test
    @SuppressWarnings("java:S5778")
    void buildInvalidAssertionUserIdBlank() {
        assertEquals(
                FenceRecordException.Reason.INVALID_USER_ID,
                assertThrows(
                        FenceRecordException.class,
                        () -> FenceRecordAssertion.fluentBuilder()
                                .setUserId(" ")
                                .setEntityId("test")
                                .setActionType(FenceActionType.CRUD)
                                .setRecordRequired(true)
                                .build()
                ).getReason()
        );
    }

    @Test
    @SuppressWarnings("java:S5778")
    void buildInvalidAssertionUserIdNull() {
        assertEquals(
                FenceRecordException.Reason.INVALID_USER_ID,
                assertThrows(
                        FenceRecordException.class,
                        () -> FenceRecordAssertion.fluentBuilder()
                                .setUserId(null)
                                .setEntityId("test")
                                .setActionType(FenceActionType.CRUD)
                                .setRecordRequired(true)
                                .build()
                ).getReason()
        );
    }

    @Test
    @SuppressWarnings("java:S5778")
    void buildInvalidAssertionEntityIdBlank() {
        assertEquals(
                FenceRecordException.Reason.INVALID_ENTITY_ID,
                assertThrows(
                        FenceRecordException.class,
                        () -> FenceRecordAssertion.fluentBuilder()
                                .setUserId("test")
                                .setEntityId("")
                                .setActionType(FenceActionType.CRUD)
                                .setRecordRequired(true)
                                .build()
                ).getReason()
        );
    }

    @Test
    @SuppressWarnings("java:S5778")
    void buildInvalidAssertionEntityIdNull() {
        assertEquals(
                FenceRecordException.Reason.INVALID_ENTITY_ID,
                assertThrows(
                        FenceRecordException.class,
                        () -> FenceRecordAssertion.fluentBuilder()
                                .setUserId("test")
                                .setEntityId(null)
                                .setActionType(FenceActionType.CRUD)
                                .setRecordRequired(true)
                                .build()
                ).getReason()
        );
    }

    @Test
    void buildValidAssertionSequentially() {
        FenceRecordAssertionBuilder assertionBuilder = FenceRecordAssertion.sequentialBuilder();

        assertionBuilder.setUserId("test1");
        assertionBuilder.setEntityId("test2");
        assertionBuilder.setActionType(FenceActionType.CRUD);
        assertionBuilder.setRecordRequired(false);

        FenceRecordAssertion assertion = assertionBuilder.build();

        assertEquals("test1", assertion.getUserId());
        assertEquals("test2", assertion.getEntityId());
        assertEquals(FenceActionType.CRUD, assertion.getActionType());
        assertFalse(assertion.isRecordRequired());
        assertEquals(-1, assertion.getRecordCount());
    }

    @Test
    void buildInvalidAssertionSequentiallyUntilValid() {
        FenceRecordAssertionBuilder assertionBuilder = FenceRecordAssertion.sequentialBuilder();

        assertEquals(
                FenceRecordException.Reason.INVALID_USER_ID,
                assertThrows(FenceRecordException.class, assertionBuilder::build).getReason()
        );

        assertionBuilder.setUserId("test1");

        assertEquals(
                FenceRecordException.Reason.INVALID_ENTITY_ID,
                assertThrows(FenceRecordException.class, assertionBuilder::build).getReason()
        );

        assertionBuilder.setEntityId("test2");

        assertEquals(
                FenceRecordException.Reason.INVALID_ACTION_TYPE,
                assertThrows(FenceRecordException.class, assertionBuilder::build).getReason()
        );

        assertionBuilder.setActionType(FenceActionType.METHOD);

        assertEquals(
                FenceRecordException.Reason.INVALID_REQUIREMENT,
                assertThrows(FenceRecordException.class, assertionBuilder::build).getReason()
        );

        FenceRecordAssertion assertion = assertionBuilder
                .setRecordRequired(true)
                .setRecordCount(2)
                .build();

        assertEquals("test1", assertion.getUserId());
        assertEquals("test2", assertion.getEntityId());
        assertEquals(FenceActionType.METHOD, assertion.getActionType());
        assertTrue(assertion.isRecordRequired());
        assertEquals(2, assertion.getRecordCount());
    }

    @Test
    void fenceRecordExceptionIsSecurityException() {
        assertThrows(
                SecurityException.class,
                FenceRecordAssertion.sequentialBuilder()::build
        );
    }
}
