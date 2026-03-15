package io.serenditree.branch.seed.repository;

import io.quarkus.logging.Log;
import io.serenditree.root.rest.transfer.ApiResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.commons.lang3.StringUtils;
import org.opensearch.client.opensearch._types.OpenSearchException;


/**
 * Maps {@link OpenSearchException}s to suitable HTTP status codes.
 */
@Provider
public class SeedRepositoryExceptionMapper implements ExceptionMapper<OpenSearchException> {

  @Override
  public Response toResponse(OpenSearchException e) {
    final Response.ResponseBuilder responseBuilder = Response.serverError();
    final String errorType = e.error().type();

    if (StringUtils.isNotBlank(errorType)) {
      if (errorType.equals("index_not_found_exception")) {
        responseBuilder.status(Response.Status.NOT_FOUND);
      } else if (errorType.equals("resource_already_exists_exception")) {
        responseBuilder.status(Response.Status.CONFLICT);
      } else {
        responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
        Log.error(e);
      }
    } else {
      responseBuilder.status(Response.Status.INTERNAL_SERVER_ERROR);
      Log.error(e);
    }

    return responseBuilder
        .entity(new ApiResponse(e.getMessage()))
        .build();
  }
}
