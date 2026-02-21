package com.example.security.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * WebAuthn / FIDO requires https for all communications with the backend, so we configure https via
 * the spring boot application.yaml settings. This class adds a http tomcat connector and configures
 * it so that if a request comments in on http it will be redirected to https.
 */
@Configuration
public class TomcatConfiguration {
  @Value("${http.port}")
  private int httpPort;

  @Value("${server.port}")
  private int serverPort;

  @Bean
  public ServletWebServerFactory servletContainer() {
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
    tomcat.addAdditionalTomcatConnectors(createStandardConnector());
    return tomcat;
  }

  /**
   * Setting the redirect port causes spring security / tomcat to know how to redirect http to https
   */
  private Connector createStandardConnector() {
    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setPort(httpPort);
    connector.setRedirectPort(serverPort);
    connector.setSecure(false);
    return connector;
  }
}
