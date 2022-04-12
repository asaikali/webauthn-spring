package com.example.security.webauthn.login;

import com.example.json.JsonUtils;
import com.yubico.webauthn.AssertionResult;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class FidoAuthenticationToken extends UsernamePasswordAuthenticationToken {
    private final String assertionResultJson;

    public FidoAuthenticationToken(String email, AssertionResult assertionResult, Collection<? extends GrantedAuthority> authorities) {
        super(email, null, authorities);
        this.assertionResultJson = JsonUtils.toJson(assertionResult);
    }

    public String getAssertionResultJson() {
        return assertionResultJson;
    }
}
