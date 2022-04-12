package com.example.security.webauthn.login;

import com.example.json.JsonUtils;
import com.yubico.webauthn.AssertionResult;
import com.yubico.webauthn.exception.AssertionFailedException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class FidoAuthenticationProvider implements AuthenticationProvider {

    private final LoginService loginService;

    public FidoAuthenticationProvider(LoginService loginService) {
        this.loginService = loginService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        var email = (String) authentication.getPrincipal();
        var credentials = (String) authentication.getCredentials();

        var loginFinishRequest = JsonUtils.fromJson(credentials,LoginFinishRequest.class);

        try {
            AssertionResult assertionResult = this.loginService.finishLogin(loginFinishRequest);
            if(assertionResult.isSuccess()){
                var result =  new FidoAuthenticationToken(email,assertionResult, Set.of());
                return result;
            }
            throw new BadCredentialsException("FIDO WebAuthn failed");
        } catch (AssertionFailedException e) {
            throw new BadCredentialsException("unable to login", e);
        }

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
