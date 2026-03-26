package com.example.moneymate.protectedtestapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "identity-broker")
public class IdentityBrokerProperties {

    private String issuerUri = "http://localhost:9000";
    private String metadataUri;
    private String deviceAuthorizationEndpoint;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getMetadataUri() {
        if (StringUtils.hasText(metadataUri)) {
            return metadataUri;
        }
        return stripTrailingSlash(issuerUri) + "/.well-known/openid-configuration";
    }

    public void setMetadataUri(String metadataUri) {
        this.metadataUri = metadataUri;
    }

    public String getDeviceAuthorizationEndpoint() {
        if (StringUtils.hasText(deviceAuthorizationEndpoint)) {
            return deviceAuthorizationEndpoint;
        }
        return stripTrailingSlash(issuerUri) + "/oauth2/device_authorization";
    }

    public void setDeviceAuthorizationEndpoint(String deviceAuthorizationEndpoint) {
        this.deviceAuthorizationEndpoint = deviceAuthorizationEndpoint;
    }

    private static String stripTrailingSlash(String uri) {
        if (!StringUtils.hasText(uri)) {
            return "";
        }
        return uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
    }
}
