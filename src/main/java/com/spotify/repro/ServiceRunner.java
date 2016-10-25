/**
 * Copyright (C) 2016 Spotify AB.
 */

package com.spotify.repro;

import com.spotify.apollo.core.Service;
import com.spotify.apollo.httpservice.HttpService;
import com.spotify.apollo.httpservice.LoadingException;

/**
 * Application entry point.
 */
public class ServiceRunner {

  static final String SERVICE_NAME = "repro";

  private ServiceRunner() { }

  /**
   * Runs the app locally.
   *
   * <p>$ curl http://localhost:8080/example/bazinga
   *
   * <p>For running the app outside the IDE, use java -jar:
   *
   * <p>$ mvn package
   * $ java -Dlogback.configurationFile=src/test/resources/logback.xml  -jar target/elide-repro.jar
   */
  public static void main(final String... args) throws LoadingException {
    Service service = HttpService.usingAppInit(WireRepro::configure, SERVICE_NAME)
        .withEnvVarPrefix("SPOTIFY")
        .build();

    HttpService.boot(service, args);
  }

}
