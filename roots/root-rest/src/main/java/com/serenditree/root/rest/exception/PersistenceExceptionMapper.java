package com.serenditree.root.rest.exception;

import com.serenditree.root.etc.maple.Maple;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class PersistenceExceptionMapper implements ExceptionMapper<PersistenceException> {

    @Override
    public Response toResponse(PersistenceException persistenceException) {
        Throwable rootCause = Maple.toRootCause(persistenceException);
        if (rootCause instanceof EntityNotFoundException || rootCause instanceof NoResultException) {
            return Response
                .status(Response.Status.NOT_FOUND)
                .build();
        } else if (rootCause instanceof EntityExistsException) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .build();
        } else {
            return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .build();
        }
    }
}
