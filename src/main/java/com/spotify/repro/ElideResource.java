/**
 * Copyright (C) 2016 Spotify AB.
 */

package com.spotify.repro;

import static java.util.Objects.requireNonNull;

import com.spotify.apollo.RequestContext;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.apollo.route.AsyncHandler;
import com.spotify.apollo.route.Route;
import com.yahoo.elide.Elide;
import com.yahoo.elide.ElideResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.ws.rs.core.MultivaluedHashMap;
import okio.ByteString;

/**
 * Hooks up Apollo endpoints with Elide. TODO: can probably move into an apollo-elide library.
 */
class ElideResource {

  private final Elide elide;
  private final String pathPrefix;

  ElideResource(Elide elide, String pathPrefix) {
    this.elide = requireNonNull(elide);
    this.pathPrefix = pathPrefix.endsWith("/") ? pathPrefix : pathPrefix + "/";
  }

  Stream<Route<AsyncHandler<Response<ByteString>>>> routes() {
    return Stream.of(
        Route.sync("GET", pathPrefix + "<query-path:path>", this::get),
        Route.sync("POST", pathPrefix + "<query-path:path>", this::post),
        Route.sync("DELETE", pathPrefix + "<query-path:path>", this::delete),
        Route.sync("PUT", pathPrefix + "<query-path:path>", this::put),
        Route.sync("PATCH", pathPrefix + "<query-path:path>", this::patch)
    )
        .map(r -> r.withMiddleware(this::serializeJsonApi));
  }

  private AsyncHandler<Response<ByteString>> serializeJsonApi(
      AsyncHandler<Response<String>> handler) {
    return requestContext -> handler.invoke(requestContext)
        .thenApply(response -> response.withHeader("Content-Type", "application/vnd.api+json")
            .withPayload(response.payload().map(ByteString::encodeUtf8).orElse(null)));
  }

  private Response<String> get(RequestContext requestContext) {
    ElideResponse elideResponse =
        // TODO: do something better wrt the user; should standardise on something; this something
        // should probably be oauth-related somehow
        elide.get(requestContext.pathArgs().get("query-path"), queryParams(
            requestContext.request().parameters()), null);

    return Response.of(
        Status.createForCode(elideResponse.getResponseCode()),
        elideResponse.getBody());
  }

  private Response<String> post(RequestContext requestContext) {
    String body = payloadAsString(requestContext);

    ElideResponse elideResponse =
        // TODO: do something better wrt the user; should standardise on something; this something
        // should probably be oauth-related somehow
        elide.post(requestContext.pathArgs().get("query-path"), body, null);

    return Response.of(
        Status.createForCode(elideResponse.getResponseCode()),
        elideResponse.getBody());
  }

  private Response<String> delete(RequestContext requestContext) {
    ElideResponse elideResponse =
        // TODO: do something better wrt the user; should standardise on something; this something
        // should probably be oauth-related somehow
        elide.delete(
            requestContext.pathArgs().get("query-path"),
            payloadAsString(requestContext),
            null);

    return Response.of(
        Status.createForCode(elideResponse.getResponseCode()),
        elideResponse.getBody());
  }

  private Response<String> put(RequestContext requestContext) {
    return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
  }

  private Response<String> patch(RequestContext requestContext) {
    return Response.forStatus(Status.INTERNAL_SERVER_ERROR);
  }

  private String payloadAsString(RequestContext requestContext) {
    return requestContext.request().payload()
        .map(ByteString::utf8)
        .orElse(null);
  }

  private MultivaluedHashMap<String, String> queryParams(Map<String, List<String>> parameters) {
    MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();

    for (String queryParameterName : parameters.keySet()) {
      map.put(queryParameterName, parameters.get(queryParameterName));
    }

    return map;
  }
}
