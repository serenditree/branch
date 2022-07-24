package com.serenditree.root.rest.exception;

import com.serenditree.root.etc.maple.Maple;

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
        if (Maple.toRootCause(persistenceException) instanceof EntityNotFoundException ||
                Maple.toRootCause(persistenceException) instanceof NoResultException) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            throw persistenceException;
        }
    }
}
