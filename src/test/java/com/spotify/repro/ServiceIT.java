/**
 * Copyright (C) 2016 Spotify AB
 */

package com.spotify.repro;

import com.spotify.apollo.Request;
import com.spotify.apollo.Response;
import com.spotify.apollo.Status;
import com.spotify.apollo.test.ServiceHelper;

import org.junit.Rule;
import org.junit.Test;

import okio.ByteString;

import static com.spotify.apollo.test.unit.ResponseMatchers.hasPayload;
import static com.spotify.apollo.test.unit.ResponseMatchers.hasStatus;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * A simple integration test that tests that the service can start and respond to 
 * a request.
 * <p>
 * Extend this integration test with more tests specific to your service! We 
 * recommend that you take a look a Cucumber for acceptance test definitions.</p>
 */
public class ServiceIT {

  @Rule
  public ServiceHelper serviceHelper = ServiceHelper.create(WireRepro::configure, ServiceRunner.SERVICE_NAME);

  @Test
  public void shouldRespondToExampleEndpoint() throws Exception {
    Request request = Request.forUri("hm://sysmodel-jsonapi/example/floop");
    Response<ByteString> response = serviceHelper.request(request).toCompletableFuture().get();

    assertThat(response, hasStatus(equalTo(Status.OK)));
    assertThat(response, hasPayload(equalTo(ByteString.encodeUtf8("hello floop!"))));
  }
}
