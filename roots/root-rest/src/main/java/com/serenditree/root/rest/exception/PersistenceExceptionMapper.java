package com.serenditree.root.rest.exception;

import com.serenditree.root.etc.maple.Maple;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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
