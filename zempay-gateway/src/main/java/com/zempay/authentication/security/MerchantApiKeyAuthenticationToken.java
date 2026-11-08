package com.zempay.authentication.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class MerchantApiKeyAuthenticationToken
        extends AbstractAuthenticationToken {

    private final Object principal;
    private final String credentials;

    private MerchantApiKeyAuthenticationToken(
            Object principal,
            String credentials,
            boolean authenticated
    ) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.principal = principal;
        this.credentials = credentials;
        super.setAuthenticated(authenticated);
    }

    public static MerchantApiKeyAuthenticationToken unauthenticated(
            String apiKey
    ) {
        return new MerchantApiKeyAuthenticationToken(
                null, apiKey, false
        );
    }

    public static MerchantApiKeyAuthenticationToken authenticated(
            MerchantPrincipal principal
    ) {
        return new MerchantApiKeyAuthenticationToken(
                principal, null, true
        );
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException(
                    "Use the authenticated factory method."
            );
        }

        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
    }
}