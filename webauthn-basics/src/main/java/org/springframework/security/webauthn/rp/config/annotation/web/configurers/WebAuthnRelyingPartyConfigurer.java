package org.springframework.security.webauthn.rp.config.annotation.web.configurers;

import java.util.Map;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.webauthn.rp.WebAuthnAssertionService;
import org.springframework.security.webauthn.rp.WebAuthnRegistrationService;
import org.springframework.security.webauthn.rp.authentication.WebAuthnAssertionAuthenticationProvider;
import org.springframework.security.webauthn.rp.web.WebAuthnLoginFilter;
import org.springframework.security.webauthn.rp.web.WebAuthnRegistrationFilter;
import org.springframework.util.StringUtils;

public final class WebAuthnRelyingPartyConfigurer extends AbstractHttpConfigurer<WebAuthnRelyingPartyConfigurer, HttpSecurity> {

    @Override
    public void init(HttpSecurity httpSecurity) throws Exception {
        WebAuthnAssertionService assertionService = getBean(httpSecurity, WebAuthnAssertionService.class);
        WebAuthnAssertionAuthenticationProvider assertionAuthenticationProvider =
                new WebAuthnAssertionAuthenticationProvider(assertionService);
        httpSecurity.authenticationProvider(assertionAuthenticationProvider);
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        WebAuthnRegistrationService registrationService = getBean(httpSecurity, WebAuthnRegistrationService.class);
        WebAuthnRegistrationFilter registrationFilter = new WebAuthnRegistrationFilter(registrationService);
        httpSecurity.addFilterBefore(postProcess(registrationFilter), AbstractPreAuthenticatedProcessingFilter.class);

        AuthenticationManager authenticationManager = httpSecurity.getSharedObject(AuthenticationManager.class);
        WebAuthnLoginFilter loginFilter = new WebAuthnLoginFilter(authenticationManager);
        httpSecurity.addFilterAfter(postProcess(loginFilter), AbstractPreAuthenticatedProcessingFilter.class);
    }

    private static <T> T getBean(HttpSecurity httpSecurity, Class<T> type) {
        return httpSecurity.getSharedObject(ApplicationContext.class).getBean(type);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getBean(HttpSecurity httpSecurity, ResolvableType type) {
        ApplicationContext context = httpSecurity.getSharedObject(ApplicationContext.class);
        String[] names = context.getBeanNamesForType(type);
        if (names.length == 1) {
            return (T) context.getBean(names[0]);
        }
        if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        }
        throw new NoSuchBeanDefinitionException(type);
    }

    private static <T> T getOptionalBean(HttpSecurity httpSecurity, Class<T> type) {
        Map<String, T> beansMap = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                httpSecurity.getSharedObject(ApplicationContext.class), type);
        if (beansMap.size() > 1) {
            throw new NoUniqueBeanDefinitionException(type, beansMap.size(),
                    "Expected single matching bean of type '" + type.getName() + "' but found " +
                            beansMap.size() + ": " + StringUtils.collectionToCommaDelimitedString(beansMap.keySet()));
        }
        return (!beansMap.isEmpty() ? beansMap.values().iterator().next() : null);
    }

    @SuppressWarnings("unchecked")
    private static <T> T getOptionalBean(HttpSecurity httpSecurity, ResolvableType type) {
        ApplicationContext context = httpSecurity.getSharedObject(ApplicationContext.class);
        String[] names = context.getBeanNamesForType(type);
        if (names.length > 1) {
            throw new NoUniqueBeanDefinitionException(type, names);
        }
        return names.length == 1 ? (T) context.getBean(names[0]) : null;
    }

}